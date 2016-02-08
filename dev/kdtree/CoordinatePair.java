package com.vividsolutions.jts.index.kdtree;

import com.vividsolutions.jts.geom.Coordinate ; 

/**
 * Stores two points, the distance between them and optionally, the {@link KdNode}
 * which corresponds to each point. This is a data type used primarily in the 
 * nearest neighbor searching in order to cache the calculated distances. The 
 * natural ordering defined by this type is the separation distance.
 * 
 * @author Bryce Nordgren
 * @since 1.12
 */
public class CoordinatePair implements Comparable {
	
	// we always have two coordinates and a distance between them
	private final Coordinate p1 ; 
	private final Coordinate p2 ; 
	private final double     distance ; 
	
	// sometimes, a coordinate is associated with a node.
	private KdNode n1 = null ; 
	private KdNode n2 = null ; 
	
	public CoordinatePair(Coordinate _p1, Coordinate _p2, double _distance) {
		p1 = _p1 ; 
		p2 = _p2 ; 
		distance = _distance ; 
	}
	
	public CoordinatePair(Coordinate _p1, Coordinate _p2) { 
		p1 = _p1 ; 
		p2 = _p2 ; 
		distance = _p1.distance(_p2) ; 
	}
	
	public Coordinate getPoint1() {
		return p1 ; 
	}
	
	public Coordinate getPoint2() {
		return p2 ; 
	}
	
	public double getDistance() {
		return distance ; 
	}
	

	public KdNode getNode1() {
		return n1;
	}

	public void setNode1(KdNode n1) {
		if (this.n1 == null) { 
			this.n1 = n1;
		}
	}

	public KdNode getNode2() {
		return n2;
	}

	public void setNode2(KdNode n2) {
		if (this.n2 == null) { 
			this.n2 = n2;
		}
	}

	/**
	 * Defines a natural ordering for CoordinatePairs. CoordinatePairs
	 * will be ordered by the separation distance.
	 * @param arg0 the other CoordinatePair
	 * @return result of comparison.
	 */
	public int compareTo(Object arg0) {
		CoordinatePair other = null ;
		if (arg0 instanceof CoordinatePair) { 
			other = (CoordinatePair)arg0 ; 
			if (distance < other.distance) {
				return -1 ; 
			} else if (distance > other.distance) { 
				return 1; 
			} else {
				return 0 ;
			}
		}
		return 0;
	}

	/**
	 * True if the distance properties of this object and the other object
	 * are equal.
	 * @param other the other object.
	 * @return distances are equal
	 */
	public boolean equals(Object other) {
		if (!(other instanceof CoordinatePair)) return false ; 
		return distance == ((CoordinatePair)other).distance ; 
	}
	
}
