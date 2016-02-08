package main.java.hdbscanstar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.princeton.cs.algorithms.EdgeWeightedGraph;
import edu.princeton.cs.algorithms.BoruvkaMST;
import edu.princeton.cs.algorithms.Edge;


public class HDBSCAN {
	
	public static NearestKdTree calculateNearestKdTree(Coordinate[] points,int k,double tolerance){
		NearestKdTree tree = new NearestKdTree(points,k,tolerance);
		tree.findKNN();
		return tree;
		
	}
	
	public static EdgeWeightedGraph calculateWeightedGraph(ArrayList<KdNode> nodes){
		HashSet<MutualReachabilityEdge> mrEdges = new HashSet();
		int numNodes = nodes.size();
		long startTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		for(KdNode node : nodes){
//			if(node.getLabel() > 110 && node.getLabel() < 125){
//			}
						
			
			for(KdNode other : node.getNeighbors()){
				if(node != null && other != null){
					MutualReachabilityEdge mrEdge = new MutualReachabilityEdge(node, other);
					mrEdges.add(mrEdge);
				}
			}
		}
		System.out.println("Time compute edges: " + (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		nodes = null;
		EdgeWeightedGraph ewg = new EdgeWeightedGraph(numNodes);
		for(MutualReachabilityEdge e : mrEdges){
			ewg.addEdge(new Edge(e.getLabel1(),e.getLabel2(),e.getMrDistance()));
		}
		System.out.println("Time add edges to ewg: " + (System.currentTimeMillis() - startTime));
//		System.out.println(ewg.toString());
		return ewg;
		
	}
	
	public static BoruvkaMST createMST(EdgeWeightedGraph ewg){
		return new BoruvkaMST(ewg);
	}
	
	public static void createMstWKT(BoruvkaMST mst,ArrayList<KdNode> nodes){
		GeometryFactory gf = new GeometryFactory(new PrecisionModel(),4326);
		try{
			File file = new File("testWkt.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("v1,v2,weight,wkt");
			for(Edge e : mst.edges()){
				int v1 = e.either();
				int v2 = e.other(v1);
				Coordinate point1 = nodes.get(v1).getCoordinate();
				Coordinate point2 = nodes.get(v2).getCoordinate();
				Coordinate[] coords = {point1,point2};
				bw.write("\n\"" + v1 + "\"" + "," + "\"" + v2 + "\"" + "," +
						"\"" + e.weight() + "\"" + "," +"\"" + gf.createLineString(coords) + "\"");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
//	public EdgeWeightedGraph calculateWeightedGraph(double tol){
//		EdgeWeightedGraph ewg = new EdgeWeightedGraph(points.length);
//		NearestKdTree kd = new NearestKdTree(tol);
//		HashMap<Integer,Double> coreDistances = new HashMap();
//		HashMap<String,Edge> potentialEdges = new HashMap();
//		
//		for(int i = 0; i < points.length; i++){
//			kd.insert(points[i],i);
//		}
//		for(int i = 0; i < points.length; i++){
//			Coordinate point = points[i];
//			HashSet<Coordinate> exclude = new HashSet();
//			exclude.add(point);
//			for(int j=1;j<=k;j++){
//				CoordinatePair nearestNeighbor = kd.nearestPair(point, exclude);
//				System.out.println(nearestNeighbor);
//				int v1 = i;
//				int v2 = (int) nearestNeighbor.getNode2().getData();
//				double distance = nearestNeighbor.getDistance();
//				if(v1==v2) continue;
//				potentialEdges.put((v1 < v2 ? String.valueOf(v1) + "_" + String.valueOf(v2) : String.valueOf(v2) + "_" + String.valueOf(v1) ),
//						new Edge(v1,v2,distance));
//				if(i==k){
//					coreDistances.put(i, nearestNeighbor.getDistance());
//				}
//			}
//		}
//		
//		System.out.println(potentialEdges.size());
//		
//		for(Edge currEdge : potentialEdges.values()){
//			int v1 = currEdge.either();
//			int v2 = currEdge.other(v1);
//			double dist1 = coreDistances.get(v1);
//			double dist2 = coreDistances.get(v2);
//			double mutualReachabilityDistance = (dist1 >= dist2 ? dist1 : dist2);
//			ewg.addEdge(new Edge(v1,v2,mutualReachabilityDistance));
//		}
//		
//		return ewg;
//	}
//	
	/**
	 * Reads in the input data set from the file given, assuming the delimiter separates attributes
	 * for each data point, and each point is given on a separate line.  Error messages are printed
	 * if any part of the input is improperly formatted.
	 * @param fileName The path to the input file
	 * @param delimiter A regular expression that separates the attributes of each point
	 * @return A double[][] where index [i][j] indicates the jth attribute of data point i
	 * @throws IOException If any errors occur opening or reading from the file
	 */
	public static Coordinate[] readInDataSet(String fileName, String delimiter) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		ArrayList<double[]> dataSet = new ArrayList<double[]>();
		int numAttributes = -1;
		int lineIndex = 0;
		String line = reader.readLine();

		while (line != null) {
			lineIndex++;
			String[] lineContents = line.split(delimiter);

			if (numAttributes == -1)
				numAttributes = lineContents.length;
			else if (lineContents.length != numAttributes)
				System.err.println("Line " + lineIndex + " of data set has incorrect number of attributes.");

			double[] attributes = new double[numAttributes];
			for (int i = 0; i < numAttributes; i++) {
				try {
					//If an exception occurs, the attribute will remain 0:
					attributes[i] = Double.parseDouble(lineContents[i]);
				}
				catch (NumberFormatException nfe) {
					System.err.println("Illegal value on line " + lineIndex + " of data set: " + lineContents[i]);
				}
			}

			dataSet.add(attributes);
			line = reader.readLine();
		}

		reader.close();
		Coordinate[] finalDataSet = new Coordinate[dataSet.size()];

		for (int i = 0; i < dataSet.size(); i++) {
			double[] point = dataSet.get(i);
			finalDataSet[i] = new Coordinate(point[0],point[1]);
		}
		return finalDataSet;
	}
	 public static void main(String[] args) {
		try{
			Coordinate[] data = readInDataSet("testData.csv", ",");
			long startTime = System.currentTimeMillis();
			NearestKdTree tree = calculateNearestKdTree(data, 32, 0.001);
			System.out.println("Time to calculate NN: " + (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();
			ArrayList<KdNode> nodes = tree.getAllNodes();
			Collections.sort(nodes);
			EdgeWeightedGraph ewg = calculateWeightedGraph(nodes);
			System.out.println("Time to create Edge Weighted Graph: " + (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();
			BoruvkaMST mst = new BoruvkaMST(ewg);
			System.out.println("Time to create Minimum Spanning Tree: " + (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();
			createMstWKT(mst,nodes);
			System.out.println("Write MST to WKT: " + (System.currentTimeMillis() - startTime));

		}catch(IOException e){
			System.out.println(e);
		}
	}
}
