package uk.ac.cam.cl.mlrd.sz373.tick6;
import uk.ac.cam.cl.mlrd.sz373.tick1.Exercise1;
import uk.ac.cam.cl.mlrd.sz373.tick1.IExercise1;
import uk.ac.cam.cl.mlrd.sz373.tick2.Exercise2;
import uk.ac.cam.cl.mlrd.sz373.tick2.IExercise2;
import uk.ac.cam.cl.mlrd.sz373.tick5.Exercise5;
import uk.ac.cam.cl.mlrd.sz373.tick5.IExercise5;

import java.io.IOException;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import uk.ac.cam.cl.mlrd.sz373.tick1.Sentiment;
import uk.ac.cam.cl.mlrd.sz373.tick1.Tokenizer;

import java.util.*; /*
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation6;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise6;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.NuancedSentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.utils.DataSplit3Way; */
public class Exercise6 implements IExercise6 {

    public  List<Map<Path, NuancedSentiment>> nuancedSplitCVRandom(Map<Path, NuancedSentiment> dataSet, int seed) {
        List<Path> shuffledPaths = new LinkedList<>();
        
        for (Path p : dataSet.keySet()) {
            shuffledPaths.add(p);
        }
        
        shuffleList(shuffledPaths, seed);
        int binSize = shuffledPaths.size()/10;
        List<Map<Path, NuancedSentiment>> bins = new LinkedList<>();
        
        for (int i = 1; i <= 10; i++) {
            Map<Path, NuancedSentiment> myBin = new HashMap<>();
            for (int j = binSize * (i - 1); j < binSize * i; j++) {
            	
                myBin.put(shuffledPaths.get(j), dataSet.get(shuffledPaths.get(j)));
                
            }
            bins.add(myBin);
        }
        return bins;
    }

    public double[] nuancedCrossValidate(List<Map<Path, NuancedSentiment>> folds) throws IOException {
        double[] scores = new double[folds.size()];
        IExercise1 e1 = new Exercise1();
        IExercise2 e2 = new Exercise2();
        IExercise5 e5 = new Exercise5();
        
        for (int x = 0; x < 10; x++) {
        	
            Map<Path, NuancedSentiment> testM = folds.get(x);
            Set<Path> testS = new HashSet<>();
            
            for (Map.Entry<Path, NuancedSentiment> element : testM.entrySet()) {
                testS.add(element.getKey());
            }
            Map<Path, NuancedSentiment> trainS = new HashMap<>();
            
            for (int y = 0; y < 10 && y!= 0; y++) {
            	
                trainS.putAll(folds.get(y));
            }
            
            Map<String, Map<NuancedSentiment, Double>> tokenLogProbs = calculateNuancedLogProbs(trainS);
            Map<NuancedSentiment, Double> classProbabilities = calculateClassProbabilities(trainS);
            Map<Path, NuancedSentiment> estimates = nuancedClassifier(testS, tokenLogProbs, classProbabilities);
            scores[x] = nuancedAccuracy(testM, estimates);
        }

        return scores;
    }

    public static void shuffleList(List<Path> list, int seed) {
        int n = list.size();
        Random random = new Random();
        random.setSeed(seed);
        random.nextInt();
        for (int i = 0; i < n; i++) {
        	
            int temp = i + random.nextInt(n - i);
            Path path = list.get(i);
            
            list.set(i, list.get(temp));
            list.set(temp, path);
            
        }
    }
    
    /**
     * Calculate the probability of a document belonging to a given class based
     * on the training data.
     *
     * @param trainingSet {@link Map}<{@link Path}, {@link NuancedSentiment}> Training review
     *                    paths
     * @return {@link Map}<{@link NuancedSentiment}, {@link Double}> Class
     * probabilities.
     * @throws IOException
     */
    
