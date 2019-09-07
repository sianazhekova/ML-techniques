package uk.ac.cam.cl.mlrd.sz373.tick7;

import java.io.IOException;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.*;
import java.util.LinkedList;
import java.util.List; /*

import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceRoll;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrd.exercises.markov_models.DiceType;
import uk.ac.cam.cl.mlrd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise7; */

public class Exercise7 implements IExercise7 {

	/**
	 * Loads the sequences of visible and hidden states from the sequence files
	 * (visible dice rolls on first line and hidden dice types on second) and uses
	 * them to estimate the parameters of the Hidden Markov Model that generated
	 * them.
	 * 
	 * @param sequenceFiles
	 *            {@link Collection}<{@link Path}> The files containing dice roll
	 *            sequences
	 * @return {@link HiddenMarkovModel}<{@link DiceRoll}, {@link DiceType}> The
	 *         estimated model
	 * @throws IOException 
	 */
	@Override
	public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {
		
		List<HMMDataStore<DiceRoll, DiceType>> dataBA = new ArrayList<HMMDataStore<DiceRoll, DiceType>>();
		for (Path p : sequenceFiles) {
			dataBA.add(HMMDataStore.loadDiceFile(p));
		}
		List<List<DiceRoll>> observedSeq = new LinkedList<>();
		List<List<DiceType>> hiddenSeq = new LinkedList<>();
		for (HMMDataStore<DiceRoll,DiceType> element : dataBA) {
			observedSeq.add(element.observedSequence);
			hiddenSeq.add(element.hiddenSequence);
		}
		// create transition and emission key matrices based on probability estimates for each entry with smoothing
		int vocab = DiceRoll.values().length -2;
		int Se_size = DiceType.values().length -2;
		int rowsOfA = Se_size + 2;  //N
		int columnsOfB = vocab +2;  //M
		Map<DiceType, Map<DiceType, Double>> transitionM = new HashMap<>();
		Map<DiceType, Map<DiceRoll, Double>> observedM = new HashMap<>();
		
		Map<DiceType, Map<DiceType, Integer>> countTransitions = new HashMap<>();
		
		for (DiceType element1 : DiceType.values()) {
			Map<DiceType, Integer> tempMap = new HashMap<>();
			for (DiceType element2 : DiceType.values()) {
				tempMap.put(element2, 0);
				
			}
			countTransitions.put(element1, tempMap);
		}
		//System.out.println(hiddenSeq.size());
		for (List<DiceType> elementList : hiddenSeq) {
			countTransitions.get(DiceType.START).put(elementList.get(1), countTransitions.get(DiceType.START).get(elementList.get(1))+1);
			countTransitions.get(elementList.get(elementList.size()-2)).put(DiceType.END, countTransitions.get(elementList.get(elementList.size()-2)).get(DiceType.END)+1);
			for (int index = 1; index < elementList.size()-2; index++) {
				countTransitions.get(elementList.get(index)).put(elementList.get(index+1), countTransitions.get(elementList.get(index)).get(elementList.get(index+1))+1);
			}
		}
		
		//Calculate transitionM now from out countTransitions:
		/*
		for (DiceType element1 : DiceType.values()) {
			
			Map<DiceType, Double> tempMap = new HashMap<>();
			int countAll = 0;
			
			for (DiceType element2 : DiceType.values()) {
				countAll+=countTransitions.get(element1).get(element2);
				System.out.println(countAll);
			}
			if (countAll !=0) {
			for (DiceType element2 : countTransitions.get(element1).keySet()) {
				
				double estimateProb = (double)(countTransitions.get(element1).get(element2))/(double)countAll;
				System.out.println(countTransitions.get(element1).get(element2)+" countTransition");
				System.out.println(countAll);
				System.out.println(estimateProb);
				tempMap.put(element2, estimateProb);
			}
			transitionM.put(element1, tempMap);
			}
			else {
				Map<DiceType, Double> nullMap = new HashMap<>();
				for (DiceType d : DiceType.values()) {nullMap.put(d, 0.0);}
				transitionM.put(element1, nullMap);
			}
		} */
		for (DiceType t1 : DiceType.values()) {
			transitionM.put(t1, (Map<DiceType, Double>)new HashMap<DiceType,Double>());
			
			for (DiceType t2: DiceType.values()) {
				transitionM.get(t1).put(t2, 0.0);
			}
		}
		for (DiceType type1 : DiceType.values()) {
			Map<DiceType, Double> temp = new HashMap<>();
			int countAll = 0;
			for (DiceType type2 : DiceType.values()) {
				countAll+=countTransitions.get(type1).get(type2);
			}
			if (countAll!=0) {
				for (DiceType type2: DiceType.values()) {
					temp.put(type2,(double)countTransitions.get(type1).get(type2)/(double)countAll);
				}
				transitionM.put(type1,temp);
			}
			
				
			}
		
		// Now do the observedM
		/*   observedM */
		Map<DiceType, Map<DiceRoll, Integer>> countEmissions = new HashMap<>();
		for (DiceType d : DiceType.values()) {
			Map<DiceRoll, Integer> tempMap = new HashMap<>();
			for (DiceRoll r : DiceRoll.values()) {
				tempMap.put(r, 0);
			}
			countEmissions.put(d, tempMap);
		}
		
		for (int indexL = 0; indexL < observedSeq.size(); indexL++) {
			for (int Ki = 0; Ki< observedSeq.get(indexL).size(); Ki++) {
				DiceType si = hiddenSeq.get(indexL).get(Ki);
				DiceRoll ki = observedSeq.get(indexL).get(Ki);
				countEmissions.get(si).put(ki, countEmissions.get(si).get(ki)+1);
			}
		}
		
		for (DiceType type : DiceType.values()) {
			Map<DiceRoll, Double > tempMap1= new HashMap<>();
			int count = 0;
			for(DiceRoll roll : DiceRoll.values()) {
				count += countEmissions.get(type).get(roll);
			}
			if (count!=0) {
				for (DiceRoll roll: DiceRoll.values()) {
					int rollsCount = countEmissions.get(type).get(roll);
					//BigDecimal b_Kr = new BigDecimal(1.0);
					//b_Kr = b_Kr.multiply(new BigDecimal((double)rollsCount)).divide(new BigDecimal((double)count));
					double b_Kr = (double)rollsCount/count;
					tempMap1.put(roll, b_Kr);
				}
				observedM.put(type, tempMap1);
			}
			else {
				for (DiceRoll r : DiceRoll.values()) {
					tempMap1.put(r, 0.0);
				}
				observedM.put(type, tempMap1);
			}
			
		}
		
		return new HiddenMarkovModel<DiceRoll, DiceType>(transitionM, observedM);
	}

}
