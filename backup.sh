#!/usr/bin/env bash

backup () {
    #Below we take all the keys-values from Consul and place them in a backup file

    result=$(curl -s exareme-keystore:8500/v1/kv/?keys)
    #result="[\"datasets/myMaster\",\"master/myMaster\",\"master/myMaster1\",\"master/myMaster2\,\"master/myMaster3\"]"
    echo ${result}
    echo "above the rsulr"
    if [[ "${result}" == "[]" ]]; then     #variable empty
        echo "result is empty"
        if [[ -s backup.json ]]; then   #file not empty....
            echo "maybe Consul restarted... place your backup in Consul"
        else
            echo "nothing yet..maybe too soon"
            sleep 1
            result=$(curl -s exareme-keystore:8500/v1/kv/?keys)   #"[\"master/myMaster\"]"
            echo ${result}
            echo "above the rsulr"
        fi
    fi

    if [[ ${result} != *","* ]]; then
        key=$(echo ${result} | cut -d'"' -f 2)
        value=$(curl -s exareme-keystore:8500/v1/kv/${key}?raw)
        echo ${key}"="${value} >> backup.json
    else
        whole_key=$(echo ${result} | cut -d',' -f 1)
        key=$(echo ${whole_key} | cut -d '"' -f 2)
        echo ${key}
        value=$(curl -s exareme-keystore:8500/v1/kv/${key}?raw)
        echo ${value}
        echo ${key}"="${value} >> backup.json

        n=1
        while true
        do
            n=$((${n} + 1))
            whole_key=$(echo ${result} | cut -d',' -f ${n})
            echo ${whole_key}
            if [[ -z ${whole_key} ]]; then
                break
            fi
            key=$(echo ${whole_key} | cut -d'"' -f 2)
            echo ${key}
            value=$(curl -s exareme-keystore:8500/v1/kv/${key}?raw)
            echo ${value}
            echo ${key}"="${value} >> backup.json
        done

    fi
}

sleep 5         #initial sleep for services to write their data in Consul key-value store

while true
do
    if [[ "$(curl -s exareme-keystore:8500/v1/health/state/passing | jq -r '.[].Status')" = "passing" ]];  then
        if [[ -s backup.json  ]]; then       #file not empty
            while read -r line ; do
                key=$( echo "$line" | cut -d'=' -f 1)
                echo ${key}
                file_value=$( echo "$line" | cut -d'=' -f 2)
                echo ${file_value}
                kv_value=$(curl -s exareme-keystore:8500/v1/kv/${key}?raw)
                echo ${kv_value}

                if [[ -z ${kv_value} ]]; then      #kapote eixe timi. ara ekane restart to service
                    echo "Consul service restarted. Sync Consul key-value store with Backup file.."
                    curl -s -X PUT -d @- exareme-keystore:8500/v1/kv/${key} <<< ${file_value}
                    echo "Consul key-value store synced.."
                elif [[ "$kv_value" == "$file_value" ]]; then
                    echo "Backup file is already synced with Consul key-value store..Nothing to be synced.."
                else
                    echo "Backup file not synced with Consul..Change backup file.."
                    sed -i -r '/'[\${key}]'/ s/'${file_value}'/'${kv_value}'/' backup.json
                    echo "Backup file synced..... "

                fi
            done < backup.json      #ana takta xronika diastimata ksana backup. alla pote to backup thelei prosoxi min ginei backup kai to consul exei molis kanei restart
        else
            touch backup.json
            backup
        fi
    fi
    sleep 2
done



