package madgik.exareme.master.engine.iterations.handler;

import madgik.exareme.common.consts.HBPConstants;
import madgik.exareme.master.client.AdpDBClient;
import madgik.exareme.master.client.AdpDBClientFactory;
import madgik.exareme.master.client.AdpDBClientProperties;
import madgik.exareme.master.engine.AdpDBManager;
import madgik.exareme.master.engine.iterations.exceptions.IterationsFatalException;
import madgik.exareme.master.engine.iterations.scheduler.IterationsScheduler;
import madgik.exareme.master.engine.iterations.state.IterationsStateManager;
import madgik.exareme.master.engine.iterations.state.IterationsStateManagerImpl;
import madgik.exareme.master.engine.iterations.state.IterativeAlgorithmState;
import madgik.exareme.master.queryProcessor.composer.AlgorithmProperties;
import madgik.exareme.master.queryProcessor.composer.Composer;
import madgik.exareme.master.queryProcessor.composer.Exceptions.ComposerException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.rmi.RemoteException;

import static madgik.exareme.common.consts.HBPConstants.DEMO_ALGORITHMS_WORKING_DIRECTORY;
import static madgik.exareme.master.engine.iterations.handler.IterationsHandlerDFLUtils.copyAlgorithmTemplatesToDemoDirectory;

/**
 * Handles iteration requests
 *
 * <p> This involves either submitting a new iterative algorithm or querying the status of an
 * already running one.
 *
 * @author Christos Aslanoglou <br> caslanoglou@di.uoa.gr <br> University of Athens / Department of
 * Informatics and Telecommunications.
 */
public class IterationsHandler {
    private static final Logger log = Logger.getLogger(IterationsHandler.class);

    // Singleton specific -----------------------------------------------------------------------
    private IterationsHandler() {
    }

    private static final IterationsHandler instance = new IterationsHandler();

    public static IterationsHandler getInstance() {
        return instance;
    }

    // Instance fields --------------------------------------------------------------------------
    private IterationsStateManager iterationsStateManager = IterationsStateManagerImpl.getInstance();
    private IterationsScheduler iterationsScheduler = IterationsScheduler.getInstance();

    // Instance methods -------------------------------------------------------------------------

