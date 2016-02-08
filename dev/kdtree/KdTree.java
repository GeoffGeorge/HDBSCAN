/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.vividsolutions.jts.index.kdtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays ; 
import java.util.Set;
import java.util.TreeSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Envelope;

/**
 * An implementation of a 2-D KD-Tree. KD-trees provide fast range searching on
 * point data.
 * <p>
 * This implementation supports detecting and snapping points which are closer than a given
 * tolerance value. If the same point (up to tolerance) is inserted more than once a new node is
 * not created but the count of the existing node is incremented.
 * 
 * 
 * @author David Skea
 * @author Martin Davis
 */
public class KdTree implements Iterable
{
	// typedef
	protected static final Coordinate [] coordType = new Coordinate[0] ; 

	private KdNode root = null;
	private KdNode last = null;
	protected long numberOfNodes;
	private double tolerance;

	/**
	 * Creates a new instance of a KdTree 
	 * with a snapping tolerance of 0.0.
	 * (I.e. distinct points will <i>not</i> be snapped)
	 */
	public KdTree() {
		this(0.0);
	}

	/**
	 * Creates a new instance of a KdTree, specifying a snapping distance tolerance.
	 * Points which lie closer than the tolerance to a point already 
	 * in the tree will be treated as identical to the existing point.
	 * 
	 * @param tolerance
	 *          the tolerance distance for considering two points equal
	 */
	public KdTree(double tolerance) {
		this.tolerance = tolerance;
	}
	
	protected KdTree(KdNode _root, double tolerance) { 
		this.tolerance = tolerance ; 
		this.root      = _root ; 
	}

	/**
	 * Inserts a new point in the kd-tree, with no data.
	 * 
	 * @param p
	 *          the point to insert
	 */
	public KdNode insert(Coordinate p) {
		return insert(p, null);
	}

	/**
	 * Inserts a new point into the kd-tree.
	 * 
	 * @param p
	 *          the point to insert
	 * @param data
	 *          a data item for the point
	 * @return returns a new KdNode if a new point is inserted, else an existing
	 *         node is returned with its counter incremented. This can be checked
	 *         by testing returnedNode.getCount() > 1.
	 */
	public KdNode insert(Coordinate p, Object data) {
		if (root == null) {
			root = new KdNode(p, data, 0);
		}

		KdNode currentNode = root;
		KdNode leafNode = root;
		int splitOrdinate = 1 ; 
		boolean isOddLevel = true;
		boolean isLessThan = true;

		// traverse the tree first cutting the plane left-right the top-bottom
		while (currentNode != last) {
			if (isOddLevel) {
				isLessThan = p.x < currentNode.getX();
			} else {
				isLessThan = p.y < currentNode.getY();
			}
			leafNode = currentNode;
			if (isLessThan) {
				currentNode = currentNode.getLeft();
			} else {
				currentNode = currentNode.getRight();
			}
			// test if point is already a node
			if (currentNode != null) {
				boolean isInTolerance = p.distance(currentNode.getCoordinate()) < tolerance;

				// if (isInTolerance && ! p.equals2D(currentNode.getCoordinate())) {
				// System.out.println("KDTree: Snapped!");
				// System.out.println(WKTWriter.toPoint(p));
				// }

				// check if point is already in tree (up to tolerance) and if so simply
				// return
				// existing node
				if (isInTolerance) {
					currentNode.increment();
					return currentNode;
				}
			}
			splitOrdinate = (splitOrdinate+1)%2 ;
			isOddLevel = !isOddLevel;
		}

		// no node found, add new leaf node to tree
		numberOfNodes = numberOfNodes + 1;
		KdNode node = new KdNode(p, data, splitOrdinate);
		node.setLeft(last);
		node.setRight(last);
		if (isLessThan) {
			leafNode.setLeft(node);
		} else {
			leafNode.setRight(node);
		}
		return node;
	}

	private void queryNode(KdNode currentNode, KdNode bottomNode,
			Envelope queryEnv, boolean odd, List result) {
		if (currentNode == bottomNode)
			return;

		double min;
		double max;
		double discriminant;
		if (odd) {
			min = queryEnv.getMinX();
			max = queryEnv.getMaxX();
			discriminant = currentNode.getX();
		} else {
			min = queryEnv.getMinY();
			max = queryEnv.getMaxY();
			discriminant = currentNode.getY();
		}
		boolean searchLeft = min < discriminant;
		boolean searchRight = discriminant <= max;

		if (searchLeft) {
			queryNode(currentNode.getLeft(), bottomNode, queryEnv, !odd, result);
		}
		if (queryEnv.contains(currentNode.getCoordinate())) {
			result.add((Object) currentNode);
		}
		if (searchRight) {
			queryNode(currentNode.getRight(), bottomNode, queryEnv, !odd, result);
		}

	}

