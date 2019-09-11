#!/usr/bin/env bash

check () {
    echo "Going to check.."${1}" "${2}
    while read -r line ; do
        key=$( echo "$line" | cut -d'=' -f 1)
        if [[ "${key}" == "${1}" ]]; then
            echo "Same key."
            file_value=$( echo "$line" | cut -d'=' -f 2)
            if [[ "${file_value}" == "${2}" ]]; then
                echo "Same value.nothing to be done.."
                return 1
            else
                echo "Different value.changing value in backup file.."
                sed -i -r '/'[\${key}]'/ s/'${file_value}'/'${2}'/' backup.json
                return 1
            fi
        fi
    done < backup.json
    echo "Adding a new pair of key-value in backup file"
    echo ${1}"="${2} >> backup.json
}

backup () {
    #Below we take all the keys-values from Consul and place them in a backup file

    result=$(curl -s exareme-keystore:8500/v1/kv/?keys)
    echo ${result}
    if [[ "${result}" == "[]" ]] || [[ "${result}" == "" ]]; then      #variable empty
        echo "Result=" ${result}" is empty"
        if [[ -s backup.json ]]; then       #file not empty....
            echo "Consul service restarted. Sync Consul key-value store with Backup file..(within backup function)"
            while read r line ; do
                key=$( echo "$line" | cut -d'=' -f 1)
                file_value=$( echo "$line" | cut -d'=' -f 2)
                curl -s -X PUT -d @- exareme-keystore:8500/v1/kv/${key} <<< ${file_value}
            done
            echo "Consul key-value store synced..(within backup function)"
            return 1
        else
            "Too soon maybe? sleep.."
            sleep 5
            return 1
        fi
    else
        echo "Result=" ${result}
        #consider if it is ok to delete backup.json and re-create it every time..
        if [[ ${result} != *","* ]]; then       # you need to check each time before you add the key value to back up
            key=$(echo ${result} | cut -d'"' -f 2)
            value=$(curl -s exareme-keystore:8500/v1/kv/${key}?raw)
            check "${key}" "${value}"
        else
            whole_key=$(echo ${result} | cut -d',' -f 1)
            key=$(echo ${whole_key} | cut -d '"' -f 2)
            value=$(curl -s exareme-keystore:8500/v1/kv/${key}?raw)
            check "${key}" "${value}"

            n=1
            while true
            do
                n=$((${n} + 1))
                whole_key=$(echo ${result} | cut -d',' -f ${n})
                if [[ -z ${whole_key} ]]; then
                    break
                fi
                key=$(echo ${whole_key} | cut -d'"' -f 2)
                value=$(curl -s exareme-keystore:8500/v1/kv/${key}?raw)
                check "${key}" "${value}"
            done

        fi
    fi
}

sleep 20         #initial sleep for services to write their data in Consul key-value store

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
            done < backup.json
        else
            backup
        fi
    fi
    sleep 5
    backup
done



