package main.java.hdbscanstar;

public class MutualReachabilityEdge {
	
	private int label1;
	private int label2;
	private double mrDistance;
	
	public MutualReachabilityEdge(KdNode node1, KdNode node2){
		if(node1.getCoreDistance() >= node2.getCoreDistance()){
			this.label1 = node1.getLabel();
			this.label2 = node2.getLabel();
			this.mrDistance = node1.getCoreDistance();
		}else{
			this.label1 = node2.getLabel();
			this.label2 = node1.getLabel();
			this.mrDistance = node2.getCoreDistance();
		}
	}
	

	public int getLabel1() {
		return label1;
	}


	public int getLabel2() {
		return label2;
	}


	public double getMrDistance() {
		return mrDistance;
	}


	@Override
	public String toString() {
		return "MutualReachabilityEdge [label1=" + label1 + ", label2=" + label2 + ", mrDistance=" + mrDistance + "]";
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + label1;
		result = prime * result + label2;
		long temp;
		temp = Double.doubleToLongBits(mrDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutualReachabilityEdge other = (MutualReachabilityEdge) obj;
		if (label1 != other.label1)
			return false;
		if (label2 != other.label2)
			return false;
		if (Double.doubleToLongBits(mrDistance) != Double.doubleToLongBits(other.mrDistance))
			return false;
		return true;
	}
}
