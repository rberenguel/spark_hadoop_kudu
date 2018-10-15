# Fun with hadoop+spark(+kudu) with a dash of Docker

This is a dockerised setup of [Apache Spark](http://spark.apache.org) (master
and worker in standalone mode in separate containers) on top of [Apache
Hadoop](http://hadoop.apache.org), with a single node [Apache
Kudu](https://kudu.apache.org) for a "why not".

The Scala project under `analysis/` can be used to load some data into `kudu`,
run a clique analysis based on connected components (using
[Graphframes](http://graphframes.github.io)) or run some simple analysis off
`hadoop` or based on the data in `kudu`.

The data used is based on the [Yelp dataset](http://yelp.com/dataset/challenge).
For local running over Docker, refrain from using the full dataset, and if you
really, really want to, at least comment out the clique computation. Connected
components scales well, but takes a while.

I had always thought `make` could be an option to build Docker images, decided
to give it a try here since I had many images to build. Can definitely recommend
it for some use cases.

Note you will need Docker with `docker-compose` accepting at least schema 2. In
case of doubt, update Docker.

## Running this

Assuming you have the yelp dataset available (or a reduced version of it) just run

```bash
./all.sh PATH_TO_DATASET.tar
```

This will:

1. Build Hadoop namenode and datanode Docker images (based on `openjdk:8`)
2. Build Spark worker and master Docker images (likewise)
3. Build a Kudu image (can be used for any of the Kudu components)
4. Run a local (you will need Scala and `sbt`) build of the Spark project. If you don't have `sbt`, you can change this for `make jar` which will do likewise but inside a Docker container
5. Use Docker compose to set up all the containers above
6. Once the Hadoop datanode is up, upload the files required from the TAR file into HDFS
7. Run the largest clique computation, stores output to HDFS
8. Load some HDFS data into Kudu
9. Run some analysis using the Kudu tables, stores output to HDFS
10. Copy the outputs from HDFS to "local" (volume mapped to user instance)
11. Shut down the system and clean up most of the Docker images

Output will be available in `output/`

# Notes

Sorry about having to use `openjdk:8` instead of the `alpine` image (which is
~300MB smaller). Hadoop needs `glibc` and alpine doesn't have it, the datanode
will segfault with alpine.

Could be even more fun to set the system up with the addition of Yarn, running
Spark on top of Yarn. The images are available to be built based on the same
`Makefile`, and they can be linked easily. But that's a bit too many JVMs on
Docker for my laptop.

# References

Spark, Hadoop and Yarn Docker setups based on
[docker-spark-hadoop](https://github.com/big-data-europe/docker-hadoop-spark-workbench)
and [docker-hadoop](https://github.com/big-data-europe/docker-hadoop) from Big
Data Europe.

Kudu Docker setup from [kunickiaj/kudu-docker](https://github.com/kunickiaj/kudu-docker)
