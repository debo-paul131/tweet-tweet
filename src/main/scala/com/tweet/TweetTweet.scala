package com.tweet
/**
 * @author debojit
 */

import org.apache.spark._
import org.apache.spark.streaming.twitter.TwitterUtils
import com.datastax.spark.connector.streaming._
import com.datastax.spark.connector._
import org.apache.spark.Logging
import play.api.libs.json._
import scala.concurrent.Await
import twitter4j.GeoLocation
import twitter4j.Place
import scala.concurrent.duration._
import play.api.libs.ws.ning._
import play.api.libs.ws._
import java.net.URLEncoder


case class Extract(createdAt: String, geoInfo: String, text: String, stopWordRemovedText: String, intents: String, sentiment: String)

object TweetTweet extends Logging {
  def main(args: Array[String]) {

    if (args.length < 4) {
      System.err.println("Usage: <consumer key> <consumer secret> <access token> <access token secret>")
      System.exit(1)
    }

    val filePath = args.takeRight(args.length - 4)

    val (twitterExtractPath, stopWordFilePath) =
      if (filePath.length == 2) {
        (filePath(0), filePath(1))
      } else ("data/twitterExtracts" , "data/stop-word-list.txt")

    val configuration = new Configuration(args)

    val sc = configuration.sc
    val ssc = configuration.ssc

    println("Twitter streaming initialized")
    val stream = TwitterUtils.createStream(ssc, None)

    val stopWords = Utils.loadStopWords(stopWordFilePath)

    def removeStopWords(tweet: String): String = {
      tweet.split("\\s+")
        .filter(!stopWords.contains(_))
        .mkString(" ")
    }

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

    //val model = NaiveBayesModel.load(ssc.sparkContext, naiveBayesModelPath)

    val tweetsRDD = filterByLang.map { tw =>

      val cleanedText = Utils.cleanUp(tw.getText)
      val remeovedStopWords = removeStopWords(cleanedText)
      val geoInfo = getGeoInformation(tw.getPlace, tw.getGeoLocation)
      val intents = "UnKnown"
      val detectedSentiments = detectSentiment(cleanedText)
      Extract(tw.getCreatedAt.toInstant.toString, geoInfo, cleanedText, remeovedStopWords, intents, detectedSentiments)
    }

    tweetsRDD.foreachRDD(rdd => {
      rdd.saveAsTextFile(twitterExtractPath)
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

