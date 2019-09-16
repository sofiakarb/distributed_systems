requirevars 'defaultDB' 'sstype';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.metadatatbl';
----------

var 'metadata' from select jgroup(code,enumerations) from (select code ,enumerations from defaultDB.metadatatbl);

drop table if exists defaultDB.sumofsquares;
create table defaultDB.sumofsquares as
select sumofsquares(no,formula,sst,ssregs,sse,%{sstype}) from defaultDB.globalAnovatbl;

var 'a' from select max(no) from defaultDB.sumofsquares;
insert into defaultDB.sumofsquares
select %{a}+1, "residuals", sse
from defaultDB.globalAnovatbl,(select max(no) as maxno from defaultDB.globalAnovatbl)
where no==maxno;

var 'SST' from select max(sst) from defaultDB.globalAnovatbl;
var 'N' from select N from defaultDB.statistics limit 1;
drop table if exists defaultDB.globalresult;
create table defaultDB.globalresult (`no` int, `model variables` text, `sum of squares` real,`Df` int,`mean square` real, `f` real, `p` real,`eta squared` real, `part eta squared` real, `omega squared` real);

insert into defaultDB.globalresult
select no, modelvariables, sumofsquares, df, meansquare, f, p, etasquared, partetasquared, omegasquared
                from (select anovastatistics(no, modelvariables, sumofsquares, '%{metadata}',%{N},%{SST} ) from defaultDB.sumofsquares);

update defaultDB.globalresult
set `f`= null,`p`= null,`eta squared`= null,`part eta squared`= null, `omega squared`= null where `model variables` =  'residuals';

var 'resulttable' from
select * from (totabulardataresourceformat title:ANOVA types:text,number,number,number,number,number,number,number,number
                select `model variables`, `sum of squares`,`Df`,`mean square`, `f`, `p`,`eta squared`, `part eta squared`, `omega squared`
                from defaultDB.globalresult where `model variables` <> 'intercept' order by no);

var 'resultjson' from
select tabletojson(`model variables`, `sum of squares`,`Df`,`mean square`, `f`, `p`,`eta squared`, `part eta squared`, `omega squared`,
                   "modelvariables,sumofsquares,Df,meansquare,f,p,eta squared,part eta squared, omega squared",1)  as componentresult
from (select * from defaultDB.globalresult where `model variables` <> 'intercept' order by no);

select '{"result": ['||'%{resultjson}'||','||'%{resulttable}'||']}';
