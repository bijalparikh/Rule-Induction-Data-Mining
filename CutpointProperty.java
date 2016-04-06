
public class CutpointProperty {
	
	double start;
	double end;
	String cutpoint;
	
	
	public CutpointProperty(double start, double end, String cutpoint){
		this.start = start;
		this.end = end;
		this.cutpoint = cutpoint;
	}
	
	public CutpointProperty(){
		
	}
	public double getStart() {
		return start;
	}
	public void setStart(double start) {
		this.start = start;
	}
	public double getEnd() {
		return end;
	}
	public void setEnd(double end) {
		this.end = end;
	}
	public String getCutpoint() {
		return cutpoint;
	}
	public void setCutpoint(String cutpoint) {
		this.cutpoint = cutpoint;
	}

}