    public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        
    	int pos = 0, neg = 0, neutral = 0;
        for (Path path : trainingSet.keySet()) {
        	
            NuancedSentiment sentiment = trainingSet.get(path);
            if (sentiment == NuancedSentiment.POSITIVE) {
                pos++;
            } else if (sentiment == NuancedSentiment.NEGATIVE) {
                neg++;
            } else {
                neutral++;
            }
        }
        Map<NuancedSentiment, Double> classProbabilities = new HashMap<>();
        
        double pPOS = 1.0 * pos / (pos + neg + neutral);
        double pNEG = 1.0 * neg / (pos + neg + neutral);
        double pNEU = 1.0 * neutral / (pos + neg + neutral);
        
        classProbabilities.put(NuancedSentiment.POSITIVE, pPOS);
        classProbabilities.put(NuancedSentiment.NEGATIVE, pNEG);
        classProbabilities.put(NuancedSentiment.NEUTRAL, pNEU);
        
        return classProbabilities; 
    }

    /**
     * Modify your smoothed Naive Bayes to calculate log probabilities for three classes.
     *
     * @param trainingSet {@link Map}<{@link Path}, {@link NuancedSentiment}> Training review
     *                    paths
     * @return {@link Map}<{@link String}, {@link Map}<{@link NuancedSentiment},
     * {@link Double}>> Estimated log probabilities
     * @throws IOException
     */
    
    public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet)
            throws IOException {

        HashMap<String, Integer> wcPOS = new HashMap<>();
        HashMap<String, Integer> wcNEG = new HashMap<>();
        HashMap<String, Integer> wcNEU = new HashMap<>();

        List<String> vocab = new LinkedList<>();
        int totalPosWords = 0, totalNegWords = 0, totalNeutWords = 0;
        for (Path path : trainingSet.keySet()) {
            NuancedSentiment currentSentiment = trainingSet.get(path);

            List<String> tokens = Tokenizer.tokenize(path);
            for (String w : tokens) {
                if (currentSentiment == NuancedSentiment.POSITIVE) {
                    if (wcPOS.containsKey(w)) {
                    	
                        wcPOS.put(w, wcPOS.get(w) + 1);
                        
                    } else {
                    	
                        wcPOS.put(w, 1);
                        
                    }
                    
                    totalPosWords++;
                } else if (currentSentiment == NuancedSentiment.NEGATIVE) {
                    if (wcNEG.containsKey(w)) {
                    	
                        wcNEG.put(w, wcNEG.get(w) + 1);
                        
                    } else {
                    	
                        wcNEG.put(w, 1);
                        
                    }
                    totalNegWords++;
                } else {
                	// WHen it is neutral
                    if (wcNEU.containsKey(w)) {
                    	
                        wcNEU.put(w, wcNEU.get(w) + 1);
                        
                    } else {
                        wcNEU.put(w, 1);
                    }
                    totalNeutWords++;
                }

                if (!vocab.contains(w)) {
                	
                    vocab.add(w);
                }
            }
        }
        
        int V = vocab.size();
        
        Map<String, Map<NuancedSentiment, Double>> smoothedLogProbs = new HashMap<>();
        
        double baseNegProb = Math.log(1.0 / (totalNegWords + V));
        double basePosProb = Math.log(1.0 / (totalPosWords + V));
        double baseNeutProb = Math.log(1.0 / (totalNeutWords + V));

        for (String w : vocab) {
            HashMap<NuancedSentiment, Double> indProb = new HashMap<>();
            indProb.put(NuancedSentiment.POSITIVE, basePosProb);
            indProb.put(NuancedSentiment.NEGATIVE, baseNegProb);
            indProb.put(NuancedSentiment.NEUTRAL, baseNeutProb);
            smoothedLogProbs.put(w, indProb);
        }

        for (String word : wcPOS.keySet()) {
            if (smoothedLogProbs.containsKey(word)) {
                if (wcPOS.get(word) != null) {
                    double Wi = (1.0 * wcPOS.get(word) + 1) / (totalPosWords + V);
                    Map<NuancedSentiment, Double> indProb = smoothedLogProbs.get(word);
                    indProb.put(NuancedSentiment.POSITIVE, Math.log(Wi));
                }
            }
        }

        for (String word : wcNEG.keySet()) {
            if (smoothedLogProbs.containsKey(word)) {
                if (wcNEG.get(word) != null) {
                    double Wi = (1.0 * wcNEG.get(word) + 1) / (totalNegWords + V);
                    Map<NuancedSentiment, Double> indiProb = smoothedLogProbs.get(word);
                    indiProb.put(NuancedSentiment.NEGATIVE, Math.log(Wi));
                }
            }
        }

        for (String word : wcNEU.keySet()) {
            if (smoothedLogProbs.containsKey(word)) {
                if (wcNEU.get(word) != null) {
                    double Wi = (1.0 * wcNEU.get(word) + 1) / (totalNeutWords + V);
                    Map<NuancedSentiment, Double> indiProb = smoothedLogProbs.get(word);
                    indiProb.put(NuancedSentiment.NEUTRAL, Math.log(Wi));
                }
            }
        }
        return smoothedLogProbs;
    }

    /**
     * Modify your Naive Bayes classifier so that it can classify reviews which
     * may also have neutral sentiment.
     *
     * @param testSet            {@link Set}<{@link Path}> Test review paths
     * @param tokenLogProbs      {@link Map}<{@link String}, {@link Map}<{@link NuancedSentiment}, {@link Double}> tokenLogProbs
     * @param classProbabilities {@link Map}<{@link NuancedSentiment}, {@link Double}> classProbabilities
     * @return {@link Map}<{@link Path}, {@link NuancedSentiment}> Predicted sentiments
     * @throws IOException
     */
    
    public Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet,
                                                         Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities)
            throws IOException {
        HashMap<Path, NuancedSentiment> predictedSentiments = new HashMap<>();

        for (Path path : testSet) {
            double posProbSum = 0;
            double negProbSum = 0;
            double neutProbSum = 0;

            for (String word : Tokenizer.tokenize(path)) {
                if (tokenLogProbs.containsKey(word)) {
                    posProbSum += (tokenLogProbs.get(word)).get(NuancedSentiment.POSITIVE);
                    negProbSum += (tokenLogProbs.get(word)).get(NuancedSentiment.NEGATIVE);
                    neutProbSum += (tokenLogProbs.get(word)).get(NuancedSentiment.NEUTRAL);
                }
            }
            double posProb = Math.log(classProbabilities.get(NuancedSentiment.POSITIVE)) + posProbSum;
            double negProb = Math.log(classProbabilities.get(NuancedSentiment.NEGATIVE)) + negProbSum;
            double neutProb = Math.log(classProbabilities.get(NuancedSentiment.NEGATIVE)) + neutProbSum;

            if (neutProb > posProb && neutProb > negProb) {
                predictedSentiments.put(path, NuancedSentiment.NEUTRAL);
            } else if (posProb > negProb) {
                predictedSentiments.put(path, NuancedSentiment.POSITIVE);
            } else {
                predictedSentiments.put(path, NuancedSentiment.NEGATIVE);
            }
        }
        return predictedSentiments;
    }

    /**
     * Calculate the proportion of predicted sentiments that were correct.
     *
     * @param trueSentiments      {@link Map}<{@link Path}, {@link NuancedSentiment}> Map of
     *                            correct sentiment for each review
     * @param predictedSentiments {@link Map}<{@link Path}, {@link NuancedSentiment}> Map of
     *                            calculated sentiment for each review
     * @return <code>double</code> The overall accuracy of the predictions
     */
    
    public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments,
                                  Map<Path, NuancedSentiment> predictedSentiments) {
        int all = 0, correct = 0;
        for (Map.Entry<Path, NuancedSentiment> pairP : predictedSentiments.entrySet()) {
            for (Map.Entry<Path, NuancedSentiment> pairT : trueSentiments.entrySet()) {
                all++;
                if (pairP.getValue() == trueSentiments.get(pairP.getKey())) {
                    correct++;
                }
            }
        }
        double proportion = correct * 1.0 / all;
        return proportion;
    }

    /**
     * Given some predictions about the sentiment in reviews, generate an
     * agreement table which for each review contains the number of predictions
     * that predicted each sentiment.
     *
     * @param predictedSentiments {@link Collection}<{@link Map}<{@link Integer},
     *                            {@link Sentiment}>> Different predictions for the
     *                            sentiment in each of a set of reviews 1, 2, 3, 4.
     * @return {@link Map}<{@link Integer}, {@link Map}<{@link Sentiment},
     * {@link Integer}>> For each review, the number of predictions that
     * predicted each sentiment
     */
    
    public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments) {

        int[] predictions = new int[8];
        for (Map<Integer, Sentiment> prediction : predictedSentiments) {
        	
            for (int i = 1; i <= 4; i++) {
                Sentiment sentiment = prediction.get(i);
                if (sentiment == Sentiment.POSITIVE) {
                    predictions[i - 1]++;
                } else {
                	
                    predictions[i + 3]++;
                    
                }

            }
        }

        Map<Integer, Map<Sentiment, Integer>> number_of_predictions = new HashMap<>();

        for (Map<Integer, Sentiment> prediction : predictedSentiments) {
            for (int i = 1; i <= 4; i++) {
                Map<Sentiment, Integer> prediction_map = new HashMap<>();
                prediction_map.put(Sentiment.POSITIVE, predictions[i - 1]);
                prediction_map.put(Sentiment.NEGATIVE, predictions[i + 3]);
                number_of_predictions.put(i, prediction_map);
            }
        }

        return number_of_predictions;
    }

    /**
     * Using your agreement table, calculate the kappa value for how much
     * agreement there was; 1 should mean total agreement and -1 should mean total disagreement.
     *
     * @param agreementTable {@link Map}<{@link Integer}, {@link Map}<{@link Sentiment},
     *                       {@link Integer}>> For each review (1, 2, 3, 4) the number of predictions
     *                       that predicted each sentiment
     * @return <code>double</code> The kappa value, between -1 and 1
     */
    
    public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {

        double N = agreementTable.size();
        double Pe = 0;
        double pos_sum = 0;
        double neg_sum = 0;
        for (int rev_num : agreementTable.keySet()) {
            Map<Integer, Integer> predictions = new HashMap<>();
            for (Sentiment sentiment : agreementTable.get(rev_num).keySet()) {
                if (predictions.containsKey(rev_num)) {
                    predictions.put(rev_num, agreementTable.get(rev_num).get(sentiment) + predictions.get(rev_num));
                } else {
                    predictions.put(rev_num, agreementTable.get(rev_num).get(sentiment));
                }
            }
            for (Sentiment sentiment : agreementTable.get(rev_num).keySet()) {
                if (sentiment == Sentiment.POSITIVE) {
                    pos_sum += 1.0 * agreementTable.get(rev_num).get(sentiment) / predictions.get(rev_num);

                } else if (sentiment == Sentiment.NEGATIVE) {
                    neg_sum += 1.0 * agreementTable.get(rev_num).get(sentiment) / predictions.get(rev_num);
                }
            }
        }

        Pe = Math.pow(pos_sum / N, 2) + Math.pow(neg_sum / N, 2);
        double pa = 0;
        for (int rev_num : agreementTable.keySet()) {
            double pos = 0;
            double neg = 0;
            for (Sentiment senti : agreementTable.get(rev_num).keySet()) {
                if (senti == Sentiment.POSITIVE) {
                    pos = agreementTable.get(rev_num).get(senti);
                } else if (senti == Sentiment.NEGATIVE) {
                    neg = agreementTable.get(rev_num).get(senti);
                }
            }
            double all = neg + pos;
            double innerSum = (pos * (pos - 1) + neg * (neg - 1)) / (all * (all - 1));
            pa += innerSum;
        }

        pa = (pa / N);
        double kappa = (pa - Pe) / (1 - Pe);
        return kappa;
    }


}