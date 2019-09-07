package uk.ac.cam.cl.mlrd.sz373.tick4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.Math;
import uk.ac.cam.cl.mlrd.sz373.tick1.*;
/*
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;
import uk.ac.cam.cl.mlrd.utils.DataSplit; */

public class Exercise4 implements IExercise4{
	/**
	 * Modify the simple classifier from Exercise1 to include the information about the magnitude of a sentiment.
	 * @param testSet
	 *            {@link Set}<{@link Path}> Paths to reviews to classify
	 * @param lexiconFile
	 *            {@link Path} Path to the lexicon file
	 * @return {@link Map}<{@link Path}, {@link Sentiment}> Map of calculated
	 *         sentiment for each review
	 * @throws IOException 
	 */
	@Override
	public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
		// Improve simple classifier to include word polarity	
		BufferedReader b = new BufferedReader(new FileReader(lexiconFile.toString()));
		String line;
		Map<String, Map<Sentiment, Integer>> wordSenWeightMap = new HashMap<>();
		Map<Path, Sentiment> reviews = new HashMap<>();
		while ( (line = b.readLine()) != null) {
     	String[] tempa =line.split(" ");
     	String pol = tempa[1].split("=")[1];
	    String[] w = tempa[0].split("=");
	    String word = w[1];
	    String[] p_acc = tempa[2].split("=");
	    //System.out.println(p_acc[1]);
	    Map<Sentiment, Integer> SenWeight = new HashMap<>();
	    int val = 1;
	    if (pol.equals("strong")) {
	    	val = 2;
	    }
		if (p_acc[1].equals("positive")) {
			SenWeight.put(Sentiment.POSITIVE, val);
			}
	    if (p_acc[1].equals("negative")) {
	    	SenWeight.put(Sentiment.NEGATIVE, val);
	    	}
	    wordSenWeightMap.put(word, SenWeight);
	    }
		for(Path p : testSet) {
			List<String> wordsperfile =Tokenizer.tokenize(p);
			int pos=0,neg = 0;
			for (String word : wordsperfile) {
				if (wordSenWeightMap.containsKey(word)) {
					Map<Sentiment, Integer> tempMap = wordSenWeightMap.get(word);
					if(tempMap.containsKey(Sentiment.POSITIVE)) {
						int pWeight = tempMap.get(Sentiment.POSITIVE);
						pos= pos + pWeight;
						}
					if (tempMap.containsKey(Sentiment.NEGATIVE)){
						int nWeight = tempMap.get(Sentiment.NEGATIVE);
						neg += nWeight;
						}
				}
			}
			if (pos-neg>=0) {
				reviews.put(p, Sentiment.POSITIVE);	
				}
			else {
				reviews.put(p, Sentiment.NEGATIVE);
				}
		}
		
		return reviews;  
		}

	/**
	 * Implement the two-sided sign test algorithm to determine if one
	 * classifier is significantly better or worse than another.
	 * The sign for a result should be determined by which
	 * classifier is more correct, or if they are equally correct should be 0.5
	 * positive, 0.5 negative and the ceiling of the least common sign total
	 * should be used to calculate the probability.
	 * 
	 * @param actualSentiments
	 *            {@link Map}<{@link Path}, {@link Sentiment}>
	 * @param classificationA
	 *            {@link Map}<{@link Path}, {@link Sentiment}>
	 * @param classificationB
	 *            {@link Map}<{@link Path}, {@link Sentiment}>
	 * @return <code>double</code>
	 */
	
	@Override
	public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA,
			Map<Path, Sentiment> classificationB) {
		//
		//BigInteger pValue = new BigInteger(0.0);
		int Null = 0;
		int Plus = 0;
		int Minus = 0;
		for (Path path : actualSentiments.keySet()) {
			if (classificationA.get(path)==classificationB.get(path)) {
				Null++;
			}
			if (classificationA.get(path)==actualSentiments.get(path)) {
				Plus++;
			}
			if (classificationB.get(path) == actualSentiments.get(path)) {
				Minus++;
			}
		}
		int k,min, n;
		if (Plus < Minus) {
			min = Plus;
		}
		else {
			min = Minus;
		}
		double val = (double)Null/2;
		int ceiling = (int)Math.ceil(val);
		k = ceiling + min;
		n = 2*ceiling + Plus + Minus;
	    BigDecimal pValue = new BigDecimal(0);
	    for (int i =0; i<=k; i++) {
	    	BigDecimal Pi = new BigDecimal(1);
	    	BigInteger fact = new BigInteger("1");
	    	BigDecimal combo = new BigDecimal(factorial(n).divide(factorial(i).multiply(factorial(n-i))));
	    	System.out.println(fact.doubleValue());
	    	//System.out.println((long)Math.pow(0.5, n));
	    	BigDecimal t = new BigDecimal(Math.pow(0.5, n));
	    	
	    	//Pi = Pi.multiply(fact.multiply(new BigInteger((long) Math.pow(0.5, n))));
	    	Pi = Pi.multiply(combo.multiply(t));
	    	System.out.println(Pi);
	    	pValue = pValue.add(Pi);
	    }
		System.out.println(2.0*pValue.doubleValue());
		return (2.0*pValue.doubleValue());
	}
	public BigInteger factorial(int N) {
    	BigInteger f = new BigInteger("1");
        // Multiply f with 2, 3, ...N 
        for (int i = 2; i <= N; i++) { 
        	f = f.multiply(BigInteger.valueOf(i));
        }
        return f;
        } 

}
