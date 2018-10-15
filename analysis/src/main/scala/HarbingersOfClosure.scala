package analysis

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.sql.functions.{desc, explode}
import org.apache.kudu.spark.kudu._
import org.apache.kudu.client._


case class HarbingersOfClosure(config:CLIConfig)(implicit spark: SparkSession){
  private def loadReviews(): DataFrame = if(config.useKudu()){
    spark.read.options(Map("kudu.master" -> "kudu-all-in-one:7051","kudu.table" -> "reviews_table")).kudu
  } else {
    spark.read.json(s"${config.inputPath()}/review.json")
  }

  private def loadBusinesses(): DataFrame = if(config.useKudu()){
    spark.read.options(Map("kudu.master" -> "kudu-all-in-one:7051","kudu.table" -> "businesses_table")).kudu
  } else {
    spark.read.json(s"${config.inputPath()}/business.json")
  }

  def compute(): DataFrame = {
    import spark.implicits._
    val reviews = loadReviews()
    val businesses = loadBusinesses()
    val badReviews = reviews.where($"stars" < 3).select("business_id", "user_id").distinct
    val closed = businesses.where($"is_open" === 0).select("business_id", "name")
    val closers = closed.as("closed").join(badReviews.as("reviews"), $"closed.business_id" === $"reviews.business_id")
    closers.groupBy("user_id").count.orderBy(desc("count"))
  }
}
