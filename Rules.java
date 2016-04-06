import java.util.HashMap;
import java.util.HashSet;


public class Rules {
	
	HashMap<String,String> rule = new HashMap<String,String>();
	HashSet<Integer> cases = new HashSet<Integer>();
	String decision;
	int totalCases;
	public int getTotalCases() {
		return totalCases;
	}
	public void setTotalCases(int totalCases) {
		this.totalCases = totalCases;
	}
	public HashMap<String, String> getRule() {
		return rule;
	}
	public void setRule(HashMap<String, String> rule) {
		this.rule = rule;
	}
	public HashSet<Integer> getCases() {
		return cases;
	}
	public void setCases(HashSet<Integer> cases) {
		this.cases = cases;
	}
	public String getDecision() {
		return decision;
	}
	public void setDecision(String decision) {
		this.decision = decision;
	}
	
	
	
	

}
