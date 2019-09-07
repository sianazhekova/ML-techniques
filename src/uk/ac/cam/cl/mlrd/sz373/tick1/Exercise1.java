package uk.ac.cam.cl.mlrd.sz373.tick1;

import java.nio.file.Path;
import java.io.*;

import java.net.*;
import java.util.*;/*
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;  */

public class Exercise1 implements IExercise1 {

	@Override
	public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {

		
		BufferedReader b = new BufferedReader(new FileReader(lexiconFile.toString()));
		String line;
		Map<String, Sentiment> wordSen = new HashMap<>();
		Map<Path, Sentiment> reviews = new HashMap<>();
		while ( (line = b.readLine()) != null) {
		  String[] tempa =line.split(" ");
		  String[] w = tempa[0].split("=");
		  String word = w[1];
		  String[] p_acc = tempa[2].split("=");
		  //System.out.println(p_acc[1]);
		  if (p_acc[1].equals("positive")) {
			  wordSen.put(word ,Sentiment.POSITIVE);
		  }
		  if (p_acc[1].equals("negative")) {
			  wordSen.put(word, Sentiment.NEGATIVE);
		  }
		}
		for(Path p : testSet) {
			List<String> wordsperfile =Tokenizer.tokenize(p);
			int pos=0,neg = 0;
			for (String word : wordsperfile) {
				if (wordSen.containsKey(word)) {
					if(wordSen.get(word) == Sentiment.POSITIVE) {
						pos++;
					}
					if(wordSen.get(word) == Sentiment.NEGATIVE) {neg++;}
			if (pos>=neg) {
			reviews.put(p, Sentiment.POSITIVE);
			}
			else {
			reviews.put(p, Sentiment.NEGATIVE);
			}
				}
			}
		}
		return reviews;  

	}
	
	/**
	 * Calculate the proportion of predicted sentiments that were correct.
	 * 
	 * @param trueSentiments
	 *            {@link Map}<{@link Path}, {@link Sentiment}> Map of correct
	 *            sentiment for each review
	 * @param predictedSentiments
	 *            {@link Map}<{@link Path}, {@link Sentiment}> Map of calculated
	 *            sentiment for each review
	 * @return <code>double</code> The overall accuracy of the predictions
	 */

	@Override
	public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments) {
		double A;
		int c=0, all=0;
		for (Map.Entry<Path, Sentiment> PSpre: predictedSentiments.entrySet()) {
			//for (Map.Entry<Path, Sentiment> PStru: trueSentiments.entrySet() ) {
			if (PSpre.getValue() == trueSentiments.get(PSpre.getKey())) {
				c++;
			}
			all++;
		}
		System.out.println(all);
		System.out.println(c);
		A = c*1.0/all;
		return A;
	}

	@Override
	public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {

		BufferedReader b = new BufferedReader(new FileReader(lexiconFile.toString()));
		String line;
		Map<String, Sentiment> wordSen = new HashMap<>();
		Map<Path, Sentiment> reviews = new HashMap<>();
		Map<Path, Map<String, Sentiment>> another = new HashMap<>();
		
		while ( (line = b.readLine()) != null) {
		  String[] tempa =line.split(" ");
		  String[] w = tempa[0].split("=");
		  String word = w[1];
		  String[] p_acc = tempa[2].split("=");
		  //System.out.println(p_acc[1]);
		  
		  if (p_acc[1].equals("positive")) {
			  wordSen.put(word ,Sentiment.POSITIVE);
		  }
		  if (p_acc[1].equals("negative")) {
			  wordSen.put(word, Sentiment.NEGATIVE);
		  }
		}
		for(Path p : testSet) {
			List<String> wordsperfile =Tokenizer.tokenize(p);
			int pos=0,neg = 0;
			for (String word : wordsperfile) {
				if (wordSen.containsKey(word)) {
					if(wordSen.get(word) == Sentiment.POSITIVE) {
						pos++;
					}
					if(wordSen.get(word) == Sentiment.NEGATIVE) {neg++;}
			if (pos-neg>=9) {
			reviews.put(p, Sentiment.POSITIVE);
			}
		    else {
			reviews.put(p, Sentiment.NEGATIVE);
			}
			}
			}
		}
		return reviews;  
		
	}
}
