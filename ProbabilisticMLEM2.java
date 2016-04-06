import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


public class ProbabilisticMLEM2 {

	ArrayList<String> attrNames = new ArrayList<String>();
	String decisionName;

	ArrayList<ArrayList<String>> attributes = new ArrayList<ArrayList<String>>();	//attributes
	ArrayList<String> decision = new ArrayList<String>();							//decision
	ArrayList<HashMap<String, HashSet<Integer>>> attrValuePairs = new ArrayList<HashMap<String, HashSet<Integer>>>();		//attrValuePairs
	HashMap<String,HashSet<Integer>> concept = new HashMap<String,HashSet<Integer>>(); 	//conceptValues
	ArrayList<String> conceptList = new ArrayList<String>(); 	//Listing concepts in the dataset
	ArrayList<HashSet<Integer>> k = new ArrayList<HashSet<Integer>>();	//characteristics sets
	ArrayList<ArrayList<Double>> probability = new ArrayList<ArrayList<Double>>();	// probability for case
	ArrayList<HashSet<Integer>> approximation = new ArrayList<HashSet<Integer>>();	//approximations
	ArrayList<Boolean> isNumeric = new ArrayList<Boolean>();		//Tracking numeric attributes
	ArrayList<CutpointProperty> cpProp = new ArrayList<CutpointProperty>();	//Tracking cutpoint properties
	ArrayList<HashSet<Integer>> ruleIntersection = new ArrayList<HashSet<Integer>>();	//Intermediate rule intersections with goal
	ArrayList<Rules> rules = new ArrayList<Rules>();
	ArrayList<MLEM2rows> mlem2rows = new ArrayList<MLEM2rows>();
	int noOfConcepts;
	int noOfRecords;
	double alpha;


	public void computeAttributeValuePairs(){
		for(int j = 0; j < attributes.size(); j++){
			ArrayList<String> aAttribute = attributes.get(j);
			HashSet<String> tempSet = new HashSet<String>();
			//compute the distinct values in a set first
			for(int i = 0; i < aAttribute.size(); i++){
				if("?".equals(aAttribute.get(i)) || "-".equals(aAttribute.get(i)) || "*".equals(aAttribute.get(i)))
					continue;
				tempSet.add(aAttribute.get(i)); 
			}
			HashMap<String,HashSet<Integer>> attrValue = new HashMap<String,HashSet<Integer>>();
			//Iterate over the set and populate the HashMap
			Iterator<String> iter = tempSet.iterator();
			while(iter.hasNext()){
				attrValue.put(iter.next(), new HashSet<Integer>());
			}

			//Loop over the attribute arraylist again and populate the sets for attr values
			for(int i = 0; i < aAttribute.size(); i++){
				if("?".equals(aAttribute.get(i))){	// add in none
					continue;
				}
				else if("*".equals(aAttribute.get(i))){	// add in all
					for (String key : attrValue.keySet()) {
						HashSet<Integer> val = attrValue.get(key);
						val.add(i);
						attrValue.put(key, val);
					}
				}
				else if("-".equals(aAttribute.get(i))){	// add in all
					String currDecision = decision.get(i);
					HashSet<Integer> set = concept.get(currDecision);
					HashSet<String> values = new HashSet<String>();
					for (Integer index : set) {
						String attrVal = aAttribute.get(index);
						if (attrVal.equals("*") || attrVal.equals("?") || attrVal.equals("-"))
							continue;
						values.add(aAttribute.get(index));
					}
					for (String key : values) {
						HashSet<Integer> val = attrValue.get(key);
						val.add(i);
						attrValue.put(key, val);
					}

				}
				else if(attrValue.containsKey(aAttribute.get(i))){
					HashSet<Integer> temp = attrValue.get(aAttribute.get(i));
					temp.add(i);
					attrValue.put(aAttribute.get(i), temp);
				}
			}
			attrValuePairs.add(attrValue);
		}
	}

