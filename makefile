NAME ?= unknown
ARGS ?= unknown

build:
	docker build -t $(NAME) -f $(NAME)/Dockerfile $(NAME)/

run-spark:
	docker run --network spark_hadoop_kudu_default -t spark-base spark/bin/spark-submit --master spark://spark-master:7077 --deploy-mode client  --packages org.apache.kudu:kudu-spark2_2.11:1.7.1,graphframes:graphframes:0.5.0-spark2.1-s_2.11 --conf spark.rdd.compress=true --class analysis.Analysis hdfs://namenode/input/yelp_analysis.jar $(ARGS)

jdk8:
	make build NAME=openjdk8

base: jdk8
	make build NAME=hadoop-base

namenode: base
	make build NAME=hadoop-namenode

datanode: base
	make build NAME=hadoop-datanode

all-hadoop: namenode datanode

clean-hadoop:
	docker stop hadoop-namenode hadoop-datanode || true
	docker rmi hadoop-namenode hadoop-datanode hadoop-base openjdk8 --force

spark: jdk8
	make build NAME=spark-base

master: spark
	make build NAME=spark-master

worker: spark
	make build NAME=spark-worker

all-spark: master worker

clean-spark:
	docker stop spark-master spark-worker || true
	docker rm spark-master spark-worker || true
	docker rmi spark-master spark-worker spark-base --force || true
	make clean-hadoop

nodemngr: base
	make build NAME=nodemanager

resourcemngr: base
	make build NAME=resourcemanager

historysrvr: base
	make build NAME=historyserver

all-yarn: nodemngr resourcemngr historysrvr

clean-yarn:

kudu:
	make build NAME=kudu-base

jar:
	make build NAME=analysis
	mkdir input || true
	docker run -v `pwd`/input:/root/target/scala-2.11/ -t analysis sbt "set test in assembly := {}" "assembly"

local:
	mkdir input || true
	cd analysis && sbt "set test in assembly := {}" "assembly"
	cp analysis/target/scala-2.11/yelp_analysis.jar input/yelp_analysis.jar

test:
	cd analysis && sbt test

load:
	make run-spark ARGS=--load-only

clique:
	make run-spark ARGS=--clique

harbinger:
	make run-spark ARGS=--harbinger

harbinger-kudu:
	make run-spark ARGS=--harbinger -k
