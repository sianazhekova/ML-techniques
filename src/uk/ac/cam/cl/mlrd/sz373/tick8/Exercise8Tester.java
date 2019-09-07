package uk.ac.cam.cl.mlrd.sz373.tick8;
import uk.ac.cam.cl.mlrd.sz373.tick7.*;
//package uk.ac.cam.cl.mlrd.testing;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.*;
import java.util.Map;
import java.util.Random;

/*
import uk.ac.cam.cl.emm68.exercises.Exercise7;
import uk.ac.cam.cl.emm68.exercises.Exercise8; 
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceRoll;
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceType;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise7;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise8;   */

public class Exercise8Tester {

	static final Path dataDirectory = Paths.get("data/dice_dataset");
	private static double[] scores;

	public static void main(String[] args)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		List<Path> sequenceFiles = new ArrayList<>();
		try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory)) {
			for (Path item : files) {
				sequenceFiles.add(item);
			}
		} catch (IOException e) {
			throw new IOException("Cant access the dataset.", e);
		}

		// Use for testing the code 
		/*
		Collections.shuffle(sequenceFiles, new Random(0));
		int testSize = sequenceFiles.size() / 10;
		List<Path> devSet = sequenceFiles.subList(0, testSize);
		List<Path> testSet = sequenceFiles.subList(testSize, 2 * testSize);
		List<Path> trainingSet = sequenceFiles.subList(testSize * 2, sequenceFiles.size()); */
		// But: Replace with cross-validation for the tick.
		Collections.shuffle(sequenceFiles, new Random(0));
		List<List<Path>> bins = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
            int begin = i * (sequenceFiles.size() / 10);
            int halt = (i + 1) * (sequenceFiles.size() / 10);
            bins.add(sequenceFiles.subList(begin, halt));
        }
        double[] precisionList = new double[10];
        double[] recallList = new double[10];
        double[] fOneMeasureList = new double[10];
        for (int i = 0; i < 10; i++) {
            TupleClass p = funSplit(bins, i);
            List<Path> devSet = p.mapInd;
            List<Path> trainingSet = p.remaining;

            IExercise7 implementation7 = (IExercise7) new Exercise7();
            HiddenMarkovModel<DiceRoll, DiceType> model = implementation7.estimateHMM(trainingSet);

            IExercise8 implementation = (IExercise8) new Exercise8();

            HMMDataStore<DiceRoll, DiceType> data = HMMDataStore.loadDiceFile(devSet.get(0));
            List<DiceType> predicted = implementation.viterbi(model, data.observedSequence);

            Map<List<DiceType>, List<DiceType>> true2PredictedMap = implementation.predictAll(model, devSet);
            double precision = implementation.precision(true2PredictedMap);

            double recall = implementation.recall(true2PredictedMap);

            double fOneMeasure = implementation.fOneMeasure(true2PredictedMap);

            precisionList[i] = precision;
            recallList[i] = recall;
            fOneMeasureList[i] = fOneMeasure;
        }
        
        System.out.println("Cross Validation Prediction precision:");
        System.out.println(cvAccuracy(precisionList));
        System.out.println();

        System.out.println("Cross Validation Prediction recall:");
        System.out.println(cvAccuracy(recallList));
        System.out.println();

        System.out.println("Cross Validation Prediction fOneMeasure:");
        System.out.println(cvAccuracy(fOneMeasureList));
        System.out.println();
		/*
		IExercise7 implementation7 = (IExercise7) new Exercise7();
		HiddenMarkovModel<DiceRoll, DiceType> model = implementation7.estimateHMM(trainingSet);

		IExercise8 implementation = (IExercise8) new Exercise8();

		HMMDataStore<DiceRoll, DiceType> data = HMMDataStore.loadDiceFile(devSet.get(0));
		List<DiceType> predicted = implementation.viterbi(model, data.observedSequence);
		System.out.println("True hidden sequence:");
		System.out.println(data.hiddenSequence);
		System.out.println();

		System.out.println("Predicted hidden sequence:");
		System.out.println(predicted);
		System.out.println();

		Map<List<DiceType>, List<DiceType>> true2PredictedMap = implementation.predictAll(model, devSet);
		double precision = implementation.precision(true2PredictedMap);
		System.out.println("Prediction precision:");
		System.out.println(precision);
		System.out.println();

		double recall = implementation.recall(true2PredictedMap);
		System.out.println("Prediction recall:");
		System.out.println(recall);
		System.out.println();

		double fOneMeasure = implementation.fOneMeasure(true2PredictedMap);
		System.out.println("Prediction fOneMeasure:");
		System.out.println(fOneMeasure);
		System.out.println(); */
        
        //helper functions
	}
        private static class TupleClass {
            public final List<Path> mapInd;
            public final List<Path> remaining;

            public TupleClass(List<Path> someIndex, List<Path> others) {
                this.mapInd = someIndex;
                this.remaining = others;
            }
        }
        
        private static double cvAccuracy(double[] scores){
        	Exercise8Tester.scores = scores;
			BigDecimal sum = new BigDecimal(0);
    		for (double score : scores) {
    			sum = sum.add(new BigDecimal(score));
    		}
    		BigDecimal average = sum.divide(new BigDecimal(scores.length));
    		return average.doubleValue();
        }
        
        private static TupleClass funSplit(List<List<Path>> bins, int index) {
    		List<Path> mapIndex = null;
            List<List<Path>> others = new ArrayList<>();
            for (int i = 0; i < bins.size(); i++) {
                if (i == index) {
                    mapIndex = bins.get(i);
                } else {
                    others.add(bins.get(i));
                }
            }
            List<Path> allInOneListUnion = new ArrayList<>();
            for (List<Path> m : others) {
                allInOneListUnion.addAll(m);
            }
            return new TupleClass(mapIndex, allInOneListUnion);
    	}

	
}