    /**
     * Submits a new iterative algorithm.
     *
     * <p> Generates required DFL scripts and submits initial query, while returning the new
     * algorithm's key. This can be used for status querying.
     *
     * @param adpDBManager        the AdpDBManager of the gateway
     * @param algorithmProperties the properties of this algorithm
     * @return the iterative algorithm state
     * @throws IterationsFatalException if it failed to initialize an AdpDBClient for the current
     *                                  algorithm
     * @see AlgorithmProperties
     */
    public IterativeAlgorithmState handleNewIterativeAlgorithmRequest(
            AdpDBManager adpDBManager, String algorithmKey,
            AlgorithmProperties algorithmProperties) {

        // Generate the adpDBClient for this algorithm and a new
        // IterativeAlgorithmState
        String database = HBPConstants.DEMO_DB_WORKING_DIRECTORY + algorithmKey;
        // -----------------------------------------
        // Create AdpDBClient of iterative algorithm state (used for submitting all queries)
        AdpDBClient adpDBClient;
        try {
            AdpDBClientProperties clientProperties =
                    new AdpDBClientProperties(database, "", "",
                            false, false, -1, 10);
            adpDBClient = AdpDBClientFactory.createDBClient(adpDBManager, clientProperties);
        } catch (RemoteException e) {
            String errMsg = "Failed to initialize " + AdpDBClient.class.getSimpleName()
                    + " for algorithm: " + algorithmKey;
            log.error(errMsg);
            throw new IterationsFatalException(errMsg, e);
        }

        log.debug("Created " + AdpDBClient.class.getSimpleName() + " for iterative algorithm: " + algorithmKey + ".");

        IterativeAlgorithmState iterativeAlgorithmState =
                new IterativeAlgorithmState(algorithmKey, algorithmProperties, adpDBClient);
        log.debug("Created " + IterativeAlgorithmState.class.getSimpleName() + " for: "
                + iterativeAlgorithmState.toString() + ".");
        // -----------------------------------------
        // Copy template files to algorithm's demo directory, prepare DFL scripts and then persist
        // them, as well.

        String dflScripts[];
        String demoCurrentAlgorithmDir =
                DEMO_ALGORITHMS_WORKING_DIRECTORY + "/" + algorithmKey;

        if (algorithmProperties.getType().equals(AlgorithmProperties.AlgorithmType.iterative)) {
            // Copying algorithm's template files under demo directory, so that these are edited per
            // algorithm's execution.
            copyAlgorithmTemplatesToDemoDirectory(algorithmProperties.getName(), algorithmKey);

            dflScripts = IterationsHandlerDFLUtils.prepareDFLScripts(
                    demoCurrentAlgorithmDir, algorithmKey, algorithmProperties, iterativeAlgorithmState);

        } else if (algorithmProperties.getType().equals(AlgorithmProperties.AlgorithmType.python_iterative)) {
            // Python iterative algorithms need a smaller procedure. The algorithm files are not copied and modified.
            dflScripts = new String[
                    IterativeAlgorithmState.IterativeAlgorithmPhasesModel.values().length];

            int dflScriptIdx = 0;
            for (IterativeAlgorithmState.IterativeAlgorithmPhasesModel phase :
                    IterativeAlgorithmState.IterativeAlgorithmPhasesModel.values()) {
                try {
                    dflScripts[dflScriptIdx++] = Composer.composePythonIterativeAlgorithmsDFLScript(
                            algorithmKey, algorithmProperties, phase);

                } catch (ComposerException e) {
                    throw new IterationsFatalException("Composer failure to generate DFL script for phase: "
                            + phase.name() + ".", e);
                }
            }
        } else {
            throw new IterationsFatalException("handleNewIterativeAlgorithmRequest wasn't called by iterative algorithm");
        }

        for (String dflScript : dflScripts) {
            log.info("dfl: " + dflScript);
        }

        try {
            for (IterativeAlgorithmState.IterativeAlgorithmPhasesModel phase :
                    IterativeAlgorithmState.IterativeAlgorithmPhasesModel.values()) {
                Composer.persistDFLScriptToAlgorithmsDemoDirectory(
                        demoCurrentAlgorithmDir, dflScripts[phase.ordinal()], phase);
            }
        } catch (IOException e) {
            log.error("Failed to persist DFL scripts for algorithm [" + algorithmKey + "]");
        }

        iterativeAlgorithmState.setDflScripts(dflScripts);

        log.debug("Generated DFL scripts for: " + iterativeAlgorithmState.toString());

        // -----------------------------------------
        // Only after DFL initialization, submit to IterationsStateManager and schedule it
        iterationsStateManager.submitIterativeAlgorithm(algorithmKey, iterativeAlgorithmState);

        /*
         Lock the instance so that IOCtrl is set, and thus NewAlgorithmEventHandler doesn't
         run (it would acquire the lock first). This is to cover a case in which the algorithm
         execution crashes during submission of the init-phase query and the IOCtrl hasn't been
         already set, and thus error-response to the client wasn't forwarded.
         */
        iterativeAlgorithmState.lock();

        iterationsScheduler.scheduleNewAlgorithm(algorithmKey);

        log.info(iterativeAlgorithmState.toString() + " was submitted.");

        return iterativeAlgorithmState;
    }

    /**
     * Removes an {@link IterativeAlgorithmState} from
     * {@link madgik.exareme.master.engine.iterations.state.IterationsStateManager}
     *
     * @param algorithmKey the algorithm's key (uniquely identifies an algorithm)
     */
    public void removeIterativeAlgorithmStateInstanceFromISM(String algorithmKey) {
        log.info("Removing " + IterativeAlgorithmState.class.getSimpleName() + " from "
                + IterationsStateManager.class.getSimpleName());
        iterationsStateManager.removeIterativeAlgorithm(algorithmKey);
    }
}
