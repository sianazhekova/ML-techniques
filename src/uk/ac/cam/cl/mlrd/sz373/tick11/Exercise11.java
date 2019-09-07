package uk.ac.cam.cl.mlrd.sz373.tick11;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
//import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise11;
public class Exercise11 implements IExercise11{
	
	public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {
		String fileLine;
		BufferedReader buffer = new BufferedReader(new FileReader(graphFile.toString()));
		Map<Integer, Set<Integer>> mapOfNodesEdges = new HashMap<>();
		while((fileLine = buffer.readLine())!=null) {
			//System.out.println(fileLine);
			int end = fileLine.indexOf(" ");
			int source = Integer.parseInt(fileLine.substring(0, end));
			Integer target = Integer.parseInt(fileLine.substring(end + 1));
			//Set<Integer> tempSet = new HashSet<>();
			if (!mapOfNodesEdges.containsKey(source)) {
				Set<Integer> tempSet = new HashSet<>();
				tempSet.add(target);
				mapOfNodesEdges.put(source, tempSet);
			}
			if(mapOfNodesEdges.containsKey(source)) {   // the Set properties ensure that duplicates are ignored
				(mapOfNodesEdges.get(source)).add(target);
				//System.out.println(mapOfNeighbours.get(source));
			}
			if(!mapOfNodesEdges.containsKey(target)) {
				Set<Integer> tempSet = new HashSet<>();
				tempSet.add(source);
				mapOfNodesEdges.put(target, tempSet);
			}
			if(mapOfNodesEdges.containsKey(target)) {
				mapOfNodesEdges.get(target).add(source);
			}	
		}
		return mapOfNodesEdges;
	}
	
	@Override
	public Map<Integer, Double> getNodeBetweenness(Path graphFile) throws IOException {
		// Data needed for Brandes' algorithm:
		Map<Integer, Set<Integer>> myGraph = loadGraph(graphFile);
		int node1,node2;
		Stack<Integer> S = new Stack<>();
		Queue<Integer> Q = new LinkedList<>();
		List<List<Integer>> Pred = new ArrayList<>();
		List<Integer> dist = new ArrayList<>();
        List<Integer> count_s = new ArrayList<>();
        List<Double> Delta = new ArrayList<>();
        Map<Integer, Double> Cb_OfV = new HashMap<>();
        for(Integer vertex : myGraph.keySet()) {  // Initialise Betweenness Centrality Map
        	Cb_OfV.put(vertex, 0.0);
        }
        for (Integer s : myGraph.keySet()) {
        	S = new Stack<>();
        	Q = new LinkedList<>();
        	Q.add(s);
        	Pred = new ArrayList<>();
        	for(int i = 0; i <= myGraph.size(); i++) {
        		Pred.add(new ArrayList<Integer>());
        	}
        	dist = new ArrayList<>();
        	for (int i = 0; i <= myGraph.size(); i++) {
        		dist.add(Integer.MAX_VALUE);
        	}
        	dist.set(s, 0);
        	count_s = new ArrayList<>();
        	for (int i = 0; i <= myGraph.size(); i++) {
        		count_s.add(0);
        	}
        	count_s.set(s, 1);
        	
        	while(!Q.isEmpty()) {
        		Integer v = Q.remove();
        		S.push(v);
        		for (Integer w : myGraph.get(v)) {
        			if (dist.get((int)w) == Integer.MAX_VALUE) { //path discovery
        				dist.set(w, dist.get((int)v) + 1);
        				Q.add(w);
        			}
        			if (dist.get(w) == dist.get(v) + 1) {  //path counting
        				count_s.set(w, count_s.get(w) + count_s.get(v));
        				Pred.get(w).add(v);
        			}
        		}
        	}  // Accumulation --> back-propagate via dependencies
        	Delta = new ArrayList<>();
        	for (int i = 0; i <= myGraph.size(); i++) {Delta.add(0.0);}
        	while (!S.isEmpty()) {
        		Integer w = S.pop();
        		for (int v :Pred.get(w)) {
        			Delta.set(v, Delta.get(v)+( ( (double)count_s.get(v) / (double)count_s.get(w) ) * (1.0 + Delta.get(w)) ) );
        		}
        		if (w != s) {
        			Cb_OfV.put(w, Cb_OfV.get(w)+Delta.get(w));
        		}
        	}
        	
        	
        	
        	
        }
        for (int v : myGraph.keySet()) {
        	Cb_OfV.put(v, Cb_OfV.get(v)/2.0);
        }

			
		

        return Cb_OfV;

	}

}