	public void handleAttrConceptValue(ArrayList<String> a,int j,int i, HashSet<Integer> intersection){

		HashSet<Integer> cases = concept.get(decision.get(j));
		Iterator<Integer> iter = cases.iterator();
		HashSet<Integer> tempSet = new HashSet<Integer>();
		while(iter.hasNext()){
			Integer attValue = iter.next();
			String attrValue = a.get(attValue);
			if (attrValue.equals("*") || attrValue.equals("?") || attrValue.equals("-"))
				continue;
			tempSet.addAll(attrValuePairs.get(i).get(attrValue));
		}
		intersection.retainAll(tempSet);
	}

	public void computeCharacteristicSets(){
		int noOfRecords = attributes.get(0).size();
		this.noOfRecords = noOfRecords;
		int noOfAttributes = attributes.size();	
		for(int j = 0; j < noOfRecords; j++){
			HashSet<Integer> intersection = new HashSet<Integer>();
			for(int i = 0; i < noOfAttributes; i++){
				if("-".equals(attributes.get(i).get(j))){
					handleAttrConceptValue(attributes.get(i),j ,i, intersection);
				}else if("?".equals(attributes.get(i).get(j)) || "*".equals(attributes.get(i).get(j))){
					continue;
				}else{	
					HashSet<Integer> currSet = attrValuePairs.get(i).get(attributes.get(i).get(j));
					if(intersection.isEmpty()){
						intersection.addAll(currSet);
					}else {
						intersection.retainAll(currSet);
					}

				}

			}
			k.add(intersection);
		}
	}

	public void computeConcepts(){

		HashSet<String> tempSet = new HashSet<String>();
		//compute the distintc values in a set first
		for(int i = 0; i < decision.size(); i++){
			tempSet.add(decision.get(i)); 
		}
		noOfConcepts = tempSet.size();

		//Iterate over the set and populate the HashMap
		Iterator<String> iter = tempSet.iterator();
		while(iter.hasNext()) {
			String value = iter.next();
			concept.put(value, new HashSet<Integer>());
			conceptList.add(value);
		}

		//Loop over the attribute arraylist again and populate the sets for attr values
		for(int i = 0; i < decision.size(); i++){
			if(concept.containsKey(decision.get(i))){
				HashSet<Integer> temp = concept.get(decision.get(i));
				temp.add(i);
				concept.put(decision.get(i), temp);
			}
		}

	}

