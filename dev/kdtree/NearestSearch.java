package com.vividsolutions.jts.index.kdtree;

import java.util.List;
import java.util.ListIterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.index.ArrayListVisitor;

/**
 * Implements the nearest neighbor search logic for a KdTree. This helper class
 * is used internally by {@link NearestKdTree}, and is not envisioned to be 
 * generally useful to client code.
 * 
 * @author Bryce Nordgren
 * @since 1.12
 * @see NearestNotInSearch
 * @see NearestNonIdenticalSearch
 * @see NearestKdTree
 *
 */
public class NearestSearch extends Search {
	
	public NearestSearch(KdNode root) { 
		super(root) ; 
	}
	
	/**
	 * Returns the nearest neighbor as a coordinate only. 
	 * @param search the point to search for
	 * @return point in the tree nearest to the search point.
	 */
	public Coordinate nearest(Coordinate search) { 
		CoordinatePair val = nearestPair(search) ;
		if (val != null) {
			return val.getPoint2() ; 
		} else {
			return null ;
		}
	}

	/**
	 * Searches for the nearest neighbor and returns the result as a 
	 * {@link CoordinatePair}. This contains the search point, the result
	 * point, and the separation distance.
	 * @param search the point to search for
	 * @return search point, nearest neighbor, and separation distance
	 */
	public CoordinatePair nearestPair(Coordinate search) {
		return nearest(getRoot(),search) ; 
	}
	
	/**
	 * Implements the nearest neighbor search algorithm (recursive).
	 * @param start The node at which to begin the search
	 * @param searchPt the point to search for.
	 * @return search point, nearest neighbor, and separation distance
	 */
	protected static CoordinatePair nearest(KdNode start, Coordinate searchPt) { 
		ArrayListVisitor v = new ArrayListVisitor() ; 
		
		traverse(start, searchPt, v) ;
		List path = v.getItems() ;
		
		ListIterator unwind = path.listIterator(path.size()) ;
		KdNode best = null ; 
		double min_dist = Double.NaN; 
		double cur_dist ; 
		while (unwind.hasPrevious()) {
			KdNode current = ((KdNode)(unwind.previous())) ; 
			
			// initialize the "best" pointer if necessary
			if (best == null) {
				best = current ; 
				min_dist = searchPt.distance(best.getCoordinate()) ; 
			} else { 

				// check current point to see if its closer than best-so-far
				cur_dist = searchPt.distance(current.getCoordinate()) ;
				if (min_dist > cur_dist) {
					best = current ; 
					min_dist = cur_dist ; 
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
					CoordinatePair childPair = nearest(child, searchPt) ;

					if (childPair != null) { 
						// see if the best match on that side is better.
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
