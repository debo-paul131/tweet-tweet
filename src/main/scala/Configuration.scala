/**
 * @author debojit
 */

import org.apache.spark._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.ws.ning._
import play.api.libs.ws._
import org.apache.spark.streaming.twitter.TwitterUtils

class Configuration(args: Array[String]) {

  //val (consumerKey, consumerSecret, accessToken, accessTokenSecret) = (args(0),args(1),args(2),args(3))

  def conf: SparkConf = new SparkConf()
    .setMaster("local[2]")
    .setAppName("tweet-tweet")
    .set("spark.executor.memory", "1g")
    .set("spark.driver.allowMultipleContexts", "true")
    .set("spark.logConf", "true")
    .set("spark.cassandra.connection.host", "127.0.0.1")
    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

  def sc: SparkContext = new SparkContext(conf)
  sc.setLogLevel("ERROR")

  def ssc: StreamingContext = new StreamingContext(conf, Seconds(5))

  System.setProperty("twitter4j.oauth.consumerKey", "u7wp5rQPLK29o68ImHNVhvwDj")
  System.setProperty("twitter4j.oauth.consumerSecret", "3Logg8C01yc90sVlwbnw3phAkeCRWPiLhMtzkPNCWzNHNsPCjy")
  System.setProperty("twitter4j.oauth.accessToken", "763019263015849984-m2sneXshUclxRaQJyz0yDvgg4vgKJuM")
  System.setProperty("twitter4j.oauth.accessTokenSecret", "NAdyJJFOdK4p8JZOzILz3lbhbm7uMPe3B31JSZb9HoioD")

}