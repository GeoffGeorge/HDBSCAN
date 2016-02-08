package com.vividsolutions.jts.index.kdtree;

import java.util.List;
import java.util.ListIterator;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.index.ArrayListVisitor;

/**
 * Encapsulates the nearest neighbor search logic for the case where the 
 * nearest neighbor cannot be the search point itself. This helper class is 
 * used internally by {@link NearestKdTree} and is not envisioned to be 
 * generally useful in client code.
 * 
 * @author Bryce Nordgren
 * @since 1.12
 * @see NearestKdTree
 * @see NearestNotInSearch
 * @see NearestSearch
 *
 */
public class NearestNonIdenticalSearch extends Search {
	
	public NearestNonIdenticalSearch(KdNode _root) { 
		super(_root) ; 
	}

	/**
	 * Searches for the nearest point in the tree which is not the 
	 * point itself.
	 * 
	 * @param search the point to search for.
	 * @return search point, nearest neighbor, and separation distance
	 */
	public CoordinatePair nearestPair(Coordinate search) {
		return nearest(getRoot(),search) ; 
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
	 * Implements the nearest neighbor search algorithm for the case where 
	 * the nearest neighbor cannot be the search point itself. (RECURSIVE)
	 * @param start node at which to begin the search
	 * @param searchPt point to search for 
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

			// ensure that current point is fair game
			if (!searchPt.equals2D(current.getCoordinate()) ) {
				// initialize the "best" pointer if necessary
				if (best == null) {
					best = current ; 
					min_dist = searchPt.distance(best.getCoordinate()) ; 
				} else {
					cur_dist = searchPt.distance(current.getCoordinate()) ;
					if (min_dist > cur_dist) {
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
					CoordinatePair childPair = nearest(child, searchPt) ;

					// see if the best match on that side is better.
					if (childPair != null && !searchPt.equals2D(childPair.getPoint2())) { 
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
