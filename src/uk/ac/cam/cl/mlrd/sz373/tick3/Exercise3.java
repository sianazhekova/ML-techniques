package uk.ac.cam.cl.mlrd.sz373.tick3;
//package uk.ac.cam.cl.mlrd.exercises.sentiment_detection;

import uk.ac.cam.cl.mlrd.sz373.tick1.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import uk.ac.cam.cl.mlrd.sz373.tick3.ChartPlotter;
import uk.ac.cam.cl.mlrd.sz373.tick3.BestFit;
import java.io.File;
import java.util.stream.Collectors;


public class Exercise3 {

    static final Path dataDirectory = Paths.get("data/large_dataset");


    public Map<String, Integer> measureFrequencies(Set<Path> trainingSet)throws IOException {
        Map<String,Integer> wordFrequencies = new HashMap<>();
        for (Path p : trainingSet){
            List<String> reviewTokens = Tokenizer.tokenize(p);
            for(String token : reviewTokens) {
                if (!wordFrequencies.containsKey(token)) {
                    wordFrequencies.put(token, 1);
                }
                wordFrequencies.put(token, wordFrequencies.get(token) + 1);
            }
        }
        Map<String,Integer> sortedMap =
                wordFrequencies.entrySet().stream()
                        .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return sortedMap;
    }

    public Map<String, Integer> measureRank(Map<String, Integer> sortedWordFrequencies){
        Map<String,Integer> wordRanks = new HashMap<>();
        int i = 1;
        for(String s: sortedWordFrequencies.keySet()){
            wordRanks.put(s,i);
            i++;
        }
        return wordRanks;
    }

    public List<BestFit.Point> plotFreqvsRank(Map<String, Integer> wordFreqs, Map<String, Integer> wordRanks){
        Collection<Integer> freqs = wordFreqs.values();
        List<BestFit.Point> points = new ArrayList<>();
        int i = 0;
        for(String s : wordFreqs.keySet()){
            if (i >= 10000){
                break;
            }
            points.add((new BestFit.Point((double) wordRanks.get(s),(double) wordFreqs.get(s))));
            i++;
        }
        return (points);
    }

    public List<BestFit.Point> plotChosenWords(Map<String, Integer> wordFreqs, Map<String, Integer> wordRanks ){
        Set<String> words = new HashSet<>();
        List<BestFit.Point> points = new ArrayList<>();
        words.add("bland");
        words.add("lacking");
        words.add("annoying");
        words.add("dry");
        words.add("classic");
        words.add("interesting");
        words.add("charm");
        words.add("emotional");
        words.add("potential");
        words.add("fun");
        for(String s : words){
            points.add((new BestFit.Point((double) wordRanks.get(s),(double) wordFreqs.get(s))));
        }
        return (points);
    }

    public static  List<BestFit.Point> logAll( List<BestFit.Point> points){
        List<BestFit.Point> loggedPoints = new ArrayList<>();
        for(BestFit.Point p : points){
            loggedPoints.add(new BestFit.Point(Math.log(p.x),Math.log(p.y)));
        }
        return(loggedPoints);
    }

    public static int getIndex(Set<String> set, String value) {
        int result = 0;
        for (Object entry:set) {
            if (entry.equals(value)) return result;
            result++;
        }
        return -1;
    }

    public Map<BestFit.Point, Double> weightedLoggedPointCalculator( Map<String,Integer> wordFreqs,  Map<String,Integer> wordRanks){
        Collection<Integer> freqs = wordFreqs.values();
        Map<BestFit.Point, Double> points = new HashMap<>();
        int i = 0;
        for(String s : wordFreqs.keySet()){
            if (i >= 10000){
                break;
            }
            points.put((new BestFit.Point(Math.log(wordRanks.get(s)), Math.log(wordFreqs.get(s)))),(double) wordFreqs.get(s));
            i++;
        }
        return (points);
    }

    public double expectedFreq (BestFit.Line line , int rank){
        double freq = line.gradient*Math.log(rank)+line.yIntercept;
        return(Math.exp(freq));
    }

    public static double logb( double a, double b )
    {
        return Math.log(a) / Math.log(b);
    }

    public double calcZipfK (BestFit.Line line){
        double k = Math.exp(line.yIntercept);
        return (k);
    }

    public double calcZipfAlpha(BestFit.Line line ){
        double alpha = -line.gradient;
        return alpha;
    }


    public  List<BestFit.Point> countUniqueWords(Set<Path> trainingSet) throws IOException{
        List<String> types = new ArrayList<>();
        List<String> foundTokens = new ArrayList<>();
        List<BestFit.Point> datapoints = new ArrayList<>();
        int i = 1;
        for (Path p : trainingSet){
            List<String> reviewTokens = Tokenizer.tokenize(p);
            for(String token : reviewTokens) {
                if (!types.contains(token)) {
                    types.add(token);
                }
                foundTokens.add(token);
                if ((i&(i-1)) == 0){
                    datapoints.add(new BestFit.Point(Math.log(types.size()),Math.log(foundTokens.size())));
                }
                i++;
            }
        }
        datapoints.add(new BestFit.Point(Math.log(types.size()),Math.log(foundTokens.size())));
        return(datapoints);
    }

    public static void main(String[] args) throws IOException{
        DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory);
        Set<Path> reviewSet = new HashSet<>();
        for(Path p: files){
            reviewSet.add(p);
        }
        Exercise3 Ex3 = new Exercise3();

        Map<String,Integer> wordFreqs = Ex3.measureFrequencies(reviewSet);
        Map<String,Integer> wordRanks = Ex3.measureRank(wordFreqs);
        List<BestFit.Point> points1 = Ex3.plotFreqvsRank(wordFreqs,wordRanks);
        List<BestFit.Point> points2 = Ex3.plotChosenWords(wordFreqs,wordRanks);
        ChartPlotter.plotLines(points1);
        ChartPlotter.plotLines(points2);

        Map<BestFit.Point, Double> weightedPoints = Ex3.weightedLoggedPointCalculator(wordFreqs,wordRanks);

        BestFit.Line line = BestFit.leastSquares(weightedPoints);
        BestFit.Point point1 = new BestFit.Point(0,line.yIntercept);
        BestFit.Point point2 = new BestFit.Point(Math.log(10000), line.gradient*Math.log(10000)+line.yIntercept);  //
        List<BestFit.Point> points3 = new ArrayList<>();
        points3.add(point1);
        points3.add(point2);
        ChartPlotter.plotLines(logAll(points1), points3);
        System.out.println(line.toString());
        System.out.println(Ex3.calcZipfK(line));
        System.out.println(Ex3.calcZipfAlpha(line));
        //List<BestFit.Point> points3 = Ex3.countUniqueWords(reviewSet);
        ChartPlotter.plotLines(points3);


        System.out.println("Done");

        }
    }