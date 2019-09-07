package uk.ac.cam.cl.mlrd.sz373.tick10;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
//import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise10;
public class Exercise10 implements IExercise10{
	
	/**
	 * Load the graph file. Each line in the file corresponds to an edge; the
	 * first column is the source node and the second column is the target. As
	 * the graph is undirected, your program should add the source as a
	 * neighbour of the target as well as the target a neighbour of the source.
	 * 
	 * @param graphFile
	 *            {@link Path} the path to the network specification
	 * @return {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> For
	 *         each node, all the nodes neighbouring that node
	 */
	@Override
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
	/**
	 * Find the number of neighbours for each point in the graph.
	 * 
	 * @param graph
	 *            {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
	 *            loaded graph
	 * @return {@link Map}<{@link Integer}, {@link Integer}> For each node, the
	 *         number of neighbours it has
	 */
	@Override
	public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph) {
		Map<Integer, Integer> output = new HashMap<>();
		for (Integer node : graph.keySet()) {
			if (!output.containsKey(node)) {
				int count = graph.get(node).size();
				output.put(node, count);
			}
			else {
				output.put(node,output.get(node)+1);
			}
		}
		return output;
	}
	/**
	 * Find the maximal shortest distance between any two nodes in the network 
	 * using a breadth-first search.
	 * 
	 * @param graph
	 *            {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
	 *            loaded graph
	 * @return <code>int</code> The diameter of the network
	 */
	@Override
	public int getDiameter(Map<Integer, Set<Integer>> graph) {
		int diameter = 0;
		for (Integer node : graph.keySet()) {
			Map<Integer, Integer> seen = new HashMap<>();
			Queue toExplore = new LinkedList();
			toExplore.add(node);
			seen.put(node, 0);
			int localMax = 0;
			while(!toExplore.isEmpty()) {
				Integer vertex = (Integer) toExplore.remove();
				localMax = Math.max(seen.get(vertex), localMax);
				for (Integer neighbour : graph.get(vertex)) {
					if (!seen.containsKey(neighbour)) {
						seen.put(neighbour, seen.get(vertex)+1);
						toExplore.add(neighbour);
					}
				}
			}
			diameter = Math.max(diameter, localMax);
		} 
		
		return diameter;
	}

}
