name := "tweet-tweet"

version := "1.0"

mainClass := Some("com.tweet.TweetTweet")

fork in (IntegrationTest, run) := true

scalaVersion := "2.10.2"
ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

assemblyMergeStrategy in assembly := {
   case PathList("META-INF", xs @ _*) => MergeStrategy.discard
   case x => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.0",
  "org.apache.spark" %% "spark-sql" % "1.6.0",
  "org.apache.spark" % "spark-mllib_2.10" % "1.6.0",
  "org.apache.spark" % "spark-streaming_2.10" % "1.6.0",
  "org.apache.spark" % "spark-streaming-twitter_2.10" % "1.6.0",
  "mysql" % "mysql-connector-java" % "5.1.34",
  "org.apache.commons" % "commons-lang3" % "3.3.2",
  "org.twitter4j" % "twitter4j-core" % "4.0.4" ,
  "com.github.nscala-time" %% "nscala-time" % "0.6.0",
  "com.datastax.spark" % "spark-cassandra-connector_2.10" % "1.6.0",
  "com.typesafe.play" %% "play-ws" % "2.4.0-M2",
  "com.typesafe.play" %% "play-json" % "2.4.0-M2"
)
