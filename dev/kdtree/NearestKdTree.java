package com.vividsolutions.jts.index.kdtree;

import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.CoordinateIndex;

/**
 * <p>A {@link KdTree} with methods to query for the nearest neighbor to a
 * specified search point. There are three current strategies for determining
 * the nearest neighbor:</p>
 * 
 * <ol>
 * <li> The point in the tree nearest to the search point. (The search point
 *      need not exist in the tree.)</li>
 * <li> The point in the tree nearest to the search point which is not 
 *      identical to the search point. (Used to locate nearest neighbors 
 *      to points which are in the tree.)</li>
 * <li> The point in the tree nearest to the search point which is not
 *      contained within a specified <code>Collection</code> of points. (Used to 
 *      locate nearest neighbors which are not part of the same cluster.)</li>
 * </ol>
 * 
 * <p>Range searches are inherited from {@link KdTree}, as are all the mutable
 * behaviors (such as adding points).</p>
 * 
 * @author Bryce Nordgren
 * @since 1.12
 * @see KdTree
 * @see NearestSearch
 * @see NearestNonIdenticalSearch
 * @see NearestNotInSearch
 *
 */
public class NearestKdTree extends KdTree implements CoordinateIndex {
	
	private NearestSearch searchAlgorithm = null ;
	private NearestNonIdenticalSearch nonIdentical = null ; 
	private NearestNotInSearch notIn   = null ; 
	
	protected NearestKdTree(KdNode root) { 
		super(root, 0) ;
		searchAlgorithm = new NearestSearch(root) ; 
		nonIdentical = new NearestNonIdenticalSearch(root) ; 
		notIn = new NearestNotInSearch(root) ; 
	}
	
	/**
	 * <p>Creates an empty <code>NearestKdTree</code>.</p>
	 * 
	 * <p><b>NOTE:</b> if you already have all or most of the points you
	 * intend to store in this tree, it is more efficient to use the 
	 * factory method: {@link #loadNearestKdTree(Coordinate[])}.</p>
	 */
	public NearestKdTree() { 
		super() ; 
		searchAlgorithm = new NearestSearch(getRoot()) ;
		nonIdentical = new NearestNonIdenticalSearch(getRoot()) ; 
		notIn = new NearestNotInSearch(getRoot()) ; 
	}
	
	/**
	 * Creates an empty KdTree with the specified snap tolerance. See 
	 * {@link KdTree#KdTree(double)} for more details.
	 * 
	 * @param tol the snap tolerance
	 */
	public NearestKdTree(double tol) { 
		super(tol) ; 
		searchAlgorithm = new NearestSearch(getRoot()) ;
		nonIdentical = new NearestNonIdenticalSearch(getRoot()) ; 
		notIn = new NearestNotInSearch(getRoot()) ; 
	}

	/**
	 * Performs a range search on the envelope and visits each of the 
	 * results.
	 * 
	 * @param searchEnv the envelope to search
	 * @param visitor the {@link ItemVisitor} which should visit each result.
	 */
	public void query(Envelope searchEnv, ItemVisitor visitor) {
		List results = query(searchEnv) ; 
		Iterator visitator = results.iterator() ; 
		while (visitator.hasNext()) { 
			visitor.visitItem(visitator.next()) ; 
		}		
	}

	/** 
	 * not implemented. always returns false.
	 */
	public boolean remove(Coordinate itemEnv, Object item) {
		return false;
	}
	
	/**
	 * Searches the tree for the specified point. 
	 * @param p the point to look for
	 * @return true if the point is in the tree, false if not
	 */
	public boolean contains(Coordinate p) { 
		return searchAlgorithm.contains(p) ; 
	}

	/**
	 * Returns the path from the root of the tree to the specified coordinate.
	 * This method is mostly used for unit testing and debugging. It may have 
	 * some other valid use.
	 * @param p coordinate to search for
	 * @return List of KdNodes from the root to the coordinate.
	 */
	public List trace(Coordinate p) { 
		return searchAlgorithm.coordinatePath(p); 
	}

	/**
	 * Locates and returns the nearest neighbor to the search point.
	 * This method returns the coordinate only, not the distance to 
	 * the nearest point. To obtain the distance as well as the 
	 * coordinate, see {@link NearestKdTree#nearestPair(Coordinate)}.
	 * 
	 * @param searchPt the point to search for
	 * @return the point in the tree nearest to the search point.
	 */
	public Coordinate nearest(Coordinate searchPt) {
		return searchAlgorithm.nearest(searchPt);
	}
	
