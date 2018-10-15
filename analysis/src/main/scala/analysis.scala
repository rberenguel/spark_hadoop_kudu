package analysis

import collection.JavaConverters._

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions.split

import org.apache.kudu.spark.kudu._
import org.apache.kudu.client._

object Analysis {
  def main(args: Array[String]): Unit = {
    val config  = CLIConfig(args)
    implicit val spark    = SparkSession.builder.appName("yelp_analysis").getOrCreate()

    import spark.implicits._
    if(config.loadOnly()) {
      val reviews = spark.read.json(s"${config.inputPath()}/review.json")
        .select("stars", "business_id", "user_id", "review_id")
      val reviewSchema = StructType(Seq(StructField("stars",LongType,false),
                                        StructField("review_id",StringType,false),
                                        StructField("user_id",StringType,false),
                                        StructField("business_id",StringType,false)))
      val businesses = spark.read.json(s"${config.inputPath()}/business.json")
        .select("is_open", "business_id", "name")
      val businessSchema = StructType(Seq(StructField("is_open",LongType,false),
                                      StructField("business_id",StringType,false),
                                      StructField("name",StringType,true)))

      val kuduContext = new KuduContext("kudu-all-in-one:7051", spark.sparkContext)
      if(kuduContext.tableExists("businesses_table")){
        kuduContext.deleteTable("businesses_table")
      }
      kuduContext.createTable("businesses_table", businessSchema, Seq("business_id"),
                              new CreateTableOptions().setNumReplicas(1)
                                .addHashPartitions(List("business_id").asJava, 3))
      if(kuduContext.tableExists("reviews_table")){
        kuduContext.deleteTable("reviews_table")
      }
      kuduContext.createTable("reviews_table", reviewSchema, Seq("review_id"),
                              new CreateTableOptions().setNumReplicas(1)
                                .addHashPartitions(List("review_id").asJava, 3))

      kuduContext.insertRows(businesses, "businesses_table")
      kuduContext.insertRows(reviews, "reviews_table")
    } else {
      if(config.clique()){
        val data = spark.read.json(s"${config.inputPath()}/users.json")
        val cliques = LargestCliques(data, config).compute
        cliques.write.csv(s"${config.outputPath()}/cliques.csv")
      }

      if(config.harbinger()){
        val harbingers = HarbingersOfClosure(config).compute
        harbingers.show
        harbingers.write.csv(s"${config.outputPath()}/harbingers.csv")
      }
    }
    spark.close
  }
}
