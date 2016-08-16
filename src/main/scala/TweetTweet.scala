
import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import scala.collection.immutable.HashMap
import com.datastax.spark.connector.streaming._
import com.datastax.spark.connector._
import org.apache.spark.Logging
import play.api.libs.json._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.util.{ Success, Failure }
import scala.concurrent.ExecutionContext.Implicits.global
import twitter4j.GeoLocation
import twitter4j.Place
import scala.concurrent.duration._
import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.ws.ning._
import play.api.libs.ws._
import java.net.URLEncoder
import org.apache.log4j.{ Level, Logger }
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.classification.{ NaiveBayes, NaiveBayesModel }

case class Extract(createdAt: String, geoInfo: String, text: String, stopWordRemovedText: String, intents: Double, sentiment: String)

object TweetTweet extends Logging {
  def main(args: Array[String]) {

    //if (args.length < 4) {
    //  System.err.println("Usage: TwitterPopularTags <consumer key> <consumer secret> " +
    //    "<access token> <access token secret>")
    //  System.exit(1)
    // }

    val configuration = new Configuration(args)

    val sc = configuration.sc
    val ssc = configuration.ssc

    println("Twitter streaming initialized")
    val stream = TwitterUtils.createStream(ssc, None)

  
    def detectSentiment(tweet: String): String = {

      val config = new NingAsyncHttpClientConfigBuilder(DefaultWSClientConfig()).build
      val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder(config)
      val httpClient = new NingWSClient(builder.build)

      val url = "http://www.sentiment140.com/api/classify?appid=bob@apple.com&text=" + URLEncoder.encode(tweet, "UTF-8")

      val holder = httpClient.url(url).get
      val result = Await.result(holder, 20.seconds)
      val polatity = result.json.\("results").\("polarity").as[Int]

      httpClient.close()
      polatity match {
        case 0 => "negative"
        case 2 => "neutral"
        case 4 => "positive"
      }

    }

    def getGeoInformation(place: Place, geo: GeoLocation): String = {

      val placeName = try {
        place.getFullName
      } catch {
        case e: Exception => "PLACE NAME NOT FOUND"
      }
      val country = try {
        place.getCountry
      } catch {
        case e: Exception => "COUNTRY NAME NOT FOUND"
      }

      val latlon = try {
        s"${geo.getLatitude},${geo.getLongitude}"
      } catch {
        case e: Exception => "COORDINATES NOT FOUND"
      }

      s""" 
             Place   : $placeName 
             Country : $country 
             latlon  : $latlon
         """
    }

    val filterByLang = stream.filter(_.getLang == "en")

    val model = NaiveBayesModel.load(ssc.sparkContext, "data/intentsModel")

    val tweetsRDD = filterByLang.map { tw =>

      val cleanedText = Utils.cleanUp(tw.getText)
      val remeovedStopWords = Utils.removeStopWords(cleanedText)
      val geoInfo = getGeoInformation(tw.getPlace, tw.getGeoLocation)
      val intents = model.predict(Utils.featurize(cleanedText))
      val detectedSentiments = detectSentiment(cleanedText)
      Extract(tw.getCreatedAt.toInstant.toString, geoInfo, cleanedText, remeovedStopWords, intents, detectedSentiments)
    }

    tweetsRDD.foreachRDD(rdd => {
      rdd.saveAsTextFile("data/twitterExtracts")
      rdd.foreach {
        case (data) =>
          println(
            s"""
          Date            : %s
          Text            : %s
          GeoInfo         : %s
          StopWordRemoved : %s
          Intents         : %s
          sentiment       : %s
          """.format(data.createdAt, data.text, data.geoInfo, data.stopWordRemovedText, data.intents, data.sentiment))
      }
    })

    ssc.start()
    ssc.awaitTermination()
    sc.stop()
  }

}