	/**
	 * Locates and returns the nearest neighbor to the search point. This 
	 * method returns the search point, the nearest neighbor and the 
	 * distance to the nearest neighbor. To obtain the coordinate only,
	 * see {@link #nearest(Coordinate)}. 
	 * 
	 * @param searchPt the point to search for
	 * @return a {@link CoordinatePair} containing the search point and 
	 *         the point in the tree nearest to the search point, as well as 
	 *         the distance between the two.
	 */
	public CoordinatePair nearestPair(Coordinate searchPt) {
		return searchAlgorithm.nearestPair(searchPt) ;
	}
	
	/**
	 * Locates and returns the nearest neighbor to the search point which
	 * is not the search point itself.
	 * This method returns the coordinate only, not the distance to 
	 * the nearest point. To obtain the distance as well as the 
	 * coordinate, see {@link NearestKdTree#nearestNotIdenticalPair(Coordinate)}.
	 * 
	 * @param searchPt the point to search for
	 * @return the nearest neighbor.
	 */
	public Coordinate nearestNotIdentical(Coordinate searchPt) {
		return nonIdentical.nearest(searchPt);
	}
	
	/**
	 * Locates and returns the nearest neighbor to the search point which
	 * is not the search point itself. This 
	 * method returns the search point, the nearest neighbor and the 
	 * distance to the nearest neighbor. To obtain the coordinate only,
	 * see {@link #nearestNotIdentical(Coordinate)}. 
	 * 
	 * @param searchPt the point to search for
	 * @return a {@link CoordinatePair} containing the search point and 
	 *         the point in the tree nearest to the search point, as well as 
	 *         the distance between the two.
	 */
	public CoordinatePair nearestNotIdenticalPair(Coordinate searchPt) {
		return nonIdentical.nearestPair(searchPt) ;
	}
	
	/**
	 * Locates and returns the nearest neighbor to the search point which
	 * is not contained in the specified collection of points.
	 * This method returns the coordinate only, not the distance to 
	 * the nearest point. To obtain the distance as well as the 
	 * coordinate, see {@link NearestKdTree#nearestNotInPair(Coordinate)}.
	 * 
	 * @param searchPt the point to search for
	 * @param exclude the collection of points to exclude as candidates for 
	 *        nearest neighbor.
	 * @return the nearest neighbor.
	 */
	public Coordinate nearestNotIn(Coordinate searchPt, Collection exclude) {
		return nearestNotInPair(searchPt,exclude).getPoint2() ; 
	}
	
	/**
	 * Locates and returns the nearest neighbor to the search point which
	 * is not in the collection of points to exclude. This 
	 * method returns the search point, the nearest neighbor and the 
	 * distance to the nearest neighbor. To obtain the coordinate only,
	 * see {@link #nearestNotInPair(Coordinate)}. 
	 * 
	 * @param searchPt the point to search for
	 * @param exclude the collection of points to exclude from 
	 *        consideration as potential nearest neighbors
	 * @return a {@link CoordinatePair} containing the search point and 
	 *         the point in the tree nearest to the search point, as well as 
	 *         the distance between the two.
	 */
	public CoordinatePair nearestNotInPair(Coordinate searchPt, Collection exclude){
		return notIn.nearestPair(searchPt, exclude) ;
	}

	/**
	 * For each point in the cluster, locates and returns the nearest neighbor
	 * to the search point which is not in the cluster.
	 * 
	 * @param cluster
	 *            the collection of points to locate nearest neighbors for.
	 * @return an array of {@link CoordinatePair}s containing the search point
	 *         and the point in the tree nearest to the search point, as well as
	 *         the distance between the two.
	 */
	public CoordinatePair[] nearestNotInPairs(Collection cluster) {
		return notIn.nearestPairs(cluster) ; 
	}

	/**
	 * Factory method to create a balanced <code>NearestKdTree</code> from a 
	 * list of points. This method is much more efficient than adding the 
	 * points one at a time to an initially empty tree. This method will 
	 * remove duplicate points from the list prior to creating the tree.
	 * 
	 * @param points the list of points from which to construct a tree.
	 * @return the balanced tree containing the points.
	 */
	public static NearestKdTree loadNearestKdTree(Coordinate []points)  {
		// sift for duplicates
		Set uniquePoints = new TreeSet() ; 
		for (int i=0; i < points.length ; i++) { 
			uniquePoints.add(points[i]) ; 
		}
		Coordinate []unique = (Coordinate[])(uniquePoints.toArray(coordType)) ; 
		
		KdNode root = makeTree(unique, 0) ; 
		NearestKdTree tree = new NearestKdTree(root) ; 	
		tree.numberOfNodes = points.length ; 
		return tree ; 
	}

}
