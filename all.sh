#!/bin/sh

TAR_LOCATION=$1

make all-hadoop
make all-spark
make kudu
make local

docker-compose up -d

# Wait until the datanode is up to continue

until curl -fqs http://localhost:50075/ ; do
    sleep 1;
done;

./to_hadoop.sh $TAR_LOCATION

make clique
make load
make harbinger-kudu

./from_hadoop.sh

docker-compose down

make clean-spark
make clean-hadoop
