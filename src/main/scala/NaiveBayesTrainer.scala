

/**
 * @author debojit
 */

import org.apache.spark._
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.classification.{ NaiveBayes, NaiveBayesModel }
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector

object NaiveBayesTrainer extends Logging {

  def main(args: Array[String]) {

    val configuration = new Configuration(args)
    val sc = configuration.sc

    val preparedData = sc.textFile("/Users/debojit/Downloads/smsspamcollection/SMSSpamCollection")
      .map(_.toLowerCase.split("\\s+"))
      .map(words => (words.head, words.tail.mkString(" ")))

    val data = preparedData.map {
      case (label, tweet) =>

        val lbl = label match {
          case "Buying"     => 1
          case "Suggestion" => 2
          case "Advice"     => 3
          case "Help"       => 4
          case _            => 0
        }

        LabeledPoint(lbl, Utils.featurize(tweet))
    }

    val splits: Array[RDD[LabeledPoint]] = data.randomSplit(Array(0.6, 0.4))
    val trainingData: RDD[LabeledPoint] = splits(0).cache()
    val testData: RDD[LabeledPoint] = splits(1)

    val model = NaiveBayes.train(trainingData)
    val predictionAndLabel = testData.map { test =>
      (model.predict(test.features), test.label)
    }
    model.save(sc, "data/intentsModel")
    val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / testData.count()
    println("accuracy: " + accuracy)

    sc.stop()

  }
}
