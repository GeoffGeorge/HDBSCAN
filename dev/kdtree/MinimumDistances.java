package com.vividsolutions.jts.index.kdtree;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator ;
import java.util.List;


/**
 * <p>Provides static methods to compute a vector of nearest neighbor
 * distances. <b>Do not create instances of this class!</b> The static methods 
 * {@link #minClusterDistance(NearestKdTree, List)} and 
 * {@link #minClusterDistance(NearestKdTree, List, int)} encapsulate the algorithms 
 * used to locate the nearest neighbor and compute distance to it. The former 
 * performs all work in the current thread, and the latter divides work among the 
 * specified number of threads. Both are cluster-based, returning the nearest 
 * neighbor <i>which is not in the same cluster</i>. The 
 * {@link #minDistance(NearestKdTree)} method is not cluster-based. It returns 
 * the vector of nearest neighbors for each point in the {@link NearestKdTree}.</p>
 * 
 * <p>
 * Instances of this class are reserved for use as workers, hence they inherit
 * from {@link Thread}. The static methods act as factories, producing instances 
 * of this class for internal use only. They do not return the instances, and the 
 * multithreaded nature of the method's internals is hidden from the caller.
 * </p>
 * 
 * <p>
 * The nearest neighbor is always reported as a {@link CoordinatePair}. Point 1
 * in the pair is the point which was searched <i>for</i>, and point 2 
 * is the nearest neighbor. For the cluster-based methods, point 1 is always 
 * a member of the source cluster, and point 2 is always a member of a different 
 * cluster. For the non-cluster method, point 1 and 2 are guaranteed to be different.
 * The distance between the two points in the <code>CoordinatePair</code> is
 * cached, and may be retrieved by the <code>getDistance()</code> method.
 * </p>
 * 
 * <p>As with SingleLinkageCluster, the list of clusters is expected to
 * be a {@link List} of {@link Collection}s which contain {@link Coordinate}s.
 * </p>
 * 
 * @author Bryce Nordgren
 *
 */
public class MinimumDistances extends Thread {
	
	private NearestKdTree tree ; 
	private List          clusters ; 
	private CoordinatePair [][]result = null ; 
	
	private MinimumDistances(NearestKdTree _tree, List _clusters, 
			ThreadGroup parent, String name) {
		super(parent,name) ; 
		tree = _tree ; 
		clusters = _clusters ; 
	}
	
	/**
	 * Computes the nearest neighbor for every point in the tree. This 
	 * is not a cluster-based method. It simply searches for the nearest
	 * point which is not the search point.
	 * 
	 * @param tree the tree of points
	 * @return a vector of nearest neighbors
	 */
	public static CoordinatePair []minDistance(NearestKdTree tree) {
		CoordinatePair []distances = new CoordinatePair[(int)(tree.size())] ;
		
		Iterator treeIter = tree.iterator() ; 
		
		int i=0 ; 
		while (treeIter.hasNext()) {
			KdNode current = (KdNode)(treeIter.next()) ; 
			distances[i] = tree.nearestNotIdenticalPair(current.getCoordinate()) ;
			distances[i].setNode1(current) ; 
			i++ ; 
			if ((i%10)==0) { 
				System.out.print(i) ; 
				System.out.println(" nodes processed.") ;
			}
		}
		
		return distances ; 
	}
	
	/**
	 * <p>Computes the nearest neighbor for every point in every cluster. 
	 * The nearest neighbor is guaranteed to not be a member of the same
	 * cluster as the search point.</p>
	 * 
	 * <p>This method is single-threaded. For a multithreaded version, see
	 * {@link #minClusterDistance(NearestKdTree, List, int)}.}
	 * 
	 * @param tree the tree to seach for nearest neighbors: must contain all
	 * points in all clusters!!!
	 * @param clusters A list of clusters (which must be <code>Collection</code>s).
	 * @return a 2D array of nearest neighbors. The first index is the cluster
	 * index of the source point. The second index refers to a particular point
	 * inside the cluster. Note that if the iterator for the collection does not 
	 * guarantee consistent ordering, this index will be meaningless. However,
	 * the Point1 attribute will always refer to the search point.
	 */
	public static CoordinatePair[][] minClusterDistance(
			NearestKdTree tree, List clusters) {
		CoordinatePair [][]distances = new CoordinatePair[clusters.size()][] ;
		
		for (int i=0; i<clusters.size(); i++) { 
			distances[i] = tree.nearestNotInPairs((Collection)(clusters.get(i))) ; 
		}
		
		return distances ; 
	}
	
