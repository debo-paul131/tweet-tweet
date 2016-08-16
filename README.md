# tweet-tweet
Twitter data analysis using Apache Spark

##Downloading, building

git clone https://github.com/debo-paul131/tweet-tweet.git

sbt compile

## Running

#Running from SBT

*cd tweet-tweet*

*sbt "run-main TweetTweet <consumer key> <consumer secret> <access token> <access token secret>"* <br/>

*sbt "run-main TweetTweetWordCount <consumer key> <consumer secret> <access token> <access token secret>"* <br/>

*sbt "run-main  NaiveBayesTrainer [Training dataset path]"*


#Running from Spark Submit

*sbt assembly*

Jar file will be location in **tweet-tweet/target/scala-2.10/tweet-tweet-assembly-1.0.jar** location

*cd SPARK_HOME*

*bin/spark-submit  --class TweetTweet /path-to-tweet-tweet/target/scala-2.10/tweet-tweet-assembly-1.0.jar* **[consumer key]** **[consumer secret]** **[access token]** **[access token secret]** **/path-to-tweet-tweet/data/twitterExtracts/**  **/path-to-tweet-tweet/data/stop-word-list.txt** <br/><br/>
*bin/spark-submit  --class TweetTweetWordCount /path-to-tweet-tweet/target/scala-2.10/tweet-tweet-assembly-1.0.jar* **[consumer key]** **[consumer secret]** **[access token]** **[access token secret]** **/path-to-tweet-tweet/data/stop-word-list.txt** <br/><br/>
*bin/spark-submit  --class NaiveBayesTrainer /path-to-tweet-tweet/target/scala-2.10/tweet-tweet-assembly-1.0.jar* **[Training dataset path]** **/path-to-tweet-tweet/data/intentsModel/** <br/>

## Build Eclipse project

sbt eclipse

