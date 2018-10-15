package analysis

import org.rogach.scallop._

case class CLIConfig(arguments: Seq[String])
    extends ScallopConf(arguments)
    with Serialization {

  private val HADOOP = "hdfs://namenode"

  val useKudu = opt[Boolean](default = Some(false),
                              required = true,
                              descr = "Use the data from Apache Kudu (if false, use the data from HDFS)",
                              short = 'k')

  val loadOnly = opt[Boolean](default = Some(false),
                         required = true,
                         descr = "Execute only the load to Kudu",
                              short = 'l')

  val clique = opt[Boolean](default = Some(false),
      required = true,
      descr = "Find the largest user clique and store it to hdfs://output")

  val harbinger = opt[Boolean](default = Some(false),
                            required = true,
                            descr = "Find the users more likely to crash a business with a negative review and store it to hdfs://output")


  val inputPath = opt[String](default = Some(s"$HADOOP/input"),
                         required = true,
                         descr = "HDFS path to load the data",
                         short = 'i')

  val outputPath = opt[String](default = Some(s"$HADOOP/output"),
                              required = true,
                              descr = "HDFS path to store the data",
                              short = 'o')


  val checkpointPath = opt[String](default = Some("hdfs://namenode/checkpointPath"),
                                   required=true,
                                   descr = "Checkpointing path (required for connected components)", short = 'c')
  verify()
}
