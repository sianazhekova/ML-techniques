package uk.ac.cam.cl.mlrd.sz373.tick9;

//package uk.ac.cam.cl.mlrd.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import uk.ac.cam.cl.mlrd.sz373.tick7.*;
//import uk.ac.cam.cl.mlrd.sz373.tick9.IExercise9;



/*
import uk.ac.cam.cl.mlrd.exercises.markov_models.AminoAcid;
import uk.ac.cam.cl.mlrd.exercises.markov_models.Feature;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise9; */

public class Exercise9Tester {

	static final Path dataFile = Paths.get("data/bio_dataset.txt");

	public static void main(String[] args) throws IOException {

		List<HMMDataStore<AminoAcid, Feature>> sequencePairs = HMMDataStore.loadBioFile(dataFile);

		// Use for testing the code
		Collections.shuffle(sequencePairs, new Random(0));
		int testSize = sequencePairs.size() / 10;
		//List<HMMDataStore<AminoAcid, Feature>> devSet = sequencePairs.subList(0, testSize);
		//List<HMMDataStore<AminoAcid, Feature>> testSet = sequencePairs.subList(testSize, 2 * testSize);
		//List<HMMDataStore<AminoAcid, Feature>> trainingSet = sequencePairs.subList(testSize * 2, sequencePairs.size());
		// But:
		// TODO: Replace with cross-validation for the tick.
		
		
		List<List<HMMDataStore<AminoAcid, Feature>>> bins = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
            int begin = i * testSize;
            int halt = (i + 1) * testSize;
            bins.add(sequencePairs.subList(begin, halt));
        }
        double[] precisionList = new double[10];
        double[] recallList = new double[10];
        double[] fOneMeasureList = new double[10];
        for (int i = 0; i < 10; i++) {
            TupleClass p = funSplit(bins, i);
            List<HMMDataStore<AminoAcid, Feature>> devSet = p.mapInd;
            List<HMMDataStore<AminoAcid, Feature>> trainingSet = p.remaining;

            IExercise9 implementation9 = (IExercise9) new Exercise9();
            HiddenMarkovModel<AminoAcid, Feature> model = implementation9.estimateHMM(trainingSet);

            IExercise9 implementation = (IExercise9) new Exercise9();

            HMMDataStore<AminoAcid, Feature> data = sequencePairs.get(i);
            List<Feature> predicted = implementation.viterbi(model, data.observedSequence);

            Map<List<Feature>, List<Feature>> true2PredictedMap = implementation.predictAll(model, devSet);
            
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
        IExercise9 implementation = (IExercise9) new Exercise9();

		HiddenMarkovModel<AminoAcid, Feature> model = implementation.estimateHMM(trainingSet);
		System.out.println("Predicted transitions:");
		System.out.println(model.getTransitionMatrix());
		System.out.println();
		System.out.println("Predicted emissions:");
		System.out.println(model.getEmissionMatrix());
		System.out.println();

		HMMDataStore<AminoAcid, Feature> data = devSet.get(0);
		List<Feature> predicted = implementation.viterbi(model, data.observedSequence);
		System.out.println("True hidden sequence:");
		System.out.println(data.hiddenSequence);
		System.out.println();

		System.out.println("Predicted hidden sequence:");
		System.out.println(predicted);
		System.out.println();

		Map<List<Feature>, List<Feature>> true2PredictedSequences = implementation.predictAll(model, devSet);
		for (Map.Entry<List<Feature>, List<Feature>> element : true2PredictedSequences.entrySet()) {
			System.out.println(element.getKey()+" "+element.getValue() );
		}
		double accuracy = implementation.precision(true2PredictedSequences);
		System.out.println("Prediction precision:");
		System.out.println(accuracy);
		System.out.println();

		double recall = implementation.recall(true2PredictedSequences);
		System.out.println("Prediction recall:");
		System.out.println(recall);
		System.out.println();

		double f1Score = implementation.fOneMeasure(true2PredictedSequences);
		System.out.println("Prediction F1 score:");
		System.out.println(f1Score);
		System.out.println(); */
	}
    private static class TupleClass {
        public final List<HMMDataStore<AminoAcid,Feature>> mapInd;
        public final List<HMMDataStore<AminoAcid, Feature>> remaining;

        public TupleClass(List<HMMDataStore<AminoAcid, Feature>> someIndex, List<HMMDataStore<AminoAcid,Feature>> others) {
            this.mapInd = someIndex;
            this.remaining = others;
        }
    }
    
    private static double cvAccuracy(double[] scores){
    	//Exercise9Tester.scores = scores;
		BigDecimal sum = new BigDecimal(0);
		for (double score : scores) {
			sum = sum.add(new BigDecimal(score));
		}
		BigDecimal average = sum.divide(new BigDecimal(scores.length));
		return average.doubleValue();
    }
    
    private static TupleClass funSplit(List<List<HMMDataStore<AminoAcid, Feature>>> bins, int index) {
		List<HMMDataStore<AminoAcid, Feature>> mapIndex = null;
        List<List<HMMDataStore<AminoAcid, Feature>>> others = new ArrayList<>();
        for (int i = 0; i < bins.size(); i++) {
            if (i == index) {
                mapIndex = bins.get(i);
            } else {
                others.add(bins.get(i));
            }
        }
        List<HMMDataStore<AminoAcid, Feature>> allInOneListUnion = new ArrayList<>();
        for (List<HMMDataStore<AminoAcid, Feature>> m : others) {
            allInOneListUnion.addAll(m);
        }
        return new TupleClass(mapIndex, allInOneListUnion);
	}
}