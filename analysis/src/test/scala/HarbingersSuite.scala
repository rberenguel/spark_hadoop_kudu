package analysis

import com.holdenkarau.spark.testing.{SharedSparkContext, DatasetSuiteBase}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.functions.max
import org.graphframes.GraphFrame
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

import scala.language.reflectiveCalls

@RunWith(classOf[JUnitRunner])
class HarbingersSuite
    extends FlatSpec
    with Matchers
    with SharedSparkContext
    with DatasetSuiteBase
    with PrivateMethodTester {

  lazy val fixture =
    new {
      val path = getClass.getResource("/").getPath
      import sqlContext.implicits._
      implicit val sparkSession = spark
      val config = CLIConfig(Seq[String]("-i", path, "-o", path))
    }

  behavior of "HarbingersOfClosure"

  it should "return most negative user" in {
    import spark.implicits._
    implicit val sparkSession = spark
    val harbingers = HarbingersOfClosure(fixture.config).compute
    harbingers.show
    assert(harbingers.as[Tuple2[String, Long]].head._1 == "Ha3iJu77CxlrFm-vQRs_8g")
  }
}
