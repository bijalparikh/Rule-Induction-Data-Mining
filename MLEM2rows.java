import java.util.HashMap;
import java.util.HashSet;


public class MLEM2rows {
	
	HashMap<String, String> attrValue = new HashMap<String, String>();
	HashSet<Integer> attrValueCases = new HashSet<Integer>();
	String attrName;
	String attrVal;
	public String getAttrName() {
		return attrName;
	}
	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}
	public String getAttrVal() {
		return attrVal;
	}
	public void setAttrVal(String attrVal) {
		this.attrVal = attrVal;
	}
	boolean isAttributeInRule = false;
	boolean isAttrNumeric = false;
	
	public MLEM2rows(){
		
	}
	public HashMap<String, String> getAttrValue() {
		return attrValue;
	}
	public void setAttrValue(HashMap<String, String> attrValue) {
		this.attrValue = attrValue;
	}
	public HashSet<Integer> getAttrValueCases() {
		return attrValueCases;
	}
	public void setAttrValueCases(HashSet<Integer> attrValueCases) {
		this.attrValueCases = attrValueCases;
	}
	public boolean isAttributeInRule() {
		return isAttributeInRule;
	}
	public void setAttributeInRule(boolean isAttributeInRule) {
		this.isAttributeInRule = isAttributeInRule;
	}
	public boolean isAttrNumeric() {
		return isAttrNumeric;
	}
	public void setAttrNumeric(boolean isAttrNumeric) {
		this.isAttrNumeric = isAttrNumeric;
	}
	
	
	

}
