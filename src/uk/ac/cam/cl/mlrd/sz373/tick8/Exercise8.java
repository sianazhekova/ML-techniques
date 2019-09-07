package uk.ac.cam.cl.mlrd.sz373.tick8;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.*;

import uk.ac.cam.cl.mlrd.sz373.tick7.DiceRoll;
import uk.ac.cam.cl.mlrd.sz373.tick7.DiceType;
import uk.ac.cam.cl.mlrd.sz373.tick7.HMMDataStore;
import uk.ac.cam.cl.mlrd.sz373.tick7.HiddenMarkovModel; /*

import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceRoll;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceType;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise7;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise8;  */
public class Exercise8 implements IExercise8 {

	/**
	 * Uses the Viterbi algorithm to calculate the most likely single sequence
	 * of hidden states given the observed sequence and a model.
	 * 
	 * @param model
	 *            {@link HiddenMarkovModel}<{@link DiceRoll}, {@link DiceType}>
	 *            A sequence model.
	 * @param observedSequence
	 *            {@link List}<{@link DiceRoll}> A sequence of observed die
	 *            rolls
	 * @return {@link List}<{@link DiceType}> The most likely single sequence of
	 *         hidden states
	 */
	@Override
	public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence) {
		// observedSequence.size() ---> number of our time steps T +2
		double[][] trellis = new double[observedSequence.size()][DiceType.values().length];
        List<Map<DiceType, DiceType>>[][] psiList = new List[observedSequence.size()][DiceType.values().length];
        trellis[0][DiceType.START.ordinal()] = 1.0;  // since it will always emit this start symbol at start
        for (DiceType hiddenState : DiceType.values()){
        	psiList[0][hiddenState.ordinal()] = new ArrayList<>();
        }
        for (int t = 1; t < observedSequence.size(); t++) {
        	DiceRoll O_t = observedSequence.get(t);
        	for (DiceType d1 : DiceType.values()) {
        		double maxPathlogProb = Double.NEGATIVE_INFINITY;  // Delta(d1)(time = t)
        		DiceType psiMax = null;  // the state Si = d2 (at time = t-1) that maximises Delta(d1)(time t)
        		double probOfObs = Math.log(model.getEmissions(d1).get(O_t));
        		double logSum;
        		for (DiceType d2 : DiceType.values()) {
        			double logOfDeltaprev = trellis[t-1][d2.ordinal()];   // log(Delta(d2)(t-1))
        			double transitionProb = Math.log(model.getTransitions(d2).get(d1));  //log(a(ij))
        			logSum = probOfObs + transitionProb + logOfDeltaprev;
        			if (logSum >= maxPathlogProb) {
        				psiMax = d2;
        				maxPathlogProb = logSum;
        			}
        		}
        		Map<DiceType,DiceType> psiMap = new HashMap<>();
        		psiMap.put(psiMax,d1);
        		List<Map<DiceType, DiceType>> listOfMaxPsiPath = new ArrayList<>(psiList[t-1][psiMax.ordinal()]);
        		listOfMaxPsiPath.add(psiMap);
        		psiList[t][d1.ordinal()] = listOfMaxPsiPath;
        		trellis[t][d1.ordinal()] = maxPathlogProb;
        	}
        }
        List<Map<DiceType, DiceType>> maxPath = new ArrayList<>();
        double Max = Double.NEGATIVE_INFINITY;  // This is the maximum probability (path out of all memoised paths) for time = T  (max prob value in last row in trellis)
        for(DiceType d : DiceType.values()) {
        	if (trellis[observedSequence.size() -1][d.ordinal()] > Max) {
        		Max = trellis[observedSequence.size()-1][d.ordinal()];
        	}
        	maxPath = psiList[observedSequence.size()-1][d.ordinal()];
        }
        DiceType addState = DiceType.START;
        List<DiceType> outputSeqOfHiddenStates = new ArrayList<>();
        outputSeqOfHiddenStates.add(addState);
        for(Map<DiceType,DiceType> transition : maxPath) {
        	addState = transition.get(addState);
        	outputSeqOfHiddenStates.add(addState);
        }
		return outputSeqOfHiddenStates;
	}
	/**
	 * Uses the Viterbi algorithm to predict hidden sequences of all observed
	 * sequences in testFiles.
	 * 
	 * @param model
	 *            The HMM model.
	 * @param testFiles
	 *            A list of files with observed and true hidden sequences.
	 * @return {@link Map}<{@link List}<{@link DiceType}>,
	 *         {@link List}<{@link DiceType}>> A map from a real hidden sequence
	 *         to the equivalent estimated hidden sequence.
	 * @throws IOException
	 */
	@Override
	public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model,
			List<Path> testFiles) throws IOException {
		// This output map will store the true hidden sequences along with their predicted ones by the viterbi's alg
		Map<List<DiceType>, List<DiceType>> output = new HashMap<>();
		for (Path p : testFiles) {
			HMMDataStore<DiceRoll,DiceType> markovModel = HMMDataStore.loadDiceFile(p);
			List<DiceType> hiddenSeq = markovModel.hiddenSequence;
			List<DiceRoll> observedSeq = markovModel.observedSequence;
			List<DiceType> bestHiddenSeq = viterbi(model, observedSeq);
			output.put(hiddenSeq, bestHiddenSeq);
		}
		return output;
	}
	/**
	 * Calculates the precision of the estimated sequence with respect to the
	 * weighted state, i.e. the proportion of predicted weighted states that
	 * were actually weighted.
	 * 
	 * @param true2PredictedMap
	 *            {@link Map}<{@link List}<{@link DiceType}>,
	 *            {@link List}<{@link DiceType}>> A map from a real hidden
	 *            sequence to the equivalent estimated hidden sequence.
	 * @return <code>double</code> The precision of the estimated sequence with
	 *         respect to the weighted state averaged over all the test
	 *         sequences.
	 */
	@Override
	public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
		long correctCount = 0;
		long WtotalCount = 0;
		for (List<DiceType> trueSeq : true2PredictedMap.keySet()) {
			List<DiceType> predictedSeq = true2PredictedMap.get(trueSeq);
			for (int i = 0; i< predictedSeq.size(); i++) {
				if (predictedSeq.get(i).equals(DiceType.WEIGHTED)) {
					if (trueSeq.get(i).equals(DiceType.WEIGHTED)) {
						correctCount++;
					}
					WtotalCount++;
				}
			}
		}
		double precision = (double)correctCount/(double)WtotalCount;
		return precision;
	}

	/**
	 * Calculates the recall of the estimated sequence with respect to the
	 * weighted state, i.e. the proportion of actual weighted states that were
	 * predicted weighted.
	 * 
	 * @param true2PredictedMap
	 *            {@link Map}<{@link List}<{@link DiceType}>,
	 *            {@link List}<{@link DiceType}>> A map from a real hidden
	 *            sequence to the equivalent estimated hidden sequence.
	 * @return <code>double</code> The recall of the estimated sequence with
	 *         respect to the weighted state averaged over all the test
	 *         sequences.
	 */
	@Override
	public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
		long correctGuessCount = 0;
		long trueTotal = 0;
		for (List<DiceType> trueSeq : true2PredictedMap.keySet()) {
			List<DiceType> guessedSeq = true2PredictedMap.get(trueSeq);
			for (int i = 0; i< guessedSeq.size(); i++) {
				if(!trueSeq.get(i).equals(DiceType.WEIGHTED)){
					continue;
				}
				else if (guessedSeq.get(i).equals(DiceType.WEIGHTED)) {
					correctGuessCount+=1;
				}
				trueTotal++;
			}
		}
		double output = (double)correctGuessCount/(double)trueTotal;
		return output;
	}
	/**
	 * Calculates the F1 measure of the estimated sequence with respect to the
	 * weighted state, i.e. the harmonic mean of precision and recall.
	 * 
	 * @param true2PredictedMap
	 *            {@link Map}<{@link List}<{@link DiceType}>,
	 *            {@link List}<{@link DiceType}>> A map from a real hidden
	 *            sequence to the equivalent estimated hidden sequence.
	 * @return <code>double</code> The F1 measure of the estimated sequence with
	 *         respect to the weighted state averaged over all the test
	 *         sequences.
	 */
	@Override
	public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
		// TODO Auto-generated method stub
		double precision = precision(true2PredictedMap);
		double recall = recall(true2PredictedMap);
		double harmonicMean = 2.0*recall*precision/(recall + precision);
		return harmonicMean;
	}

}
