package uk.ac.cam.cl.mlrd.sz373.tick12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
/*
import uk.ac.cam.cl.mlrd.sz373.tick10.*;
import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise10;
import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise12;  */
public class Exercise12 implements IExercise12 {

	/**
     * Compute graph clustering using the Girvan-Newman method. Stop algorithm when the
     * minimum number of components has been reached (your answer may be higher than 
     * the minimum).
     * 
     * @param graph
     *        {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *        loaded graph
     * @param minimumComponents {@link int} The minimum number of components to reach.
     * @return {@link List}<{@link Set}<{@link Integer}>>
     *        List of components for the graph.
     */
	@Override
	public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents) {
		// TODO Auto-generated method stub
		List<Set<Integer>> setOfComponents = getComponents(graph);
        while (setOfComponents.size() < minimumComponents) {
            
            Map<Integer, Map<Integer, Double>> edgeCb = getEdgeBetweenness(graph);
            ArrayList<Integer> node1 = new ArrayList<>();
            ArrayList<Integer> node2 = new ArrayList<>();
            double maxOfedgeCb = 0;
            for (Entry<Integer, Map<Integer, Double>> element1: edgeCb.entrySet()) {
                for (Entry<Integer, Double> element2: element1.getValue().entrySet()) {
                    double currentCb = element2.getValue();
                    if (Math.abs(maxOfedgeCb - currentCb) < 1e-06) {
                        node1.add(element1.getKey());
                        node2.add(element2.getKey());
                    } else if (maxOfedgeCb < currentCb) {
                        maxOfedgeCb = currentCb;
                        node1.clear();
                        node2.clear();
                        node1.add(element1.getKey());
                        node2.add(element2.getKey());
                    }
                }
            }
            for (int i = 0; i < node1.size(); i++) {
                graph.get(node1.get(i)).remove(node2.get(i));
            }
            setOfComponents = getComponents(graph);
        }
        return setOfComponents;
	}
	/**
     * Find the number of edges in the graph.
     * 
     * @param graph
     *        {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *        loaded graph
     * @return {@link Integer}> Number of edges.
     */
	@Override
	public int getNumberOfEdges(Map<Integer, Set<Integer>> graph) {
		int edgeCount = 0;
        for (Set<Integer> edgeSet: graph.values()) {
            edgeCount += edgeSet.size();
        }
        
        return edgeCount/2;  // Div by 2 to avoid double counting
	}
	/**
     * Find the number of components in the graph using a DFS.
     * 
     * @param graph
     *        {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *        loaded graph
     * @return {@link List}<{@link Set}<{@link Integer}>>
     *        List of components for the graph.
     */
	@Override
	public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph) {
		Set<Integer> seen = new HashSet<>();
		List<Set<Integer>> setOfComp = new ArrayList<>();
        for (Integer vertex: graph.keySet()) {
            if (!seen.contains(vertex)) {
                Set<Integer> comp = new HashSet<>();
                helperDepthFirst(vertex, graph, seen, comp);
                
                setOfComp.add(comp);
            }
        }
        return setOfComp;
	}
	
	private void helperDepthFirst(Integer vertex, Map<Integer, Set<Integer>> myGraph, Set<Integer> seen, Set<Integer> component) {
        component.add(vertex);
        seen.add(vertex);
        for (int nextVertexToVisit: myGraph.get(vertex)) {
            if (!seen.contains(nextVertexToVisit)) {
                helperDepthFirst(nextVertexToVisit, myGraph, seen, component); // here, we're using a recursive implementation
            }
        }
    }
	
	/**
     * Calculate the edge betweenness.
     * 
     * @param graph
     *         {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *         loaded graph
     * @return {@link Map}<{@link Integer}, 
     *         {@link Map}<{@link Integer},{@link Double}>> Edge betweenness for
     *         each pair of vertices in the graph
     */
	@Override
	public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph) {
        Map<Integer, Map<Integer, Double>> edgeCb = new HashMap<>();
        Map<Integer, Set<Integer>> myGraph = graph;
        Queue<Integer> queue = new LinkedList<>();
        Stack<Integer> stack = new Stack<>();
        for (Entry<Integer, Set<Integer>> element: myGraph.entrySet()) {
            edgeCb.put(element.getKey(), new HashMap<Integer, Double>());
            for (Integer neighbour: element.getValue()) {
                edgeCb.get(element.getKey()).put(neighbour, 0.0);
            }
        }
        for (int s: myGraph.keySet()) {
            Map<Integer, Integer> dist = new HashMap<>();
            Map<Integer, List<Integer>> Pred = new HashMap<>();
            Map<Integer, Integer> count_s = new HashMap<>();
            Map<Integer, Double> Delta = new HashMap<>();
            for (int v: myGraph.keySet()) { //Initialise all
                dist.put(v, Integer.MAX_VALUE);
                Pred.put(v, new ArrayList<>());
                count_s.put(v, 0);
            }
            dist.put(s, 0);
            count_s.put(s, 1);
            queue.add(s);
            
            while (!queue.isEmpty()) {
                int v = queue.remove();
                stack.push(v);
                for (int w: myGraph.get(v)) { 
                    if (dist.get(w) == Integer.MAX_VALUE) {  //path counting
                        dist.put(w, dist.get(v) + 1);
                        queue.add(w);
                    }
                    if (dist.get(w) == dist.get(v) + 1) { //path counting
                        count_s.put(w, count_s.get(w) + count_s.get(v));
                        Pred.get(w).add(v);
                    }
                }
            }
            for (int v: myGraph.keySet()) {  
                Delta.put(v, 0d);
            }
            while (!stack.isEmpty()) { // Accumulation via back-propagation
                int w = stack.pop();
                for (int v: Pred.get(w)) {
                    double c = ((double)count_s.get(v) / (double)count_s.get(w)) * (1d + Delta.get(w));
                    edgeCb.get(v).put(w, edgeCb.get(v).get(w) + c);
                    Delta.put(v, Delta.get(v) + c);
                }
            }
        }

        return edgeCb;
	}

}
