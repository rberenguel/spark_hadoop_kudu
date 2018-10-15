import sbt.complete._
import complete.DefaultParsers._

name := "yelp_analysis"

version := "0.1"

scalaVersion in ThisBuild := "2.11.8"

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += Resolver.sonatypeRepo("releases")
resolvers += "bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven/"

val libVersions = Map(
  "spark"         -> "2.3.2",
  "scalatest"     -> "3.0.1",
  "spark-testing" -> "2.3.0_0.9.0",
  "scallop"       -> "3.1.2",
  "spark-graphx"  -> "2.1.0",
  "graphframes"   -> "0.5.0-spark2.1-s_2.11",
  "kudu" -> "1.7.1"
)


libraryDependencies ++= Seq(
  "org.apache.spark"      %% "spark-core"         % libVersions("spark") % "provided",
  "org.apache.spark"      %% "spark-sql"          % libVersions("spark") % "provided",
  "org.apache.spark" %% "spark-graphx"       % libVersions("spark-graphx") % "provided",
  "graphframes"      % "graphframes"         % libVersions("graphframes"),
  "org.apache.kudu" %% "kudu-spark2" % libVersions("kudu"),
  "org.scalatest"         % "scalatest_2.11"      % libVersions("scalatest") % "test",
  "com.holdenkarau"       %% "spark-testing-base" % libVersions("spark-testing") % "test",
  "org.apache.spark"      %% "spark-hive"         % libVersions("spark") % "test",
  "org.rogach"            %% "scallop"            % libVersions("scallop")
)

fork in Test := true
parallelExecution in Test := false
logBuffered in Test := false
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

lazy val deployS3 =
  inputKey[Unit]("Deploy the jar to S3")

val snapshot: Parser[String]       = " snapshot"
val alpha: Parser[String]          = " alpha"
val production: Parser[String]     = " production"
val combinedParser: Parser[String] = snapshot | alpha | production

deployS3 := {
  val destination = "af-artifacts"
  import sys.process._
  val majorVersion = scalaVersion.value
    .slice(-1, 4)
  val environment = combinedParser.parsed.trim
  streams.value.log.info(s"About to upload $environment version to $destination")
  val outputJarSuffix = environment match {
    case "snapshot"   => "-SNAPSHOT"
    case "alpha"      => "-ALPHA"
    case "production" => ""
  }

  val commands = List(
    s"aws s3 cp target/scala-$majorVersion/${name.value}.jar s3://$destination/spark/${name.value}$outputJarSuffix.jar",
    s"aws s3 cp src/main/resources/log4j.properties s3://$destination/spark/${name.value}$outputJarSuffix.properties",
    s"aws s3 cp scripts/${name.value}.sh s3://$destination/spark/${name.value}.sh"
  )
  commands.map(command => command !)
}
