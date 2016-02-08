package com.vividsolutions.jts.index.kdtree;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.index.ArrayListVisitor;

/**
 * Implements the nearest neighbor search logic, where members of a
 * collection of points are excluded from consideration as the nearest 
 * neighbor. This helper class is used internally by {@link NearestKdTree}, and 
 * is not envisioned to be generally useful to client code.
 * 
 * @author Bryce Nordgren
 * @since 1.12
 * @see NearestKdTree
 * @see NearestNonIdenticalSearch
 * @see NearestSearch
 *
 */
public class NearestNotInSearch extends Search {
	
	public NearestNotInSearch(KdNode _root) { 
		super(_root) ; 
	}

	/**
	 * Searches for the nearest neighbor which is not present in the 
	 * exclude collection.
	 * @param search point to search for
	 * @param exclude points which cannot be nearest neighbors.
	 * @return search point, nearest neighbor, and separation distance
	 */
	public CoordinatePair nearestPair(Coordinate search, Collection exclude) {
		return nearest(getRoot(),search, exclude) ; 
	}
	

	/**
	 * Searches for the nearest point outside the cluster for every point in the 
	 * cluster. 
	 * @param cluster the set of points which comprise the cluster.
	 * @return an array of inside-outside points
	 */
	public CoordinatePair[] nearestPairs(Collection cluster) {
		if (cluster==null) return null ;
		
		CoordinatePair[] retval = new CoordinatePair[cluster.size()] ; 
		
		Iterator clusterIter = cluster.iterator() ;
		int i=0 ; 
		while (clusterIter.hasNext()) { 
			Coordinate current = (Coordinate)(clusterIter.next()) ;
			retval[i] = nearestPair(current, cluster) ; 
			i++ ; 
		}
		
		return retval ; 
	}
	
	/**
	 * Searches for the nearest neighbor to the indicated point, excluding from
	 * consideration all points listed in the exclude collection. (Recursive)
	 * @param start location in the tree to start searching.
	 * @param searchPt point to search for
	 * @param exclude collection of points which cannot be the nearest neighbor.
	 * @return search point, nearest neighbor, and separation distance.
	 */
	protected static CoordinatePair nearest(KdNode start, Coordinate searchPt, 
			Collection exclude) { 
		ArrayListVisitor v = new ArrayListVisitor() ; 
		
		traverse(start, searchPt, v) ;
		List path = v.getItems() ;
		
		ListIterator unwind = path.listIterator(path.size()) ;
		KdNode best = null ; 
		double min_dist = Double.NaN; 
		double cur_dist ; 
		while (unwind.hasPrevious()) {
			KdNode current = ((KdNode)(unwind.previous())) ; 
			
			// check that the current node is "fair game"
			if (!exclude.contains(current.getCoordinate())) {
				// initialize the "best" pointer if necessary
				if (best == null) {
					best = current ; 
					min_dist = searchPt.distance(best.getCoordinate()) ; 					
				} else { 
					cur_dist = searchPt.distance(current.getCoordinate()) ;
					if ((min_dist > cur_dist) || Double.isNaN(min_dist)) {
						best = current ; 
						min_dist = cur_dist ; 
					}					
				}
			}
				
			// check if it's possible that a closer point may be on 
			// the other branch of the tree...
			Coordinate projection = current.projectPoint(searchPt) ; 
			if ((searchPt.distance(projection) < min_dist) || Double.isNaN(min_dist)) {
				// determine which is the "other" branch
				KdNode child = otherChild(current, searchPt) ; 

				if (child != null) { 
					// search the "other" children of this node
					CoordinatePair childPair = nearest(child, searchPt, exclude) ;

					// see if the best match on that side is better.
					// note that "nearest" won't return a point in the exclude list.
					if (childPair != null) { 
						cur_dist = childPair.getDistance() ;
						if ((min_dist > cur_dist) || Double.isNaN(min_dist)) { 
							best = childPair.getNode2() ; 
							min_dist = cur_dist ; 
						}
					}
				} 
			}			
		}	

		CoordinatePair bestMatch = null ; 
		
		if (best != null) {
			bestMatch = new CoordinatePair(searchPt, best.getCoordinate(),min_dist) ; 
			bestMatch.setNode2(best) ; 
		}
		
		return bestMatch;		
	}
	

}
