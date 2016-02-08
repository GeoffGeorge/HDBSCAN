package com.vividsolutions.jts.index.kdtree;

import java.util.List ; 
import java.util.ArrayList ; 

/**
 * Utility class which implements a last in first out buffer. Add things to 
 * the buffer with {@link #push(Object)} and retrieve things from the 
 * buffer with {@link #pop()}.
 * 
 * @author Bryce Nordgren
 * @since 1.12
 *
 */
public class Stack {
	
	private List stack = null  ;
	
	public Stack() { 
		stack = new ArrayList() ; 
	}
	
	public void push(Object o) { 
		stack.add(0,o) ; 
	}
	
	public Object pop() { 
		return stack.remove(0) ; 
	}
	
	public int size() {
		return stack.size() ; 
	}
	
	public boolean isEmpty() {
		return stack.isEmpty() ; 
	}

}
