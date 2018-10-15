package analysis

import com.holdenkarau.spark.testing.{SharedSparkContext, DatasetSuiteBase}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.{Dataset, DataFrame}
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.functions.{max, split}
import org.graphframes.GraphFrame
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

import scala.language.reflectiveCalls

@RunWith(classOf[JUnitRunner])
class ConnectedComponentsSuite
    extends FlatSpec
    with Matchers
    with SharedSparkContext
    with DatasetSuiteBase
    with PrivateMethodTester {

  import spark.implicits._

  lazy val fixture =
    new {
      val userSource = getClass.getResource("/users.json").getPath
      import sqlContext.implicits._
      implicit val sparkSession = spark
      val config: CLIConfig = CLIConfig(Seq[String]("-c", "checkpoints"))
      val data = spark.read.json(userSource)
      val projection: Dataset[UserAndFriends] = data.select($"user_id".as("userId"), split($"friends", ", ").as("friends")).as[UserAndFriends]
    }

  behavior of "LargestCliques"

  it should "handle data with space after comma properly" in {
    val lc = LargestCliques(fixture.data, fixture.config)(spark)
    val edges = PrivateMethod[DataFrame]('edges)
    val lcEdges = lc invokePrivate edges()
    assert(lcEdges.select("dst").where($"dst" contains " ").count == 0)
  }

  it should "remove None as a user" in {
    val lc = LargestCliques(fixture.data, fixture.config)(spark)
    val edges = PrivateMethod[DataFrame]('edges)
    val lcEdges = lc invokePrivate edges()
    assert(lcEdges.select("dst").where($"dst" === "None").count == 0)
  }

  it should "return cliques" in {
    val cliques = LargestCliques(fixture.data, fixture.config)(spark).compute
    cliques.show
    assert(cliques.agg(max("count").cast(LongType)).as[Long].collect.head ==  3)
  }
}
