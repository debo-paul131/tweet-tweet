

/**
 * @author debojit
 */
import java.io.InputStream
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector

object Utils {

  def cleanUp(tweet: String): String = tweet.split("https")(0).trim().replaceAll("[^a-zA-Z\\s]", "").trim().toLowerCase();

  def loadStopWords(path: String): Set[String] = {
    import java.io.File
    import scala.io.Source
    Source.fromFile(new File(path)).getLines().toSet
  }


  val hashingTF = new HashingTF()

  def featurize(tweet: String): Vector = hashingTF.transform(tweet)

}