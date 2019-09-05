#!/usr/bin/env bash

backup () {
    #This takes the 2 values of workers


    result="[\"datasets/myMaster\"]"    #curl -s http://192.168.99.100:8500/v1/kv/?keys

    if [[ -z "${result}" ]]; then
        echo "no keys? what is that even mean?"
    fi

    if [[ ${result} != *","* ]]; then
        key=$(echo ${result} | cut -d'"' -f 2)
        echo ${key}
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

    exit 1
    rm backup.json

    key="master/myMaster"
    #value=$(curl -s http://192.168.99.100/v1/kv/${EXAREME_MASTER_PATH}/$(curl -s ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/?keys | jq -r '.[]' | sed "s/${EXAREME_MASTER_PATH}\///g")?raw)
    #value=$(curl -s http://192.168.99.100/v1/kv/master/myMaster?raw)
    value="10.20.30.49"
    #echo $key"="$value >> backup.json
    echo "myMaster=10.20.30.40" >> backup.json
    echo "myWorker1=10.20.30.100" >> backup.json
}

backup

while true
do
    #if [[ "$(curl -s http://192.168.99.100:8500/v1/health/state/passing | jq -r '.[].Status')" = "passing" ]];  then
    if [[ -s backup.json  ]]; then       #file not empty
        while read -r line ; do
            key=$( echo "$line" | cut -d'=' -f 1)
            file_value=$( echo "$line" | cut -d'=' -f 2)
            #kv_value=$(curl -s http://192.168.99.100/v1/kv/$key?raw)
            kv_value="10.20.30.9"
            if [[ -z "$kv_value" ]]; then      #kapote eixe timi. ara ekane restart to service
                echo "service restared"
                curl -s -X PUT -d @- http://192.168.99.100/v1/kv/${key} <<< ${file_value}
            elif [[ "kv_value" == "file_value" ]]; then
                echo "nothing happens"
            else
                echo "need replace"
                sed -i '/'$key'/ s/'$file_value'/'kv_value'/' backup.json
            fi
        done < backup.json      #ana takta xronika diastimata ksana backup. alla pote to backup thelei prosoxi min ginei backup kai to consul exei molis kanei restart
    else
        touch backup.json
        backup
    fi
    break
    #fi
done


