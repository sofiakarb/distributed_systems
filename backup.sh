#!/usr/bin/env bash

backup () {

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
            value2=$( echo "$line" | cut -d'=' -f 2)
            echo $key
            echo $value2
            #value=$(curl -s http://192.168.99.100/v1/kv/$key?raw)
            value="10.20.30.9"
            if [[ -z "$value" ]]; then      #kapote eixe timi. ara ekane restart to service
                curl -s -X PUT -d @- http://192.168.99.100/v1/kv/${key} <<< ${value2}
            elif [[ "$value" == "$value2" ]]; then
                echo "nothing happens"
            else
                echo "need replace"
                sed -i '/'$key'/ s/'$value2'/'$value'/' backup.json
            fi
        done < backup.json
    else
        touch backup.json
        backup
    fi
    break
    #fi
done


