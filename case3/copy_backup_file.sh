#!/usr/bin/env bash


docker-machine scp myMaster1:/home/myMaster/backup/backup.json .
docker-machine scp backup.json myMaster2:/home/myMaster/backup/
docker-machine scp backup.json myMaster3:/home/myMaster/backup/