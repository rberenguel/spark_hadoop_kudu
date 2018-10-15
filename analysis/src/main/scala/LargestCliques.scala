package analysis

import scala.reflect.ClassTag

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.sql.functions.{desc, explode, split, trim}
import org.graphframes._

case class UserAndFriends(userId: String, friends: Seq[String])

case class Cliques(component: String, count: Long)

case class LargestCliques(ds: Dataset[UserAndFriends], config: CLIConfig)(implicit spark: SparkSession, tag: ClassTag[UserAndFriends]){
  import spark.implicits._

  private val appId = spark.sparkContext.applicationId

  private lazy val vertices: DataFrame = {
    ds.select($"userId").toDF("id")
      .union(edges.select($"dst".as("id")))
      .distinct
      .cache
  }

  private lazy val edges: DataFrame = {
    ds.withColumn("friend", explode($"friends"))
      .where($"friend" =!= "None")
      .select($"userId".as("src"), trim($"friend").as("dst"))
      .distinct
      .cache
  }

  private def getGraph: GraphFrame = {
    GraphFrame(vertices, edges)
  }

  private def connectedComponents: DataFrame = {
    spark.sparkContext.setCheckpointDir(s"${config.checkpointPath()}-$appId")
    getGraph.connectedComponents.run.select($"id", $"component")
  }

  def compute: Dataset[Cliques] = {
    connectedComponents.groupBy($"component").count.orderBy(desc("count")).as[Cliques]
  }
}

object LargestCliques {
  def apply(df: DataFrame, config: CLIConfig)(implicit spark: SparkSession): LargestCliques = {
    import spark.implicits._
    val projected = df.select($"user_id".as("userId"), split($"friends", ",").as("friends")).as[UserAndFriends]
    LargestCliques(projected, config)
  }
}
