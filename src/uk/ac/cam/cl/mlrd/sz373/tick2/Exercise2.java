package uk.ac.cam.cl.mlrd.sz373.tick2;
import java.io.IOException;

import java.nio.file.Path;
import java.util.*;
import java.nio.file.Paths;

import uk.ac.cam.cl.mlrd.sz373.tick1.*;
/*
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;
import uk.ac.cam.cl.mlrd.utils.DataSplit; */

public class Exercise2 implements IExercise2 {

	@Override
	public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {
		
		int pos=0,all = 0;
		Map<Sentiment, Double> probabilities = new HashMap<>();
		for(Map.Entry<Path, Sentiment>  ps : trainingSet.entrySet()) {
			//Path path = ps.getKey();
			if (ps.getValue() == Sentiment.POSITIVE) {
				pos++;
			}
			all++;
		}
		
		Double P_Pos_ =new Double(1.0*pos/all);
		probabilities.put(Sentiment.POSITIVE, P_Pos_);
		probabilities.put(Sentiment.NEGATIVE, (1-P_Pos_));
		return probabilities;	
	}

    /**
     * For each word and sentiment present in the training set, estimate the
     * unsmoothed log probability of a word to occur in a review with a
     * particular sentiment.
     *
     * @param trainingSet {@link Map}<{@link Path}, {@link Sentiment}> Training review
     *                    paths
     * @return {@link Map}<{@link String}, {@link Map}<{@link Sentiment},
     * {@link Double}>> Estimated log probabilities
     * @throws IOException
     */
    public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet)
            throws IOException {
        int PTotal = 0;
        int NTotal = 0;
        HashMap<String, Integer> POSwc = new HashMap<String, Integer>();
        HashMap<String, Integer> NEGwc = new HashMap<String, Integer>();
        for (Path path : trainingSet.keySet()) {
            Sentiment senti = trainingSet.get(path);
            List<String> tokens = Tokenizer.tokenize(path);
            for (String s : tokens) {
                if (senti == Sentiment.POSITIVE) {
                	
                    if (POSwc.containsKey(s)) {
                        int newtotal = POSwc.get(s) + 1;
                        POSwc.put(s, newtotal);
                    }
                    else {
                        POSwc.put(s, 1);
                    }
                    PTotal++;
                }
                else 
                {
                    if (NEGwc.containsKey(s)) {
                        int newtotal = NEGwc.get(s) + 1;
                        NEGwc.put(s, newtotal);
                    }
                    else {
                        NEGwc.put(s, 1);
                    }
                    NTotal++;
                }
            }
        }
        
        Map<String,Map<Sentiment,Double>> unsmoothedLogprob = new HashMap<String,Map<Sentiment,Double>>();
        HashMap<String, Map<Sentiment, Integer>> totalW_pn = new HashMap<>();
        for(String word : POSwc.keySet()) {
        	Map<Sentiment, Integer> SIpair = new HashMap<>();
        	SIpair.put(Sentiment.POSITIVE, POSwc.get(word));
        	totalW_pn.put(word, SIpair);
        }
        for(String word : NEGwc.keySet()) {
        	if(!totalW_pn.containsKey(word)) {
        		totalW_pn.put(word, new HashMap<>());
        	}
        	totalW_pn.get(word).put(Sentiment.NEGATIVE, NEGwc.get(word));
        }
        
        for(String word : totalW_pn.keySet()) {
        	
        	HashMap<Sentiment,Double> condProbOfWi = new HashMap<Sentiment,Double>();
        	Map<Sentiment, Integer> pair = totalW_pn.get(word);
        	double Wip = (double)pair.getOrDefault(Sentiment.POSITIVE, 0)/(double) PTotal;
        	condProbOfWi.put(Sentiment.POSITIVE, Math.log(Wip));
            double Win = (double)pair.getOrDefault(Sentiment.NEGATIVE, 0)/(double) NTotal;
        	condProbOfWi.put(Sentiment.NEGATIVE, Math.log(Win));
            unsmoothedLogprob.put(word,condProbOfWi); 
        	
            }
        
        return unsmoothedLogprob;

    }
    /**
     * For each word and sentiment present in the training set, estimate the
     * smoothed log probability of a word to occur in a review with a particular
     * sentiment. Use the smoothing technique described in the instructions.
     *
     * @param trainingSet
     *            {@link Map}<{@link Path}, {@link Sentiment}> Training review
     *            paths
     * @return {@link Map}<{@link String}, {@link Map}<{@link Sentiment},
     *         {@link Double}>> Estimated log probabilities
     * @throws IOException
     */
    public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet)
            throws IOException{

        HashMap<String, Integer> POSwc = new HashMap<String, Integer>();
        HashMap<String, Integer> NEGwc = new HashMap<String, Integer>();
        List<String> Vocab = new LinkedList<String>();
        int PTotal = 0;
        int NTotal = 0;
        for (Path p : trainingSet.keySet()) {
            
        	Sentiment senti = trainingSet.get(p);
            List<String> tokens = Tokenizer.tokenize(p);
            for (String s : tokens) {
                if (senti == Sentiment.POSITIVE) {
                    if (POSwc.containsKey(s)) {
                        POSwc.put(s, POSwc.get(s) + 1);
                    } else {
                        POSwc.put(s, 1);
                    }
                    PTotal++;
                } 
                else {
                    if (NEGwc.containsKey(s)) {
                        NEGwc.put(s, NEGwc.get(s) + 1);
                    } else {
                        NEGwc.put(s, 1);
                    }
                    NTotal++;
                }
                if(!Vocab.contains(s)){
                    Vocab.add(s);
                }
            }
        }  //Use Laplace rule to calculate smoothed Log probabilities for P(Wi|Class) for each Wi contained in the vocabulary 
        int V = Vocab.size();
        Map<String,Map<Sentiment,Double>> smoothLogProb = new HashMap<String,Map<Sentiment,Double>>();
        double base_NEG_chance = Math.log(1.0 / ((double)NTotal + V));
        double base_POS_chance = Math.log(1.0 / ((double)PTotal + V));
        for(String word : Vocab){
            HashMap<Sentiment,Double> map = new HashMap<Sentiment,Double>();
            map.put(Sentiment.POSITIVE, base_POS_chance);
            map.put(Sentiment.NEGATIVE, base_NEG_chance);
            smoothLogProb.put(word,map);
        }
        for(String word : POSwc.keySet()) {
            if (smoothLogProb.containsKey(word)) {
                double Wi = ((double) POSwc.getOrDefault(word, 0) + 1) / ((double) PTotal + V);
                Map<Sentiment, Double> p_w = smoothLogProb.get(word);
                p_w.put(Sentiment.POSITIVE, Math.log(Wi));
            }
        }
        for(String word : NEGwc.keySet()) {
            if (smoothLogProb.containsKey(word)) {
                double Wi = ((double) NEGwc.getOrDefault(word, 0) + 1) / ((double) NTotal + V);
                Map<Sentiment, Double> Pwi = smoothLogProb.get(word);
                Pwi.put(Sentiment.NEGATIVE, Math.log(Wi));
            }
        } 
        return smoothLogProb;
    }

    /**
     * Use the estimated log probabilities to predict the sentiment of each
     * review in the test set.
     *
     * @param testSet
     *            {@link Set}<{@link Path}> Test review paths
     * @param tokenLogProbs
     *            {@link Map}<{@link String}, {@link Map}<{@link Sentiment},
     *            {@link Double}>> Log probabilities
     * @return {@link Map}<{@link Path}, {@link Sentiment}> Predicted sentiments
     * @throws IOException
     */
    public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs,
                                           Map<Sentiment, Double> classProbabilities) throws IOException{


        HashMap<Path,Sentiment> PredAtOutput = new HashMap<Path,Sentiment>();
        for(Path p : testSet) {   //iterate through each path in the testing set
            double p_POS = Math.log(classProbabilities.get(Sentiment.POSITIVE));
            double p_NEG = Math.log(classProbabilities.get(Sentiment.NEGATIVE));
            List<String> tokens  = Tokenizer.tokenize(p);
            for(String word : tokens){

                if(tokenLogProbs.containsKey(word)){
                    Map<Sentiment,Double> LogProbperWord = tokenLogProbs.get(word);
                    p_POS += LogProbperWord.getOrDefault(Sentiment.POSITIVE, Double.NEGATIVE_INFINITY);
                    p_NEG += LogProbperWord.getOrDefault(Sentiment.NEGATIVE, Double.NEGATIVE_INFINITY);
                }   // otherwise if the token is not contained in the tokenLogProbs map, do not take it into account in the P(POSITIVE) calculation. 
            }
            if (p_POS >= p_NEG) {
                PredAtOutput.put(p,Sentiment.POSITIVE);
            }
            else{
                PredAtOutput.put(p,Sentiment.NEGATIVE);
            }
        }
        return PredAtOutput;
    }
}




