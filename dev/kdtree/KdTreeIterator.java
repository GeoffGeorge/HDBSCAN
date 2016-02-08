package com.vividsolutions.jts.index.kdtree;

import java.util.Iterator;

/**
 * Implements a depth-first, left, node, right iterator over all the nodes in a
 * {@link KdTree}. This implementation uses a simple list based stack to keep
 * track of its position in the tree. The objects returned by calls to
 * {@link #next()} are {@link KdNode}s.
 * 
 * @author Bryce Nordgren
 * @since 1.12
 * @see KdTree
 * @see Stack
 * 
 */
public class KdTreeIterator implements Iterator {
	private KdNode root ; 
	private Stack nodeStack ; 
	
	public KdTreeIterator(KdNode _root) {
		root = _root ; 
		nodeStack = new Stack() ; 
		
		descend(_root) ;  
	}
	
	private void descend(KdNode current) {
		while (current != null) {
			nodeStack.push(current) ; 
			current = current.getLeft() ; 
		}		
	}

	/**
	 * true if there is another item in the tree to visit.
	 */
	public boolean hasNext() {
		return !nodeStack.isEmpty();
	}

	/**
	 * Returns the next node in the tree
	 */
	public Object next() {
		KdNode current = (KdNode)(nodeStack.pop()) ; 
		KdNode rightChild = current.getRight() ; 
		
		if (rightChild != null) { 
			descend(rightChild) ; 
		}
		
		return current;
	}

	/**
	 * Not implemented. Throws {@link UnsupportedOperationException}.
	 */
	public void remove() {
		throw new UnsupportedOperationException() ; 
	}

}
