package uk.ac.cam.cl.mlrd.sz373.tick5;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import java.util.*;
import java.util.LinkedList;
import java.util.Random;

import uk.ac.cam.cl.mlrd.sz373.tick1.*;
import uk.ac.cam.cl.mlrd.sz373.tick2.*;
import uk.ac.cam.cl.mlrd.sz373.tick4.*;  /*

import uk.ac.cam.cl.mlrd.sz373.tick1.Exercise1;
import uk.ac.cam.cl.mlrd.sz373.tick2.Exercise2;
import uk.ac.cam.cl.mlrd.sz373.tick5.Exercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;  */

public class Exercise5 implements IExercise5 {
	
	/**
	 * Split the given data randomly into 10 folds.
	 * 
	 * @param dataSet
	 *            {@link Map}<{@link Path}, {@link Sentiment}> All review paths
	 * 
	 * @param seed
	 *            A seed for the random shuffling.
	 * @return {@link List}<{@link Map}<{@link Path}, {@link Sentiment}>> A set
	 *         of folds with even numbers of each sentiment
	 */
	
	@Override
	public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {
		// Do the above ^^^
		Map<Path,Sentiment> outputMap = shuffleList(dataSet,seed);
		assert(outputMap.size() == dataSet.size());
		List<Map<Path, Sentiment>> outputList = new LinkedList<Map<Path, Sentiment>>();
		Map<Path, Sentiment> tempMap = new HashMap<Path,Sentiment>();
		int binSize = outputMap.size()/10;
		for (Map.Entry<Path, Sentiment> element : outputMap.entrySet()) {
			
			if (tempMap.keySet().size() < binSize) {
				tempMap.put(element.getKey(), element.getValue());
			}
			if (tempMap.keySet().size()== binSize) {
				outputList.add(tempMap);
				System.out.println(tempMap.keySet().size()+" <- Number of keys ||| Number of entries -> "+tempMap.size());
				tempMap = new HashMap<Path, Sentiment>();
				System.out.println(tempMap.size());
			}
		}
		System.out.println(outputList.size());
		return outputList;
	}
	
	/**
	 * Split the given data randomly into 10 folds but so that class proportions
	 * are preserved in each fold.
	 * 
	 * @param dataSet
	 *            {@link Map}<{@link Path}, {@link Sentiment}> All review paths
	 * @param seed
	 *            A seed for the random shuffling.
	 * @return {@link List}<{@link Map}<{@link Path}, {@link Sentiment}>> A set
	 *         of folds with even numbers of each sentiment
	 */
	
