#!/usr/bin/env bash

backup () {
    #Below we take all the keys-values from Consul and place them in a backup file

    result=$(curl -s exareme-keystore:8500/v1/kv/master?keys)   #"[\"master/myMaster\"]"

    if [[ -z ${result} ]]; then     #variable empty
        echo "result is empty"
        if [[ -s backup.json ]]; then   #file not empty....
            echo "maybe Consul restarted... place your backup in Consul"
        else
            echo "nothing yet..maybe too soon"
            sleep 1
            #exit    #is that needed?
        fi
    fi

    if [[ ${result} != *","* ]]; then
        echo "we only have a master, so it should be here"
        key=$(echo ${result} | cut -d'"' -f 2)
        echo ${key}
        value=$(curl -s exareme-keystore:8500/v1/kv/${key}?raw)
        echo ${value}
        echo ${key}"="${value} >> backup.json
    else
        whole_key=$(echo ${result} | cut -d',' -f 1)
        key=$(echo ${whole_key} | cut -d '"' -f 2)
        echo ${key}

        n=1
        while true
        do
            n=$((${n} + 1))
            whole_key=$(echo ${result} | cut -d',' -f $n)
            if [[ -z ${whole_key} ]]; then
                break
            fi
            key=$(echo ${whole_key} | cut -d'"' -f 1)
            echo ${key}
        done

    fi

    #rm backup.json

    #key="master/myMaster"
    #value=$(curl -s http://192.168.99.100/v1/kv/${EXAREME_MASTER_PATH}/$(curl -s ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/?keys | jq -r '.[]' | sed "s/${EXAREME_MASTER_PATH}\///g")?raw)
    #value=$(curl -s http://192.168.99.100/v1/kv/master/myMaster?raw)
    #value="10.20.30.49"
    #echo $key"="$value >> backup.json
    #echo "myMaster=10.20.30.40" >> backup.json
    #echo "myWorker1=10.20.30.100" >> backup.json
}
sleep 5

while true
do
    if [[ "$(curl -s exareme-keystore:8500/v1/health/state/passing | jq -r '.[].Status')" = "passing" ]];  then
        sleep 1
        if [[ -s backup.json  ]]; then       #file not empty
            while read -r line ; do
                key=$( echo "$line" | cut -d'=' -f 1)
                echo "here is the key from the backup./jsom"
                echo ${key}
                file_value=$( echo "$line" | cut -d'=' -f 2)
                echo "here is the value from the backup./jsom"
                echo ${file_value}
                kv_value=$(curl -s exareme-keystore:8500/v1/kv/${key}?raw)
                echo "here is the kv_value from Consul"
                echo ${kv_value}

                if [[ -z ${kv_value} ]]; then      #kapote eixe timi. ara ekane restart to service
                    echo "service restared"
                    curl -s -X PUT -d @- exareme-keystore:8500/v1/kv/${key} <<< ${file_value}
                elif [[ "$kv_value" == "$file_value" ]]; then
                    echo "nothing happens"
                else
                    echo "need replace"
                    sed -i '/'${key}'/ s/'${file_value}'/'${kv_value}'/' backup.json
                fi
            done < backup.json      #ana takta xronika diastimata ksana backup. alla pote to backup thelei prosoxi min ginei backup kai to consul exei molis kanei restart
        else
            echo "I will crteate the file and write key-value in it"
            touch backup.json
            backup
        fi
    fi
    sleep 2
done



