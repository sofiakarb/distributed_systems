#!/usr/bin/env bash

check () {

    #Check if key-value were already added in the file. If so, do nothing.

    echo -e "\nGoing to check.."${1}" "${2}
    while read -r line ; do
        key=$( echo "$line" | cut -d'=' -f 1)
        if [[ "${key}" == "${1}" ]]; then
            echo "Same key."
            file_value=$( echo "$line" | cut -d'=' -f 2)
            if [[ "${file_value}" == "${2}" ]]; then
                echo "Same value. Nothing to be done!!"
                return 1
            else
                echo "Different value. Changing value in backup file!!"
                sed -i -r '/'[\${key}]'/ s/'${file_value}'/'${2}'/' /home/backup/backup.json
                return 1
            fi
        fi
    done < /home/backup/backup.json
    echo "Adding a new pair of key-value in backup file."
    echo ${1}"="${2} >> /home/backup/backup.json
}

backup () {

    #Here we take all the keys-values from Consul and place them in a backup file

    result=$(curl -s exareme-keystore:8500/v1/kv/?keys)
    if [[ "${result}" == "[]" ]] || [[ "${result}" == "" ]]; then      #variable empty
        echo -e "\nResult=" ${result}" is empty"
        if [[ -s backup.json ]]; then       #file not empty....
            echo "Consul service restarted. Sync Consul key-value store with Backup file!!(within backup function)"
            while read r line ; do
                key=$( echo "$line" | cut -d'=' -f 1)
                file_value=$( echo "$line" | cut -d'=' -f 2)
                curl -s -X PUT -d @- exareme-keystore:8500/v1/kv/${key} <<< ${file_value}
            done
            echo "Consul key-value store synced!!(within backup function)"
            return 1
        else
            echo "Too soon maybe? sleep.."
            sleep 5
            return 1
        fi
    else
        echo -e "\nResult=" ${result}

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

sleep 10         #initial sleep for services to write their data in Consul key-value store

while true
do
    if [[ "$(curl -s exareme-keystore:8500/v1/health/state/passing | jq -r '.[].Status')" = "passing" ]];  then
        if [[ -s backup.json  ]]; then       #file not empty
            while read -r line ; do
                key=$( echo "$line" | cut -d'=' -f 1)
                file_value=$( echo "$line" | cut -d'=' -f 2)
                kv_value=$(curl -s exareme-keystore:8500/v1/kv/${key}?raw)

                if [[ -z ${kv_value} ]]; then      #Since backup.json file has key-value, but Consul key-value store seams to be empty, the service restarted..
                    echo "Consul service restarted. Sync Consul key-value store with Backup file.."
                    curl -s -X PUT -d @- exareme-keystore:8500/v1/kv/${key} <<< ${file_value}
                    echo "Consul key-value store synced!!"
                elif [[ "$kv_value" == "$file_value" ]]; then
                    echo "Backup file is already synced with Consul key-value store..Nothing to be synced!!"
                else
                    echo "Backup file not synced with Consul..Change backup file!!"
                    sed -i .bak '/'[\${key}]'/ s/'${file_value}'/'${kv_value}'/' /home/backup/backup.json
                    echo "Backup file synced!!"

                fi
            done < /home/backup/backup.json
        else                            #file empty
            backup
        fi
    fi
    sleep 5
    backup
done



