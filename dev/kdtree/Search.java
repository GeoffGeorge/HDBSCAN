package com.vividsolutions.jts.index.kdtree;

import java.util.List;
import java.util.ListIterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.index.ArrayListVisitor;
import com.vividsolutions.jts.index.ItemVisitor;

/**
 * Utility class which implements the search logic for a {@link KdTree}. In 
 * particular, this class encapsulates the tree traversal logic for a KdTree.
 * This helper class is not intended for general purpose use. It is the common
 * basis of the various search strategies exposed on the KdTree class.
 * 
 * @author Bryce Nordgren
 * @since 1.12
 * @see NearestSearch
 * @see NearestNotInSearch
 * @see NearestNonIdenticalSearch
 *
 */
public class Search {
	
	private KdNode root = null ; 
	
	public Search(KdNode _root) { 
		root = _root ; 
	}
	
	protected KdNode getRoot() {
		return root ; 
	}
	
	/**
	 * Traverses the tree in search of point p, and returns the leaf node at the
	 * end of the search.
	 * @param p
	 * @return leaf node at the end of the tree traversal
	 */
	public KdNode findLeaf(Coordinate p) {
		List path = path(p) ; 
		return (KdNode)(path.get(path.size()-1)) ;
	}
	
	/**
	 * Returns the path through the tree (all the way to the leaf node) caused 
	 * by traversing the tree looking for coordinate p. The coordinate is not expected
	 * to exist in the tree. 
	 * 
	 * @param p coordinate to search for.
	 * @return path from root to leaf, caused by searching for p.
	 */
	public List path(Coordinate p) {
		ArrayListVisitor v = new ArrayListVisitor() ; 
		
		traverse(root, p, v) ; 
		return v.getItems() ; 
	}
	
	/**
	 * Searches the tree for the provided point. Terminates when the point
	 * is found.
	 * @param p The point to find
	 * @return true if the point is in the tree.
	 */
	public boolean contains(Coordinate p) { 
		return traverseFind(root, p) ; 
	}
	
	/**
	 * Returns the path from the root node to the specified coordinate.
	 * @param p coordinate to search for
	 * @return list of KdNodes in order of traversal.
	 */
	public List coordinatePath(Coordinate p) { 
		List leafPath = path(p) ; 
		ListIterator popper = leafPath.listIterator(leafPath.size()) ; 
		
		boolean found = false; 
		while (popper.hasPrevious() && !found) { 
			KdNode node = (KdNode)(popper.previous()) ;
			found = node.getCoordinate().equals2D(p) ; 
			if (!found) { 
				popper.remove() ; 
			}
		}
		
		return leafPath ; 		
	}
	
	/**
	 * Inverts the tree traversal logic to return the un-traversed child.
	 * @param currentNode the node for which un-traversed children must be found
	 * @param p search point
	 * @return the child not traversed (can be null). 
	 */
	protected static KdNode otherChild(KdNode currentNode, Coordinate p) { 
		KdNode other = null ; 
		double testPt = currentNode.getSplitValue(p) ;
		double split = currentNode.getSplitValue() ;
					
		if (testPt < split) {
			other = currentNode.getRight();
		} else if (testPt > split) {
			other = currentNode.getLeft();
		} else { 
			other = null ; // both children were traversed
		}
			
		return other ; 		
	}
	
	/**
	 * Tree traversal logic which terminates when the provided coordinate
	 * is located. It simply returns true if the point is present, false otherwise.
	 * @param start the node at which to start looking.
	 * @param p the coordinate to look for
	 * @return true if the point is in the tree.
	 */
	public static boolean traverseFind(KdNode start, Coordinate p) { 
		boolean found = false ;
		KdNode currentNode = start;
		KdNode last = null ; 

		// traverse the tree first cutting the plane left-right then top-bottom
		while (!found && (currentNode != last)) {
			found = (p.equals(currentNode.getCoordinate())) ; 
			
			if (!found) { 
				double testPt = currentNode.getSplitValue(p) ;
				double split = currentNode.getSplitValue() ;
							
				if (testPt < split) {
					currentNode = currentNode.getLeft();
				} else if (testPt > split) {
					currentNode = currentNode.getRight();
				} else { 
					found = traverseFind(currentNode.getRight(), p) ;  // recursively traverse the right child
					currentNode = currentNode.getLeft() ; // continue on with the left child.
				}
			}
		}
		return found ; 
	}
	
	/**
	 * Traverses the tree structure in search of the coordinate p, starting 
	 * from the provided node. Note that this method will not terminate 
	 * on "p" if it occurs in the interior of the tree. It always navigates all 
	 * the way to a leaf node.
	 * @param start The node in the tree from which to begin the traversal
	 * @param p coordinate to search for
	 * @param v if provided, will be made to visit all the nodes from start to
	 *          a leaf.
	 */
	public static void traverse(KdNode start, Coordinate p, ItemVisitor v) { 
		KdNode currentNode = start;
		KdNode last = null ; 

		// traverse the tree first cutting the plane left-right then top-bottom
		while (currentNode != last) {
			// visit the node
			if (v != null) { 
				v.visitItem(currentNode) ;
			}
			
			double testPt = currentNode.getSplitValue(p) ;
			double split = currentNode.getSplitValue() ;
						
			if (testPt < split) {
				currentNode = currentNode.getLeft();
			} else if (testPt > split) {
				currentNode = currentNode.getRight();
			} else { 
				traverse(currentNode.getRight(), p, v) ;  // recursively traverse the right child
				currentNode = currentNode.getLeft() ; // continue on with the left child.
			}
		}
	}
}
