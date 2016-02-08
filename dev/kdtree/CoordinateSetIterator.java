package com.vividsolutions.jts.index.kdtree;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * An iterator over a {@link KdTree} which returns {@link Coordinate}s. This 
 * is returned by KdTree and is not typically instantiated by the user.
 * 
 * @author Bryce Nordgren
 * @since 1.12
 * @see KdTree#coordinateIterator()
 *
 */
public class CoordinateSetIterator extends KdTreeIterator {

	public CoordinateSetIterator(KdNode _root) {
		super(_root);
	}

	/**
	 * Returns the next Coordinate, or null. 
	 * @return next Coordinate or null
	 */
	public Object next() {
		Coordinate retval = null ; 
		KdNode node =  (KdNode)(super.next());
		if (node != null) { 
			retval = node.getCoordinate() ; 
		}
		return retval ;
	}

}
