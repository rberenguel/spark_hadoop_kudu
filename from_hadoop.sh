#!/bin/sh

DOCKER_NETWORK=spark_hadoop_kudu_default
ENV_FILE=hadoop.env

docker run --network ${DOCKER_NETWORK} -v `pwd`/output:/hadoop-data/ --env-file ${ENV_FILE} hadoop-base hdfs dfs -copyToLocal /output/cliques.csv /hadoop-data/cliques.csv

docker run --network ${DOCKER_NETWORK} -v `pwd`/output:/hadoop-data/ --env-file ${ENV_FILE} hadoop-base hdfs dfs -rm -r /output/cliques.csv

docker run --network ${DOCKER_NETWORK} -v `pwd`/output:/hadoop-data/ --env-file ${ENV_FILE} hadoop-base hdfs dfs -copyToLocal /output/harbingers.csv /hadoop-data/harbingers.csv

docker run --network ${DOCKER_NETWORK} -v `pwd`/output:/hadoop-data/ --env-file ${ENV_FILE} hadoop-base hdfs dfs -rm -r /output/harbingers.csv