	@Override
	public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed) {
		// TODO Auto-generated method stub
		Map<Path,Sentiment> outputMap = shuffleList(dataSet,seed);
		assert(outputMap.size() == dataSet.size());
		Map<Path, Sentiment> mapPOSreviews = new HashMap<>();
		Map<Path,Sentiment> mapNEGreviews = new HashMap<>();
		for (Map.Entry<Path, Sentiment> element : dataSet.entrySet()) {
			if (element.getValue() == Sentiment.POSITIVE) {
				mapPOSreviews.put(element.getKey(), Sentiment.POSITIVE);
			}
			else {
				mapNEGreviews.put(element.getKey(), Sentiment.NEGATIVE);
			}
		}
		//List<Map<Path, Sentiment>> outputList = new LinkedList<Map<Path, Sentiment>>();
		Map<Path, Sentiment> tempMapPos = new HashMap<Path,Sentiment>();
		Map<Path, Sentiment> tempMapNeg = new HashMap<Path,Sentiment>();
		List<Map<Path, Sentiment>> PoutList = new LinkedList<>();
		int binSizePN = outputMap.size()/20;
		int count = 0;
		for (Map.Entry<Path, Sentiment> element : mapPOSreviews.entrySet()) {
			count++;
			tempMapPos.put(element.getKey(), Sentiment.POSITIVE);
			if (count == outputMap.size()/20) {
				PoutList.add(tempMapPos);
				tempMapPos = new HashMap<Path,Sentiment>();
				count = 0;
			}
		}
		List<Map<Path, Sentiment>> outList = new LinkedList<>();
		count = 0;
		for (Map.Entry<Path, Sentiment> element : mapNEGreviews.entrySet()) {
			count++;
			tempMapNeg.put(element.getKey(), Sentiment.NEGATIVE);
			if (count == binSizePN) {
				outList.add(tempMapNeg);
				tempMapNeg = new HashMap<Path,Sentiment>();
				count = 0;
			}
		}
		assert(PoutList.size() == 10);
		assert(outList.size() == 10);
		//Map<Path,Sentiment> tempM = new HashMap<>();
		for (int index = 0; index < PoutList.size(); index++) {
			//tempM = new HashMap<>(NoutList.get(index));
			outList.get(index).putAll(PoutList.get(index));
			
		}  
		
		return outList;
	}
	
	/**
	 * Run cross-validation on the dataset according to the folds.
	 * 
	 * @param folds
	 *            {@link List}<{@link Map}<{@link Path}, {@link Sentiment}>> A
	 *            set of folds.
	 * @return Scores for individual cross-validation runs.
	 * @throws IOException
	 */
	
	@Override
	public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException {
		// TODO Auto-generated method stub
		
		double[] accuracyAV = new double[folds.size()];
		IExercise1 implementation1 = (IExercise1) new Exercise1();
		IExercise2 implementation = (IExercise2) new Exercise2();
		for (int indexFold = 0; indexFold < folds.size(); indexFold++) {
			
			Map<Path,Sentiment> validationSet = new HashMap<>();
			validationSet= folds.get(indexFold);
			Map<Path, Sentiment> trainingSet = new HashMap<Path, Sentiment>();
			for (int index = 0; index< folds.size(); index++) {
				if (index!= indexFold) {
					trainingSet.putAll(folds.get(index));
				}
			}
			Map<Sentiment, Double> classProbabilities = implementation.calculateClassProbabilities(trainingSet);

			Map<String, Map<Sentiment, Double>> smoothedLogProbs = implementation
					.calculateSmoothedLogProbs(trainingSet);

			Map<Path, Sentiment> smoothedNBPredictions = implementation.naiveBayes(validationSet.keySet(),
					smoothedLogProbs, classProbabilities);
			double smoothedNBAccuracy = implementation1.calculateAccuracy(validationSet, smoothedNBPredictions);
			System.out.println("Naive Bayes classifier accuracy with smoothing:");
			System.out.println(smoothedNBAccuracy);
			accuracyAV[indexFold]= smoothedNBAccuracy;
		}
		return accuracyAV;
	}

	@Override
	public double cvAccuracy(double[] scores) {
		BigDecimal sum = new BigDecimal(0);
		for (double score : scores) {
			sum = sum.add(new BigDecimal(score));
		}
		BigDecimal average = sum.divide(new BigDecimal(scores.length));
		return average.doubleValue();
	}

	@Override
	public double cvVariance(double[] scores) {
		
		BigDecimal sumOfScores= new BigDecimal(0);
		
		for (double score : scores) {
			sumOfScores = sumOfScores.add(new BigDecimal(score));
		}
		BigDecimal mew = sumOfScores.divide(new BigDecimal(scores.length));
		BigDecimal var = new BigDecimal(0);
		for (double score : scores) {
			BigDecimal big = (new BigDecimal(score)).subtract(mew);
			var = var.add(big.multiply(big));
		}
		var = var.divide(new BigDecimal((double)scores.length));
		return var.doubleValue();
	}
	
	Map<Path, Sentiment> shuffleList(Map<Path,Sentiment> inputData, int seed){
		Random rand = new Random(seed);
		Path[] pathArr = new Path[inputData.keySet().size()];
		int count = 0;
		for(Map.Entry<Path, Sentiment> element : inputData.entrySet()) {
			pathArr[count] = element.getKey();
			count++;
		}
		assert(inputData.keySet().size()==inputData.size());
		int[] arr = new int[inputData.keySet().size()];
		Set<Integer> set = new HashSet<Integer>();
		for (int index = 0, a; index < inputData.size();)
		    if (set.add(a = rand.nextInt(inputData.size())))
		        arr[index++] = a;
		Map<Path, Sentiment> myMap = new HashMap<Path, Sentiment>();
		for (int i =0; i<arr.length; i++) {
			myMap.put(pathArr[arr[i]], inputData.get(pathArr[arr[i]]));
		}
		return myMap;
	}
	

}
