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

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A node of a {@link KdTree}, which represents one or more points in the same location.
 * 
 * @author dskea
 */
public class KdNode {

    private Coordinate p = null;
    private Object     data;
    private KdNode     left;
    private KdNode     right;
    private int        count;
    private int        splitOrdinate ; 

    /**
     * Creates a new KdNode.
     * 
     * @param _x coordinate of point
     * @param _y coordinate of point
     * @param data a data objects to associate with this node
     */
    public KdNode(double _x, double _y, Object data, int _splitOrdinate) {
        p = new Coordinate(_x, _y);
        splitOrdinate = _splitOrdinate ; 
        left = null;
        right = null;
        count = 1;
        this.data = data;
    }

    /**
     * Creates a new KdNode.
     * 
     * @param p point location of new node
     * @param data a data objects to associate with this node
     */
    public KdNode(Coordinate p, Object data, int _splitOrdinate) {
        this.p = (Coordinate)(p.clone()) ; 
        splitOrdinate = _splitOrdinate ;
        left = null;
        right = null;
        count = 1;
        this.data = data;
    }

    /**
     * Returns the X coordinate of the node
     * 
     * @retrun X coordiante of the node
     */
    public double getX() {
        return p.x;
    }

    /**
     * Returns the Y coordinate of the node
     * 
     * @return Y coordiante of the node
     */
    public double getY() {
        return p.y;
    }

    /**
     * Returns the location of this node
     * 
     * @return p location of this node
     */
    public Coordinate getCoordinate() {
        return p;
    }

    /**
     * Gets the user data object associated with this node.
     * @return
     */
    public Object getData() {
        return data;
    }

    /**
     * Returns the left node of the tree
     * 
     * @return left node
     */
    public KdNode getLeft() {
        return left;
    }

    /**
     * Returns the right node of the tree
     * 
     * @return right node
     */
    public KdNode getRight() {
        return right;
    }
    
    /**
     * Retrieves the ordinate representing the split dimension:
     * <ul>
     * <li>0 == x</li>
     * <li>1 == y</li>
     * <li>2 == z</li>
     * </ul>
     * @return ordinate of split axis.
     * @since 1.12
     */
    public int getSplitOrdinate() { 
    	return splitOrdinate ; 
    }
    
    /**
     * The value of this node's coordinate along the split dimension.
     * @return this node's split dimension coordinate
     * @since 1.12
     */
    public double getSplitValue() { 
    	double retval ; 
    	switch (splitOrdinate) { 
    	case 0 :
    		retval = p.x ; 
    		break ;
    	case 1 : 
    		retval = p.y ; 
    		break ; 
    	case 2 : 
    		retval = p.z ; 
    		break ; 
    	default :
    		retval = Double.NaN ; 
    	}
    	return retval ;
    }
    
    /**
     * Returns the value of the coordinate of the specified point along the
     * split axis. 
     * @param other the specified point
     * @return the coordinate value of {@link other} along the split axis.
     * @since 1.12
     */
    public double getSplitValue(Coordinate other) { 
    	double retval ; 
    	switch (splitOrdinate) {
    	case 0 :
    		retval = other.x ; 
    		break ;
    	case 1 : 
    		retval = other.y ; 
    		break ; 
    	case 2 : 
    		retval = other.z ; 
    		break ; 
    	default :
    		retval = Double.NaN ; 
    	}
    	return retval ;
    }
    
    /**
     * Returns the projection of the point onto the splitting plane.
     * @param other The coordinate to project onto the splitting plane
     * @return returns the projection of the point onto the splitting 
     * plane of this node.
     * @since 1.12
     */
    public Coordinate projectPoint(Coordinate other) {
    	Coordinate retval = (Coordinate)(other.clone())	 ;
    	switch (splitOrdinate) {
    	case 0:
    		retval.x = p.x ; 
    		break ; 
    	case 1:
    		retval.y = p.y ; 
    		break ; 
    	case 2: 
    		retval.z = p.z ; 
    		break ; 
    	default :
    		;; // don't modify the coordinate
    	}
    	return retval ; 
    }
    

    // Increments counts of points at this location
    void increment() {
        count = count + 1;
    }

    /**
     * Returns the number of inserted points that are coincident at this location.
     * 
     * @return number of inserted points that this node represents
     */
    public int getCount() {
        return count;
    }

    /**
     * Tests whether more than one point with this value have been inserted (up to the tolerance)
     * 
     * @return true if more than one point have been inserted with this value
     */
    public boolean isRepeated() {
        return count > 1;
    }

    // Sets left node value
    void setLeft(KdNode _left) {
        left = _left;
    }

    // Sets right node value
    void setRight(KdNode _right) {
        right = _right;
    }
}
