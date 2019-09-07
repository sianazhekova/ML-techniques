package uk.ac.cam.cl.mlrd.sz373.tick9;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.cam.cl.mlrd.sz373.tick7.DiceRoll;
import uk.ac.cam.cl.mlrd.sz373.tick7.DiceType;
import uk.ac.cam.cl.mlrd.sz373.tick7.HMMDataStore;
import uk.ac.cam.cl.mlrd.sz373.tick7.HiddenMarkovModel; 

/*
import uk.ac.cam.cl.mlrd.exercises.markov_models.AminoAcid;
import uk.ac.cam.cl.mlrd.exercises.markov_models.Feature;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise9;  */

public class Exercise9 implements IExercise9 {

	/**
	 * Loads the sequences of visible and hidden states from the sequence files
	 * (visible amino acids on first line and hidden features on second) and
	 * uses them to estimate the parameters of the Hidden Markov Model that
	 * generated them.
	 * 
	 * @param bioDataFiles
	 *            {@link Collection}<{@link Path}> The files containing amino
	 *            acid sequences
	 * @return {@link HiddenMarkovModel}<{@link AminoAcid}, {@link Feature}> The
	 *         estimated model
	 * @throws IOException
	 */
	@Override
	public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs)
			throws IOException {
		
		
		//List<HMMDataStore<DiceRoll, DiceType>> dataBA = new ArrayList<HMMDataStore<DiceRoll, DiceType>>();
		
		List<List<AminoAcid>> observedSeq = new LinkedList<>();
		List<List<Feature>> hiddenSeq = new LinkedList<>();
		for (HMMDataStore<AminoAcid,Feature> element : sequencePairs) {
			observedSeq.add(element.observedSequence);
			hiddenSeq.add(element.hiddenSequence);
		}
		// create transition and emission key matrices based on probability estimates for each entry with smoothing
		
		Map<Feature, Map<Feature, Double>> transitionM = new HashMap<>();
		Map<Feature, Map<AminoAcid, Double>> observedM = new HashMap<>();
		
		Map<Feature, Map<Feature, Integer>> countTransitions = new HashMap<>();
		
		for (Feature element1 : Feature.values()) {
			Map<Feature, Integer> tempMap = new HashMap<>();
			for (Feature element2 : Feature.values()) {
				tempMap.put(element2, 0);
				
			}
			countTransitions.put(element1, tempMap);
		}
		//System.out.println(hiddenSeq.size());
		for (List<Feature> elementList : hiddenSeq) {
			countTransitions.get(Feature.START).put(elementList.get(1), countTransitions.get(Feature.START).get(elementList.get(1))+1);
			countTransitions.get(elementList.get(elementList.size()-2)).put(Feature.END, countTransitions.get(elementList.get(elementList.size()-2)).get(Feature.END)+1);
			for (int index = 1; index < elementList.size()-2; index++) {
				countTransitions.get(elementList.get(index)).put(elementList.get(index+1), countTransitions.get(elementList.get(index)).get(elementList.get(index+1))+1);
			}
		}
		
		
		for (Feature t1 : Feature.values()) {
			transitionM.put(t1, (Map<Feature, Double>)new HashMap<Feature,Double>());
			
			for (Feature t2: Feature.values()) {
				transitionM.get(t1).put(t2, 0.0);
			}
		}
		for (Feature type1 : Feature.values()) {
			Map<Feature, Double> temp = new HashMap<>();
			int countAll = 0;
			for (Feature type2 : Feature.values()) {
				countAll+=countTransitions.get(type1).get(type2);
			}
			if (countAll!=0) {
				for (Feature type2: Feature.values()) {
					temp.put(type2,(double)countTransitions.get(type1).get(type2)/(double)countAll);
				}
				transitionM.put(type1,temp);
			}
			
				
			}
		
		// Now do the observedM
		/*   observedM */
		Map<Feature, Map<AminoAcid, Integer>> countEmissions = new HashMap<>();
		for (Feature d : Feature.values()) {
			Map<AminoAcid, Integer> tempMap = new HashMap<>();
			for (AminoAcid r : AminoAcid.values()) {
				tempMap.put(r, 0);
			}
			countEmissions.put(d, tempMap);
		}
		
		for (int indexL = 0; indexL < observedSeq.size(); indexL++) {
			for (int Ki = 0; Ki< observedSeq.get(indexL).size(); Ki++) {
				Feature si = hiddenSeq.get(indexL).get(Ki);
				AminoAcid ki = observedSeq.get(indexL).get(Ki);
				countEmissions.get(si).put(ki, countEmissions.get(si).get(ki)+1);
			}
		}
		
		for (Feature type : Feature.values()) {
			Map<AminoAcid, Double > tempMap1= new HashMap<>();
			int count = 0;
			for(AminoAcid roll : AminoAcid.values()) {
				count += countEmissions.get(type).get(roll);
			}
			if (count!=0) {
				for (AminoAcid roll: AminoAcid.values()) {
					int rollsCount = countEmissions.get(type).get(roll);
					//BigDecimal b_Kr = new BigDecimal(1.0);
					//b_Kr = b_Kr.multiply(new BigDecimal((double)rollsCount)).divide(new BigDecimal((double)count));
					double b_Kr = (double)rollsCount/count;
					tempMap1.put(roll, b_Kr);
				}
				observedM.put(type, tempMap1);
			}
			else {
				for (AminoAcid r : AminoAcid.values()) {
					tempMap1.put(r, 0.0);
				}
				observedM.put(type, tempMap1);
			}
			
		}
		
		return new HiddenMarkovModel<AminoAcid, Feature>(transitionM, observedM);
	}
	
	/**
	 * Uses the Viterbi algorithm to calculate the most likely single sequence
	 * of hidden states given the observed sequence.
	 * 
	 * @param model
	 *            A pre-trained HMM.
	 * @param observedSequence
	 *            {@link List}<{@link AminoAcid}> A sequence of observed amino
	 *            acids
	 * @return {@link List}<{@link Feature}> The most likely single sequence of
	 *         hidden states
	 */
	
	@Override
	public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence) {
		
		double[][] trellis = new double[observedSequence.size()][Feature.values().length];
        List<Map<Feature, Feature>>[][] psiList = new List[observedSequence.size()][Feature.values().length];
        trellis[0][Feature.START.ordinal()] = 1.0;  // since it will always emit this start symbol at start
        for (Feature hiddenState : Feature.values()){
        	//System.out.println(hiddenState.ordinal());
        	psiList[0][hiddenState.ordinal()] = new ArrayList<>();
        }
        for (int t = 1; t < observedSequence.size(); t++) {
        	AminoAcid O_t = observedSequence.get(t);
        	for (Feature d1 : Feature.values()) {
        		double maxPathlogProb = Double.NEGATIVE_INFINITY;  // Delta(d1)(time = t)
        		Feature psiMax = null;  // the state Si = d2 (at time = t-1) that maximises Delta(d1)(time t)
        		double probOfObs = Math.log(model.getEmissions(d1).get(O_t));
        		double logSum;
        		for (Feature d2 : Feature.values()) {
        			double logOfDeltaprev = trellis[t-1][d2.ordinal()];   // log(Delta(d2)(t-1))
        			double transitionProb = Math.log(model.getTransitions(d2).get(d1));  //log(a(ij))
        			logSum = probOfObs + transitionProb + logOfDeltaprev;
        			if (logSum >= maxPathlogProb) {
        				psiMax = d2;
        				maxPathlogProb = logSum;
        			}
        		}
        		Map<Feature,Feature> psiMap = new HashMap<>();
        		psiMap.put(psiMax,d1);
        		List<Map<Feature, Feature>> listOfMaxPsiPath = new ArrayList<>(psiList[t-1][psiMax.ordinal()]);
        		listOfMaxPsiPath.add(psiMap);
        		psiList[t][d1.ordinal()] = listOfMaxPsiPath;
        		trellis[t][d1.ordinal()] = maxPathlogProb;
        	}
        }
        List<Map<Feature, Feature>> maxPath = new ArrayList<>();
        double Max = Double.NEGATIVE_INFINITY;  // This is the maximum probability (path out of all memoised paths) for time = T  (max prob value in last row in trellis)
        for(Feature d : Feature.values()) {
        	if (trellis[observedSequence.size() -1][d.ordinal()] > Max) {
        		Max = trellis[observedSequence.size()-1][d.ordinal()];
        	}
        	maxPath = psiList[observedSequence.size()-1][d.ordinal()];
        }
        Feature addState = Feature.START;
        List<Feature> outputSeqOfHiddenStates = new ArrayList<>();
        outputSeqOfHiddenStates.add(addState);
        for(Map<Feature,Feature> transition : maxPath) {
        	addState = transition.get(addState);
        	outputSeqOfHiddenStates.add(addState);
        }
		
		return outputSeqOfHiddenStates;
	}
	
	/**
	 * Uses the Viterbi algorithm to predict hidden sequences of all observed
	 * sequences in testSequencePairs.
	 * 
	 * @param model
	 *            The HMM model.
	 * @param testSequencePair
	 *            A list of {@link HMMDataStore}s with observed and true hidden
	 *            sequences.
	 * @return {@link Map}<{@link List}<{@link Feature}>,
	 *         {@link Feature}<{@link Feature}>> A map from a real hidden
	 *         sequence to the equivalent estimated hidden sequence.
	 * @throws IOException
	 */
	
	@Override
	public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model,
			List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException {
		
		// This output map will store the true hidden sequences along with their predicted ones by the viterbi's alg
				Map<List<Feature>, List<Feature>> output = new HashMap<>();
				for (HMMDataStore<AminoAcid, Feature> p : testSequencePairs) {
					//HMMDataStore<AminoAcid,Feature> markovModel = HMMDataStore.loadDiceFile(p);
					List<Feature> hiddenSeq = p.hiddenSequence;
					List<AminoAcid> observedSeq = p.observedSequence;
					List<Feature> bestHiddenSeq = viterbi(model, observedSeq);
					output.put(hiddenSeq, bestHiddenSeq);
				}
		
		return output;
	}
	
	/**
	 * Calculates the precision of the estimated sequence with respect to the
	 * membrane state, i.e. the proportion of predicted membrane states that
	 * were actually in the membrane.
	 * 
	 * @param true2PredictedMap
	 *            {@link Map}<{@link List}<{@link Feature}>,
	 *            {@link List}<{@link Feature}>> A map from a real hidden
	 *            sequence to the equivalent estimated hidden sequence.
	 * @return <code>double</code> The precision of the estimated sequence with
	 *         respect to the membrane state averaged over all the test
	 *         sequences.
	 */
	
	@Override
	public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
		
		long correctCount = 0;
		long WtotalCount = 0;
		int c = 0;
		for (List<Feature> trueSeq : true2PredictedMap.keySet()) {
			System.out.println(c);
			List<Feature> predictedSeq = true2PredictedMap.get(trueSeq);
			System.out.println(predictedSeq);
			c++;
			for (int i = 0; i< predictedSeq.size(); i++) {
				//System.out.println(predictedSeq.get(i) + " " + i);
				if (predictedSeq.get(i).equals(Feature.MEMBRANE)) {
					if (trueSeq.get(i).equals(Feature.MEMBRANE)) {
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
	 * Calculate the recall for the membrane state.
	 * 
	 * @param true2PredictedMap
	 *            {@link Map}<{@link List}<{@link Feature}>,
	 *            {@link List}<{@link Feature}>> A map from a real hidden
	 *            sequence to the equivalent estimated hidden sequence.
	 * @return The recall for the membrane state.
	 */
	@Override
	public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) {
		
		long correctGuessCount = 0;
		long trueTotal = 0;
		for (List<Feature> trueSeq : true2PredictedMap.keySet()) {
			List<Feature> guessedSeq = true2PredictedMap.get(trueSeq);
			for (int i = 0; i< guessedSeq.size(); i++) {
				if(!trueSeq.get(i).equals(Feature.MEMBRANE)){
					continue;
				}
				else if (guessedSeq.get(i).equals(Feature.MEMBRANE)) {
					correctGuessCount+=1;
				}
				trueTotal++;
			}
		}
		double output = (double)correctGuessCount/(double)trueTotal;
		
		return output;
	}
	
	/**
	 * Calculate the F1 score for the membrane state.
	 * 
	 * @param true2PredictedMap
	 */
	
	@Override
	public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
		
		double precision = precision(true2PredictedMap);
		double recall = recall(true2PredictedMap);
		double harmonicMean = 2.0*recall*precision/(recall + precision);
		return harmonicMean;
	}

}