	private static MinimumDistances[] divideWork(
			NearestKdTree tree, List clusters, int threads) { 
		// divvy up the work
		ThreadGroup calculators = new ThreadGroup("calculators") ; 
		MinimumDistances []workers = new MinimumDistances[threads] ; 
		int worker_len = clusters.size() / threads ; 
		int start_i = 0;
		int thread = 0 ; 
		while (thread < threads) { 
			int end_i = start_i + worker_len ;
			
			// the last thread gets all remaining clusters
			if (thread == (threads-1)) {
				end_i = clusters.size() ; 
			}
			
			List subCluster = Collections.unmodifiableList(
					clusters.subList(start_i, start_i+worker_len));
			String name = "calculator".concat(Integer.toString(thread)) ; 
			MinimumDistances calc = 
				new MinimumDistances(tree, clusters, calculators, name) ;
			calc.start() ; 		
			workers[thread] = calc ; 
			
			// counter overhead
			start_i = end_i ; 
			thread ++ ; 
		}
		return workers ;
	}
	
	private static void waitOnWorkers(MinimumDistances []workers) 
	    throws InterruptedException { 
		
		ThreadGroup calculators = workers[0].getThreadGroup() ; 
		
		int numActive = calculators.activeCount() ; 
		while (numActive > 0) { 
			Thread.sleep(4000) ; // sleep four seconds before checking again
			numActive = calculators.activeCount() ; 
		}
	}
	
	private static CoordinatePair[][] collectResults(MinimumDistances []workers) { 
		int total = 0 ; 
		for (int i=0; i<workers.length; i++) { 
			total += workers[i].getResult().length ; 
		}
		
		CoordinatePair [][]consolidated = new CoordinatePair[total][] ; 
		
		int out_i = 0 ; 		
		for (int worker=0; worker<workers.length; worker++) { 
			CoordinatePair [][]current = workers[worker].getResult() ; 
			for (int i=0; i<current.length; i++) {
				consolidated[out_i] = new CoordinatePair[current[i].length] ;
				for (int j=0; j<current[i].length; j++) { 
					consolidated[out_i][j] = current[i][j] ; 
				}
				out_i ++ ; 
			}
		}
		
		return consolidated ; 
	}


	
	/**
	 * <p>Computes the nearest neighbor for every point in every cluster. 
	 * The nearest neighbor is guaranteed to not be a member of the same
	 * cluster as the search point.</p>
	 * 
	 * <p>This method is multi-threaded. For a single-threaded version, see
	 * {@link #minClusterDistance(NearestKdTree, List)}, or supply 1 as the 
	 * number of threads.</p>
	 * 
	 * @param tree the tree to seach for nearest neighbors: must contain all
	 * points in all clusters!!!
	 * @param clusters A list of clusters (which must be <code>Collection</code>s).
	 * @param threads the number of threads over which to divide the work.
	 * @return a 2D array of nearest neighbors. The first index is the cluster
	 * index of the source point. The second index refers to a particular point
	 * inside the cluster. Note that if the iterator for the collection does not 
	 * guarantee consistent ordering, this index will be meaningless. However,
	 * the Point1 attribute will always refer to the search point.
	 * 
	 * @throws InterruptedException if the user interrupts one or more of the 
	 * workers.
	 */
	public static CoordinatePair[][] minClusterDistance(
			NearestKdTree tree, List clusters, int threads) 
		throws InterruptedException { 
		
		if (threads == 1) { 
			return minClusterDistance(tree, clusters) ;
		}
		
		MinimumDistances[] calculators = divideWork(tree, clusters, threads) ; 
		
		CoordinatePair [][]retval = null ;
		// wait till workers are all done
		waitOnWorkers(calculators) ;

		retval = collectResults(calculators) ; 

		return retval ; 
		
	}

	private CoordinatePair[][] getResult() {
		return result;
	}

	/**
	 * You don't see this. It's not here. Never use it. Never make an 
	 * instance of this class. Move along.
	 */
	public void run() {
		result = minClusterDistance(tree, clusters) ; 
	}

}