	/**
	 * Performs a range search of the points in the index.
	 * 
	 * @param queryEnv
	 *          the range rectangle to query
	 * @return a list of the KdNodes found
	 */
	public List query(Envelope queryEnv) {
		List result = new ArrayList();
		queryNode(root, last, queryEnv, true, result);
		return result;
	}

	/**
	 * Performs a range search of the points in the index.
	 * 
	 * @param queryEnv
	 *          the range rectangle to query
	 * @param result
	 *          a list to accumulate the result nodes into
	 */
	public void query(Envelope queryEnv, List result) {
		queryNode(root, last, queryEnv, true, result);
	}
	
	protected KdNode getRoot() { 
		return root ; 
	}
	protected KdNode getLast() {
		return last ; 
	}
	
	/**
	 * Recursively creates a balanced set of nodes given a list of 
	 * points. The list of points is sorted by the axis which is 
	 * being used for the split on this level. The median value
	 * is taken for this node. The remaining points are divided into
	 * left and right lists, which are processed by another call to
	 * this algorithm. The node returned by this 
	 * @param points List of points to make into a balanced tree
	 * @param level level of the tree (root is zero).
	 * @return the root of the produced tree.
	 * @since 1.12
	 */
	protected static KdNode makeTree(Coordinate[]points, int level) {
		KdNode middle = null; 
		int axis = level %2 ; 
		CoordinateComparator sortAxis = 
			CoordinateComparator.getComparator(axis) ; 
		
		// Sort the list 
		Arrays.sort(points, sortAxis) ; 
		
		// If the list is bigger than three points, recurse.
		if (points.length > 3) { 
			int median_idx = points.length/2 ; 
			middle = new KdNode(points[median_idx], null, axis) ;
			
			Coordinate []leftPoints = new Coordinate[median_idx];  
			Coordinate []rightPoints = new Coordinate[points.length-(median_idx+1)] ; 
			
			// split the list into "left" and "right"
			for (int i=0; i<median_idx; i++) {
				leftPoints[i] = points[i] ; 
			}		
			for (int i=median_idx+1; i<points.length; i++) { 
				rightPoints[i-(median_idx+1)] = points[i] ; 
			}
			
			middle.setLeft(makeTree(leftPoints,level+1)) ; 
			middle.setRight(makeTree(rightPoints,level+1)) ; 
		} else if (points.length == 3) {
			// if exactly three points, we know how this plays out
			middle = new KdNode(points[1], null, axis) ; 
			
			axis = (axis+1) %2 ;
			middle.setLeft(new KdNode(points[0], null, axis)) ; 
			middle.setRight(new KdNode(points[2], null, axis)) ; 
		} else if (points.length == 2) { 
			// if exactly two points, we can also just hardcode it
			middle = new KdNode(points[1], null, axis) ; 
			
			axis = (axis+1)%2 ; 
			middle.setLeft(new KdNode(points[0], null, axis)) ; 
		} else if (points.length == 1) { 
			// we should only get here if the list starts out with 
			// length one.
			middle = new KdNode(points[0], null, axis);  
		}
		
		return middle ; 
	}
	
	/**
	 * Factory method to create a balanced kd-tree from an array of 
	 * {@link Coordinate}s. The algorithm used is recursive. The points 
	 * array is sorted based on the x coordinate when this call completes.
	 * @param points Points to index with a kd-tree.
	 * @return Balanced Kd tree containing all the points in the array. 
	 * @since 1.12
	 */
	public static KdTree loadTree(Coordinate []points) { 
		// sift for duplicates
		Set uniquePoints = new TreeSet() ; 
		for (int i=0; i < points.length ; i++) { 
			uniquePoints.add(points[i]) ; 
		}
		Coordinate []unique = (Coordinate[])(uniquePoints.toArray(coordType)) ; 

		KdNode root = makeTree(unique,0) ;
		
		KdTree tree = new KdTree() ;
		tree.root = root ; 
		tree.numberOfNodes = points.length ; 
		
		return tree; 
	}

	/**
	 * Returns a depth-first iterator over the nodes.
	 * @since 1.12
	 */
	public Iterator iterator() {
		return new KdTreeIterator(root);
	}
	
	/**
	 * Returns an iterator over the node <i>Coordinates</i>.
	 * @return an iterator which returns coordinates.
	 * @since 1.12
	 */
	public Iterator coordinateIterator() { 
		return new CoordinateSetIterator(root) ; 
	}
	
	/**
	 * Returns the number of points stored in the tree.
	 * @return size of the tree.
	 * @since 1.12
	 */
	public long size() {
		return numberOfNodes ; 
	}
	
}