	public void parseInput(String fileName) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			int currentInput = br.read();
			StringBuilder sb = new StringBuilder();
			List<String> row = new ArrayList<String>();
			boolean headerRow = false;
			int currAttrCnt = 0;
			int attributeCount = 0;
			while (currentInput != -1) {
				char currentChar = (char) currentInput;
				//comments row
				if (currentChar == '!') {
					while (currentInput != -1 && currentChar != '\n') {
						currentInput = br.read();
						currentChar = (char)currentInput;
					}
					currentInput = br.read();
					continue;
				}
				//attribute definition row
				if (currentChar == '<') {
					while (currentInput != -1 && currentChar != '>') {
						currentInput = br.read();
						currentChar = (char)currentInput;
					}
					currentInput = br.read();
					continue;
				}

				if (currentChar == '+' ||
						currentChar == '-' ||
						currentChar == '.' ||
						currentChar == '*' ||
						currentChar == '?' ||
						currentInput >= 48 && currentInput <= 57 || //numbers
						currentInput >= 65 && currentInput <= 90 || //lowercase chars
						currentInput >= 97 && currentInput <= 122) { // uppercase chars
					sb.append(currentChar);
					currentInput = br.read();
					continue;
				}
				//attribute names row start
				if (currentChar == '[') {
					headerRow = true;
					currentInput = br.read();
					continue;
				}

				//attribute names row end
				if (currentChar == ']') {
					headerRow = false;
					attributeCount = attrNames.size() - 1;
					decisionName = attrNames.get(attributeCount);
					attrNames.remove(attributeCount);
					currentInput = br.read();
					continue;
				}

				//word end
				if (currentChar == ' ' ||
						currentChar == '\t' ||
						currentChar == '\n') {
					if (sb.length() == 0) {
						currentInput = br.read();
						continue;
					}
					if (headerRow) {
						attrNames.add(sb.toString());
					} else {
						if (currAttrCnt == attributeCount) {
							attributes.add(new ArrayList<String>(row));
							row.clear();
							decision.add(sb.toString());
							sb.setLength(0);
							currAttrCnt = 0;
						} else {
							row.add(sb.toString());
							currAttrCnt++;
						}
					}

					while (currentChar == ' ') {
						currentInput = br.read();
						currentChar = (char) currentInput;
					}
					sb.setLength(0);
					continue;
				}
				currentInput = br.read();
			}
			br.close();
			rotateAttributes();
			System.out.println(decisionName);
			for (int i = 0; i < attrNames.size(); i++) {
				System.out.print(attrNames.get(i) + " ");
			}
			System.out.println();
			for (int i = 0; i < decision.size(); i++) {
				System.out.print(decision.get(i) + " ");
			}
			System.out.println();
			printAttribute();

		} catch (FileNotFoundException fe) {
			System.out.println("Input file "+ fileName +" not found!!!" + fe);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void rotateAttributes() {
		int rowLen = attributes.size();
		int colLen = attributes.get(0).size();
		ArrayList<ArrayList<String>> attr = new ArrayList<ArrayList<String>>();
		for (int j = 0; j < colLen; j++) {
			attr.add(new ArrayList<String>());
		}
		for (int j = 0; j < colLen; j++) {
			for (int i = 0; i < rowLen; i++) {
				attr.get(j).add(attributes.get(i).get(j));
			}
		}
		attributes = attr;
	}

	public void printAttribute() {
		for (int i = 0; i < attributes.size(); i++) {
			for (int j = 0; j < attributes.get(i).size(); j++) {
				System.out.print(attributes.get(i).get(j) + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	public void discretizeData(){
		//Discretize the data here	
		for(int i = 0; i < attributes.size(); i++){
			if(checkAttributeNumerical(attributes.get(i))) {
				isNumeric.add(true);
				attributes.set(i, convertToSymbolic(attributes.get(i)));
			}else{
				isNumeric.add(false);
				cpProp.add(new CutpointProperty());

			}
		}
		printAttribute();
	}

	public boolean checkAttributeNumerical(ArrayList<String> aAttribute){
		try{
			int index = firstNonSpecialIndex(aAttribute);
			Integer.parseInt((String)aAttribute.get(index));
			return true;
		}
		catch( Exception e){
			return false;
		}
	}

	public boolean isNumerical(String str){
		NumberFormat formatter = NumberFormat.getInstance();
		  ParsePosition pos = new ParsePosition(0);
		  formatter.parse(str, pos);
		  return str.length() == pos.getIndex();
	}

	public int firstNonSpecialIndex(ArrayList<String> aAttribute){
		int i = 0;
		while ( ("?").equals(aAttribute.get(i)) || ("*").equals(aAttribute.get(i)) || ("-").equals(aAttribute.get(i)))
			i++;
		return i;
	}

	public ArrayList<String> removeSpecialCharacters(ArrayList<String> a){
		ArrayList<String> removed = new ArrayList<String>();
		for(int i = 0; i < a.size(); i++){
			if("?".equals(a.get(i)) || ("*").equals(a.get(i)) || ("-").equals(a.get(i))){
				continue;
			}
			removed.add(a.get(i));
		}
		return removed;
	}

	public ArrayList<String> removeDuplicates(ArrayList<String> aAttribute){
		ArrayList<String> result = new ArrayList<String>();
		String reference = aAttribute.get(0);
		result.add(reference);
		for(int i = 1; i <aAttribute.size(); i++){
			if(reference.equals(aAttribute.get(i))){
				continue;
			}
			reference = aAttribute.get(i);
			result.add(reference);
		}
		return result;
	}

	public ArrayList<String> retainSelectedCutpoints(ArrayList<String> sortedCopy, ArrayList<String> aAttribute){
		ArrayList<String> result = new ArrayList<String>();

		String first = sortedCopy.get(0);
		String second;
		for(int i = 1; i < sortedCopy.size(); i++){
			second = sortedCopy.get(i);
			if(!areDecisionsSame(aAttribute, first, second)){
				result.add(first);
				result.add(second);
				first = second;
			}
		}

		return result;
	}

	public void createCutpoints(ArrayList<String> cutpoints, ArrayList<String> aAttribute){
		cutpoints.add(aAttribute.get(0));
		for(int i = 1; i < aAttribute.size(); i++){
			double cutValue = (Double.parseDouble(aAttribute.get(i - 1)) + Double.parseDouble(aAttribute.get(i))) / 2;
			cutpoints.add(cutValue + ""); 
		}
		cutpoints.add(aAttribute.get(aAttribute.size() - 1));
	}
	
	public void getAttrValuePairs() {
		int attrCnt = attributes.size();
		HashMap<String, HashSet<Integer>> ret ;
		for (int i = 0; i < attrCnt; i++) {
			ret = new HashMap<String, HashSet<Integer>>();
			boolean isNumeric = false;
			ArrayList<String> attrValues = new ArrayList<String>();
			for (int j = 0; j < attributes.get(i).size(); j++) {
				if (isNumerical(attributes.get(i).get(j))) {
					isNumeric = true;
				}
				attrValues.add(attributes.get(i).get(j));
			}
			if (isNumeric) {
				attrValues = removeSpecialCharacters(attrValues);
				Collections.sort(attrValues, new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						double val1 = Double.parseDouble(o1);
						double val2 = Double.parseDouble(o2);
						return (int)(val1 - val2);
					}
					
				});
				attrValues = removeDuplicates(attrValues);
				ArrayList<Double> attrValNumeric = new ArrayList<Double>();
				for (String val : attrValues) {
					attrValNumeric.add(Double.parseDouble(val));
				}
				ArrayList<Double> cutpoints = new ArrayList<Double>();
				for (int k = 1; k < attrValNumeric.size(); k++) {
					double cutpointVal = (attrValNumeric.get(k - 1) + attrValNumeric.get(k)) / 2.0;
					cutpoints.add(cutpointVal);
				}
				
				double minVal = attrValNumeric.get(0);
				double maxVal = attrValNumeric.get(attrValNumeric.size() - 1);
				for (int k = 0; k < cutpoints.size(); k++) {
					double currCutpoint = cutpoints.get(k);
					String cutpointKey1 = minVal + "..." + currCutpoint;
					String cutpointKey2 = currCutpoint + "..." + maxVal;
					HashSet<Integer> set1 = new HashSet<Integer>();
					HashSet<Integer> set2 = new HashSet<Integer>();
					ArrayList<String> attrCol = attributes.get(i);
					for (int m = 0; m < attrCol.size(); m++) {
						String currAttrVal = attrCol.get(m);
						if (currAttrVal.equals("*")) {
							set1.add(m);
							set2.add(m);
						} else if (isNumerical(currAttrVal)) {
							double currAttrValNum = Double.parseDouble(currAttrVal);
							if (currAttrValNum <= currCutpoint) {
								set1.add(m);
							} else {
								set2.add(m);
							}
						}
					}
					ret.put(cutpointKey1, set1);
					ret.put(cutpointKey2, set2);
				}
				attrValuePairs.set(i, ret);
			}
			
		}
		/* 1 - check if attribute is numerical
		 * 2 - remove special characters from attribues
		 * 3- sort the attributes
		 * 4- remove duplicates
		 * 5 - compute cutpoint value
		 * 6- compute attribute value pairs for all cutpoints (Should include * and - cases)
		 */

	}

	public ArrayList<String> convertToSymbolic(ArrayList<String> a){
		ArrayList<String> outcome = new ArrayList<String>();
		outcome.addAll(a);
		//ArrayList<String> cutpoints = new ArrayList<String>();
		ArrayList<String> aCopy = new ArrayList<String>();
		aCopy.addAll(a);
		aCopy = removeSpecialCharacters(aCopy);
		Collections.sort(aCopy);
		aCopy = removeDuplicates(aCopy);
		//aCopy = retainSelectedCutpoints(aCopy, a);
		ArrayList<String> cutpoints = new ArrayList<String>();
		createCutpoints(cutpoints, aCopy);

		StringBuffer cutpoint  = new StringBuffer();	
		int index = 0;//firstNonSpecialIndex(a);	
		cutpoint.append(aCopy.get(index) + "...");	
		double first = Double.parseDouble((String)aCopy.get(index));
		double second;
		for(int i = index; i < cutpoints.size(); i++){
			second = Double.parseDouble((String)cutpoints.get(i));
			cutpoint.append(second);
			updateCutpoint(outcome,cutpoint,first,second);
			cpProp.add(new CutpointProperty(first, second, cutpoint.toString()));
			first = second;
			cutpoint = new StringBuffer( first + "...");
		}
		return outcome;
	}

	public void updateCutpoint(ArrayList<String> a, StringBuffer cutpoint, double first, double second){
		for(int i = 0; i < a.size();i++){
			if(isNumerical((String)a.get(i))){
				double value = Double.parseDouble((String)a.get(i));
				if(value >= first && value < second){
					a.set(i, cutpoint.toString());
				}
			}
		}
	}

	public boolean areDecisionsSame(ArrayList<String> aAttribute, String a, String b){
		if (aAttribute.indexOf("*") != -1) {
			return false;
		}
		HashSet<String> decisionA = getDecisionSet(a, aAttribute);
		HashSet<String> decisionB = getDecisionSet(b, aAttribute);

		if(decisionA.containsAll(decisionB) && decisionB.containsAll(decisionA))
			return true;
		return false;
	}


	public HashSet<String> getDecisionSet(String value , ArrayList<String> aAttribute){
		HashSet<String> result = new HashSet<String>();
		for(int s = 0; s < aAttribute.size(); s++){
			if(aAttribute.get(s).equals(value)){
				result.add(decision.get(s));
			}
		}
		return result;
	}

	public void performProbabilisticMlem2() throws Exception{
		//	discretizeData();
		computeConcepts();
		computeAttributeValuePairs();
		computeCharacteristicSets();
		computeProbability();
		double alpha = 0.5;
		computeApproximations(alpha);
		getAttrValuePairs();
		performMLEM2();
		printRules();
	}

	public static void main(String args[]) throws Exception{

		//	int noOfRecords = 10;	
		ProbabilisticMLEM2 pm = new ProbabilisticMLEM2();
		Scanner sc = new Scanner(System.in);
		String fileName = sc.nextLine();
		pm.alpha = sc.nextDouble();
		pm.parseInput(fileName);
		pm.performProbabilisticMlem2();
	}

	public int computeCardinality(HashSet<String> aSet){
		return aSet.size();
	}

	public void computeProbability(){
		for(int i = 0; i < noOfConcepts; i++){
			ArrayList<Double> probList = new ArrayList<Double>();
			HashSet<Integer> conceptSet = concept.get(conceptList.get(i));
			for(int j = 0; j < noOfRecords; j++){
				HashSet<Integer> temp = new HashSet<Integer>();
				temp.addAll(k.get(j));
				temp.retainAll(conceptSet);
				probList.add((double)temp.size()/k.get(j).size());
			}
			probability.add(probList);
		}
	}

	//computes singleton approximation
	public void computeApproximations(double alpha){
		for(int i = 0; i < noOfConcepts; i++){
			ArrayList<Double> probabList = new ArrayList<Double>();
			probabList.addAll(probability.get(i));
			HashSet<Integer> tempSet = new HashSet<Integer>();
			for(int j = 0; j < noOfRecords; j++){
				if(probabList.get(j) > alpha){
					tempSet.add(j);
				}
			}
			approximation.add(tempSet);
		}
	}
	public void populateMLEM2Rows(){

		for(int i = 0; i < attrValuePairs.size(); i++){
			HashMap<String, HashSet<Integer>> attVal =  attrValuePairs.get(i);
			Set<String> setKey = attVal.keySet();

			Iterator<String> iter = setKey.iterator();
			while(iter.hasNext()){
				MLEM2rows mRows = new MLEM2rows();
				String key = iter.next();
				HashMap<String, String> tempMap = new HashMap<String,String>();
				tempMap.put(attrNames.get(i),key);
				mRows.setAttrName(attrNames.get(i));
				mRows.setAttrVal(key);
				mRows.setAttrValue(tempMap);
				mRows.setAttrValueCases(attVal.get(key));
				mRows.setAttrNumeric(checkAttributeNumerical(attributes.get(i)));
				mlem2rows.add(mRows);
			}
		}

	}

	public void performMLEM2(){
			
			populateMLEM2Rows();

		for(int i = 0; i < noOfConcepts; i++){ // Rule induction per concept
			HashSet<Integer> goal = new HashSet<Integer>();
			goal.addAll(approximation.get(i));
			HashSet<Integer> d = new HashSet<Integer>();
			d.addAll(goal);
			HashSet<Integer> junk = new HashSet<Integer>();
			HashSet<Integer> completed = new HashSet<Integer>();

			while(!goal.isEmpty()){
				// compute the intersection of all attribute value pairs with goals
				HashMap<String,String> currRule = new HashMap<String,String>();
				HashSet<Integer> t = new HashSet<Integer>();
				int size = mlem2rows.size();
				ArrayList<HashSet<Integer>> tempIntersection = new ArrayList<HashSet<Integer>>();
				for(int k = 0; k < size; k++){
					tempIntersection.add(new HashSet<Integer>());
				}
				int minIndex = 0;
				HashSet<Integer> tempGoal = new HashSet<Integer>();
				tempGoal.addAll(goal);
				while ((t.isEmpty() || !d.containsAll(t)) && !goal.isEmpty()){
					int maxIntersections = 0;
					int minSize = 1000000;
					
					//Compute temporary intersections
					for(int j = 0; j < mlem2rows.size(); j++){
						if(!mlem2rows.get(j).isAttributeInRule){
							HashSet<Integer> tempSet = tempIntersection.get(j);
							if(tempSet.isEmpty()){
								tempSet.addAll(mlem2rows.get(j).getAttrValueCases());
							}else{
								tempSet.retainAll(mlem2rows.get(j).getAttrValueCases());
							}
							tempSet.retainAll(tempGoal);
						}else{
							tempIntersection.set(j,new HashSet<Integer>());
						}
					}

					//Finding maximum intersecting value
					int count = 0;
					ArrayList<Integer> index = new ArrayList<Integer>();
					boolean intersectionFound = false;
					for(int j = 0; j < tempIntersection.size(); j++){
						int currSize = tempIntersection.get(j).size();
						if (currSize == 0)
							continue;
						if(currSize > maxIntersections && !mlem2rows.get(j).isAttributeInRule){
							maxIntersections = currSize;
							count = 1;
							index = new ArrayList<Integer>();
							index.add(j);
							intersectionFound = true;
						} else if (currSize == maxIntersections && !mlem2rows.get(j).isAttributeInRule){
							count++;
							index.add(j);
							intersectionFound = true;
						}
					}if(intersectionFound){
					minIndex = index.get(0);
					// if tie, find min cardinality
					if(count > 1){
						minIndex = 0;
						for(int k =0; k < index.size(); k++){
							if(mlem2rows.get(index.get(k)).getAttrValueCases().size() < minSize){
								minSize = mlem2rows.get(index.get(k)).getAttrValueCases().size();
								minIndex = index.get(k);
							}
						}
					}

					//The attribute value pair at location minIndex is selected.
					if(t.isEmpty()){
						t.addAll(mlem2rows.get(minIndex).getAttrValueCases());
					}else{
						t.retainAll(mlem2rows.get(minIndex).getAttrValueCases());
					}
					tempGoal.retainAll(t);
					mlem2rows.get(minIndex).setAttributeInRule(true);
					if (currRule.containsKey(mlem2rows.get(minIndex).getAttrName())) {
						String oldTemp = currRule.get(mlem2rows.get(minIndex).getAttrName());
						String newTemp = mlem2rows.get(minIndex).getAttrVal();
						String[] split1 = oldTemp.split("\\.\\.\\.");
						String[] split2 = newTemp.split("\\.\\.\\.");
						double[] split1Num = new double[2];
						double[] split2Num = new double[2];
						for (int ind = 0; ind < split1.length; ind++) {
							split1Num[ind] = Double.parseDouble(split1[ind]);
						}
						for (int ind = 0; ind < 2; ind++) {
							split2Num[ind] = Double.parseDouble(split2[ind]);
						}
						String finalTemp = Math.max(split1Num[0], split2Num[0]) + "..." + Math.min(split1Num[1], split2Num[1]);
						currRule.put(mlem2rows.get(minIndex).getAttrName(), finalTemp);
					} else {
						currRule.put(mlem2rows.get(minIndex).getAttrName(),mlem2rows.get(minIndex).getAttrVal());
					}
					String attr_name = mlem2rows.get(minIndex).getAttrName();
					for(int k = 0; k < mlem2rows.size(); k++){
						if(attr_name.equals(mlem2rows.get(k).getAttrName()) && !(mlem2rows.get(k).isAttrNumeric)){
							mlem2rows.get(k).setAttributeInRule(true);
						}
					}
					//code to update remaining attributes in rule goes here.
			
						intersectionFound = false;
					} else {
						break;
					}
				}
				
				_resetMLEM2();

				//Compute the probability of the rule
				HashSet<Integer> dummy = new HashSet<Integer>();
				
				dummy.addAll(t);
				dummy.retainAll(goal);
				
				double probablty = dummy.size() / t.size();
				if(probablty >= alpha){
					d.addAll(t);
					completed.addAll(t);
					Rules r = new Rules();
					r.setRule(currRule);
					r.setCases(dummy);
					r.setTotalCases(t.size());
					r.setDecision(conceptList.get(i));
					rules.add(r);
				} else {
					junk.addAll(t);
				}
				HashSet<Integer> tempSet = new HashSet<Integer>();
				tempSet.addAll(d);
				tempSet.removeAll(completed);
				tempSet.removeAll(junk);
				goal = tempSet;
			}
		}
	}
	
	public void printRules() throws Exception {
		PrintWriter rulesFile = new PrintWriter("rules.txt", "UTF-8");
		for (int i = 0; i < rules.size(); i++) {
			Rules rule = rules.get(i);
			rulesFile.println("(" + rule.getRule().size() + "," + rule.getCases().size() + "," + rule.getTotalCases() + ")");
			String ruleString = "";
			HashMap<String, String> hm = rule.getRule();
			for (String key : hm.keySet()) {
				ruleString += "(" + key + ", " + hm.get(key) + ") & ";
			}
			ruleString = ruleString.substring(0, ruleString.length() - 2);
			ruleString += " -> ";
			ruleString += "(" + decisionName + ", " + rule.getDecision() + ")";
			rulesFile.println(ruleString);
		}
		rulesFile.close();
		
	}
	
	public void _resetMLEM2(){
		for(int i = 0 ; i < mlem2rows.size(); i++){
			mlem2rows.get(i).setAttributeInRule(false);
		}
	}
}
