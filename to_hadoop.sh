#!/bin/sh

DOCKER_NETWORK=spark_hadoop_kudu_default
ENV_FILE=hadoop.env
TAR_LOCATION=$1
DIR_OF_TAR=$(dirname $TAR_LOCATION)

mkdir `pwd`/input || true

FILES=( yelp_academic_dataset_business.json yelp_academic_dataset_review.json yelp_academic_dataset_user.json )

for file in "${FILES[@]}"
do
    tar xvf $TAR_LOCATION $file
    mv `pwd`/$file `pwd`/input/$file
done

docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} hadoop-base hdfs dfs -mkdir -p /input/
docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} hadoop-base hdfs dfs -mkdir -p /output/
docker run --network ${DOCKER_NETWORK} -v `pwd`/input:/hadoop-data/ --env-file ${ENV_FILE} hadoop-base hdfs dfs -copyFromLocal -f /hadoop-data/yelp_academic_dataset_business.json /input/business.json
docker run --network ${DOCKER_NETWORK} -v `pwd`/input:/hadoop-data/ --env-file ${ENV_FILE} hadoop-base hdfs dfs -copyFromLocal -f /hadoop-data/yelp_academic_dataset_review.json /input/review.json
docker run --network ${DOCKER_NETWORK} -v `pwd`/input:/hadoop-data/ --env-file ${ENV_FILE} hadoop-base hdfs dfs -copyFromLocal -f /hadoop-data/yelp_academic_dataset_user.json /input/users.json

docker run --network ${DOCKER_NETWORK} -v `pwd`/input:/hadoop-data/ --env-file ${ENV_FILE} hadoop-base hdfs dfs -copyFromLocal -f /hadoop-data/yelp_analysis.jar /input/yelp_analysis.jar
