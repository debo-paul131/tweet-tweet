
/**
 * @author debojit
 */
import org.apache.spark._
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{ Seconds, StreamingContext }

object TweetTweetWordCount extends Logging {
  def main(args: Array[String]) {
    
    if (args.length < 4) {
      System.err.println("Usage: <consumer key> <consumer secret> <access token> <access token secret>")
      System.exit(1)
    }

    val configuration = new Configuration(args)
    val sc = configuration.sc

    val ssc = configuration.ssc
    ssc.checkpoint("streamingCheckPoint")

    println("Twitter streaming initialized")
    val stream = TwitterUtils.createStream(ssc, None)

    def updateCount(values: Seq[Int], state: Option[Int]) = {
      Some(values.sum + state.getOrElse(0))
    }

    val words = stream.flatMap { tw =>
      val afterCleanUp = Utils.cleanUp(tw.getText)
      val renovedStopWord = Utils.removeStopWords(afterCleanUp)
      renovedStopWord.split(" ")
    }

    val wordPairs = words.map(x => (x, 1))
    val wordCount = wordPairs.reduceByKey(_ + _)
    val stateDstream = wordCount.updateStateByKey(updateCount _)

    stateDstream.foreachRDD(rdd => {
      rdd.foreach { case (word, count) => println(s"""$word : $count""") }
    })

    ssc.start()
    ssc.awaitTermination()
    sc.stop()
  }
}