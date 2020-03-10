package com.demo.spark;

import java.util.Arrays;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.demo.fact.Significance;
import com.demo.fact.Word;
import com.demo.fact.WordBuilder;

import scala.Tuple2;

class App {
	public static final String APP_NAME = "word-count-drools";

	public static void main(String[] args) {
		SparkSession sparkSession = SparkSession.builder().appName(APP_NAME).master("local").getOrCreate();
		Dataset<Word> countWords = countWords("input.txt", sparkSession);
		Dataset<Word> wordDS = executeRules(countWords, sparkSession);
		wordDS.show();
	}

	private static Dataset<Word> executeRules(Dataset<Word> countWords, SparkSession sparkSession) {
		KieContainer kc = KieServices.Factory.get().getKieClasspathContainer();
		JavaSparkContext javaSparkContext = JavaSparkContext.fromSparkContext(sparkSession.sparkContext());

		KieSession kieSession = kc.newKieSession("ksession-rule");
		KieRuntimeLogger logger = KieServices.Factory.get().getLoggers().newThreadedFileLogger(kieSession, "test",
				1000);
		Broadcast<KieSession> rulesBroadcasted = javaSparkContext.broadcast(kieSession);

		JavaRDD<Word> rulesFired = countWords.javaRDD().map(w -> fireAllRules(rulesBroadcasted.value(), w));
//		kieSession.dispose();
		logger.close();
		return sparkSession.sqlContext().createDataset(rulesFired.rdd(), Encoders.bean(Word.class));

	}

	public static Word fireAllRules(KieSession kieSession, Word word) {
		kieSession.insert(word);
		kieSession.fireAllRules();
		return word;
	}

	private static Dataset<Word> countWords(String fileName, SparkSession sparkSession) {
		JavaRDD<String> textRDD = sparkSession.read().textFile(fileName).toJavaRDD();
		JavaRDD<String> textSplittedRDD = textRDD.flatMap(t -> Arrays.asList(t.split(" ")).iterator());
		JavaPairRDD<String, Integer> textPairRDD = textSplittedRDD.mapToPair(w -> new Tuple2<String, Integer>(w, 1));
		JavaPairRDD<String, Integer> wordCountRDD = textPairRDD.reduceByKey((c1, c2) -> c1 + c2);
		JavaRDD<Word> wordRDD = wordCountRDD
				.map(t -> new WordBuilder().word(t._1).occurence(t._2).level(Significance.NONE).build());
		Dataset<Word> wordDS = sparkSession.createDataset(wordRDD.rdd(), Encoders.bean(Word.class));
		return wordDS;
	}

}
