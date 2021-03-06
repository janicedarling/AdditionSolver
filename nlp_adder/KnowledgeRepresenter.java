package nlp_adder;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;



class TimeStamp {
	String time;
	String entity;
	Set value;
}

class State extends ArrayList<TimeStamp> {};

class Situation extends HashMap<String, State> {}; // verb, state


public class KnowledgeRepresenter {

	private static final String CHANGE_OUT = "changeOut";
	private static final String CHANGE_IN = "changeIn";
	private static final String COMPARE_PLUS = "comparePlus";
	private static final String COMPARE_MINUS = "compareMinus";
	private static final String INCREASE = "increase";
	private static final String REDUCTION = "reduction";
	private static final String ALTOGETHER_EQ = "altogetherEq";
	private static final String COMPARE_PLUS_EQ = "comparePlusEq";
	private static final String COMPARE_MINUS_EQ = "compareMinusEq";
	
	private static final String X_VALUE = "x";
	private static final String VAR_PATTERN = "x\\d+";

	private static final String CHANGE = "change";
	private static final String OWNER_1 = "[owner1]";
	private static final String OWNER_2 = "[owner2]";
	private static final String ENTITY = "[entity]";
	private static final String UNKNOWN = "unknown";
	private static final String TIMESTAMP_PREFIX = "t";
	
	private static final int NO_OWNERS_SUPPORTED = 2;
	private static final String PRESENT = "present";
	private static final String PAST = "past";

	static int timeStep = 0;
	static int varCount = 1;
	static char setName = 'A';
	static int unknownCounter = 0;

	static int questionTime = -1;
	static int attempts = 0;	
	static String questionEntity = "";
	static String questionOwner = "";
	static String questionOwner1 = "";
	static String questionOwner2 = "";
	static String questionVerb = "";
	static String equalityEquation = "";	
	static String finalAns = "";
	static String question = "";
	
	static boolean isQuestionAggregator = false;
	static boolean isQuestionDifference = false;
	static boolean isQuestionComparator = false;
	static boolean isQuestionSet = false;

	static HashMap<String,Set> sets = new HashMap<>();
	static HashMap<String,Situation> story = new HashMap<>();
	static LinkedHashSet<String> entities = new LinkedHashSet<String>();
	static HashMap<String,String> variables = new HashMap<String,String>();
	static HashMap<String,String> procedureMap = new HashMap<String,String>();
	static ArrayList<String> storyTense = new ArrayList<String>();
	static HashMap<String,String> keywordMap = new HashMap<String,String>();
	static ArrayList<String> allEquations = new ArrayList<String>();
	static String fullStory = "";
	static ArrayList<String> ignoreWords = new ArrayList<String>();
	private static LinkedHashSet<String> owners = new LinkedHashSet<String>();
	static String explanation;
	
	

	private static void loadProcedureLookup() {
		keywordMap.put("put", CHANGE_OUT);
		keywordMap.put("place", CHANGE_OUT);
		keywordMap.put("plant", CHANGE_OUT);
		keywordMap.put("stack", CHANGE_OUT);
		keywordMap.put("add", CHANGE_OUT);
		keywordMap.put("sell", CHANGE_OUT);
		keywordMap.put("distribute", CHANGE_OUT);
		//keywordMap.put("serve", CHANGE_OUT);
		keywordMap.put("give", CHANGE_OUT);
		keywordMap.put("load", CHANGE_OUT);
		keywordMap.put("pour", CHANGE_OUT);
		keywordMap.put("build", CHANGE_OUT);
		
		keywordMap.put("more than", COMPARE_PLUS);
		keywordMap.put("less than", COMPARE_MINUS);
		keywordMap.put("get", CHANGE_IN);
		keywordMap.put("buy", CHANGE_IN);
		keywordMap.put("pick", CHANGE_IN);
		keywordMap.put("cut", CHANGE_IN);
		keywordMap.put("take", CHANGE_IN);
		keywordMap.put("receive", CHANGE_IN);
		keywordMap.put("borrow", CHANGE_IN);
		keywordMap.put("lose", REDUCTION);
		keywordMap.put("use", REDUCTION);
		keywordMap.put("leave", REDUCTION);
		keywordMap.put("transfer", REDUCTION);
		keywordMap.put("spill", REDUCTION);
		keywordMap.put("leak", REDUCTION);
		keywordMap.put("produce", INCREASE);
		keywordMap.put("remove", REDUCTION);
		keywordMap.put("spend", REDUCTION);
		keywordMap.put("eat", REDUCTION);
		keywordMap.put("more", INCREASE);
		keywordMap.put("immigrate", INCREASE);
		keywordMap.put("increase", INCREASE);
		keywordMap.put("carry", INCREASE);
		keywordMap.put("saw", REDUCTION);
		keywordMap.put("taller", INCREASE);
		//keywordMap.put("find", INCREASE);
		keywordMap.put("decrease", REDUCTION);
		//keywordMap.put("break", REDUCTION);
		keywordMap.put("finish", REDUCTION);
		
		procedureMap.put(CHANGE_OUT, "[owner1]-[entity].[owner2]+[entity]");
		procedureMap.put(CHANGE_IN, "[owner1]+[entity].[owner2]-[entity]");
		procedureMap.put(COMPARE_PLUS, "[entity]+[owner2]");
		procedureMap.put(COMPARE_MINUS, "[owner2]-[entity]");
		procedureMap.put(REDUCTION, "[owner1]-[entity]");
		procedureMap.put(INCREASE, "[owner1]+[entity]");
		procedureMap.put(ALTOGETHER_EQ, "[owner1]+[owner2]=[entity]");
		procedureMap.put(COMPARE_PLUS_EQ, "[owner1] = [owner2]+[entity]");
		procedureMap.put(COMPARE_MINUS_EQ, "[owner1] = [owner2]-[entity]");
		
		ignoreWords.add("crack");
		ignoreWords.add("paint");
		ignoreWords.add("go");
		ignoreWords.add("jog");
		ignoreWords.add("want");
		ignoreWords.add("pay");
		ignoreWords.add("land");
		ignoreWords.add("tear");
		ignoreWords.add("dye");
		ignoreWords.add("rain");
		ignoreWords.add("contain");
		ignoreWords.add("drive");
		//ignoreWords.add("fill");
		ignoreWords.add("record");
		ignoreWords.add("purchase");
		ignoreWords.add("move");
		ignoreWords.add("drink");
		ignoreWords.add("snow");
		ignoreWords.add("break");
		ignoreWords.add("require");
		ignoreWords.add("run");
		ignoreWords.add("hike");
		//ignoreWords.add("find");
		ignoreWords.add("weigh");
		ignoreWords.add("read");
		ignoreWords.add("convert");
		ignoreWords.add("travel");
		ignoreWords.add("walk");
		ignoreWords.add("call");
		ignoreWords.add("total");
		ignoreWords.add("result");
		ignoreWords.add("serve");
		ignoreWords.add("list");
	}
	
	static void clear() {
		timeStep = 0;
		varCount = 1;
		unknownCounter = 0;
		setName = 'A';
		isQuestionAggregator = false;
		isQuestionDifference = false;
		isQuestionComparator = false;
		isQuestionSet = false;

		questionTime = 0;
		questionEntity = "";
		questionOwner = "";
		questionOwner1 = "";
		questionOwner2 = "";
		finalAns = "";
		explanation  = "";
		fullStory = "";

		story = new HashMap<String,Situation>();
		variables = new HashMap<String,String>();
		procedureMap = new HashMap<String,String>();
		entities = new LinkedHashSet<String>();
		storyTense = new ArrayList<String>();
		allEquations = new ArrayList<String>();
		sets = new HashMap<String,Set>();
		
		sets.put(Set.Empty.name, Set.Empty);
	}
	
	public static String commonString(String s1, String s2) {
		   for (int i = Math.min(s1.length(), s2.length()); ; i--) {
		       if (s2.endsWith(s1.substring(0, i))) {
		    	   return s1.substring(0, i);
		       }
		   }    
	}

	private static void updateTimestamp (String owner, Set value, 
			String tense, String verbQual, String entity) {
		owner = owner.toLowerCase();
		System.out.println(owner + "|update|" +  "|" +timeStep +"|"+ verbQual+"|"+entity);
		String changeTime = "";
		if (tense.equals(PAST) && storyTense.contains(PRESENT) && (Collections.frequency(storyTense, PRESENT) == 1)) 
				changeTime  = "0";
		else
			changeTime = timeStep + "";
		storyTense.add(tense);
		if (verbQual.equals("has")) {
			ArrayList<String> timeStrings = new ArrayList<>();
			timeStrings.add("today");
			timeStrings.add("yesterday");
			timeStrings.add("tomorrow");
			for (String timeString : timeStrings) {
				if (entity.contains(timeString))
					entity = entity.replace(timeString, "");
			}
		}
		String time = TIMESTAMP_PREFIX + changeTime;
		Situation newSituation = new Situation();
		if (story.containsKey(owner)) {
			newSituation = story.get(owner);
		}
		State newState = new State();
		if (!newSituation.isEmpty() && newSituation.containsKey(verbQual))
			newState = newSituation.get(verbQual);
		Set existingValue = new Set();
		String lhs = "", rhs = value.cardinality;
		System.out.println(lhs+"|"+rhs);
		boolean changeFlag = false;
		for (TimeStamp t : newState) {
			if (lhs.isEmpty() && rhs.contains("x") || rhs.isEmpty() && lhs.contains("x"))
				break;
			if (value.cardinality.contains(".") && !isQuestionComparator)
				break;
			if (t.time.equals(time) && (t.entity.toLowerCase().contains(entity.toLowerCase()) || entity.toLowerCase().contains(t.entity.toLowerCase()))) {
				//System.err.println(entity+t.entity);
				existingValue = t.value;
				lhs = existingValue.cardinality;
				if (!lhs.contains("+") && !rhs.contains("+") && !lhs.contains("-") && !rhs.contains("-")) {
					if (entity.equals("dollars") && !verbQual.equals("has"))
						break;
					if (ignoreWords.contains(verbQual))
						break;
					if (lhs.equals("x")) {
						t.value.cardinality = value.cardinality;
						sets.put(t.value.name,value);
						return;
					}
					System.out.println(entity+t.entity+verbQual);
					timeStep++;
					State tempState = new State();
					TimeStamp t1 = new TimeStamp();
					t1.time = TIMESTAMP_PREFIX + timeStep;
					
					if (Double.parseDouble(rhs) > Double.parseDouble(lhs)) {
						t1.value = Set.difference(value, t.value);
						explanation = explanation + "Now, " + owner + " " + verbQual + " " + rhs + " " + entity + ".\n";
						explanation = explanation + "Hence, " + owner + " got " + t1.value.cardinality + " " +entity + ".";
						changeFlag = true;
						sets.put(t1.value.name, t1.value);
						t1.entity = entity;
						tempState.add(t1);
						newSituation.put("get", tempState);
						
					}
					if (Double.parseDouble(rhs) < Double.parseDouble(lhs)) {
						t1.value = Set.difference(t.value, value);
						explanation = explanation + "Now, " + owner + " " + verbQual + " " + rhs + " " + entity + ".\n";
						explanation = explanation + "Hence, " + owner + " lost " + t1.value.cardinality + " " +entity + ".\n";
						changeFlag = true;
						sets.put(t1.value.name, t1.value);
						t1.entity = entity;
						tempState.add(t1);
						newSituation.put("lose", tempState);
					}
					timeStep++;
					time = TIMESTAMP_PREFIX + timeStep;
					break;
				}
				Set unknown;
				if (lhs.contains("x") || rhs.contains("x")) {
					String ans = EquationSolver.getSolution(lhs + "=" + rhs);
					explanation = explanation + "Hence, " + owner + " " + verbQual + " " + lhs + " " +entity + ".\n";
					explanation = explanation + "Now, " + owner + " " + verbQual + " " + rhs + " " + entity + ".\n";
					explanation = explanation + "Therefore, " + lhs + " = " + rhs; 
					Set replace = null;
					if (lhs.contains("x")) 
						replace = existingValue;
					else
						replace = value;
					unknown = replace.unknownComponent();
					if (ans.endsWith(".0"))
						ans = ans.replace(".0", ""); 
					unknown.cardinality = ans;
					replace.components.put(unknown.name, unknown);
					sets.put(unknown.name, unknown);
					recomputeSets();
					replace.computeCardinality();
					changeFlag = true;
				}
			}
		}
		if (!value.cardinality.contains("x") && !changeFlag)
			explanation = explanation + owner + " " + verbQual + " " + value.cardinality + " " + entity + ".\n";
		else if (value.cardinality.equals("x"))
			explanation = explanation + owner + " " + verbQual + " x " + entity + ".\n";
		TimeStamp t = new TimeStamp();
		t.time = time;
		t.value = value;
		sets.put(t.value.name, t.value);
		t.entity = entity;
		newState.add(t);
		newSituation.put(verbQual,newState);
		story.put(owner, newSituation);
		String temp = explanation;
		if (verbQual.endsWith("s") && !verbQual.equals("has"))
			verbQual = verbQual.substring(0,verbQual.length()-1);
		System.out.println(verbQual+"aa");//return;
		if (!keywordMap.containsKey(verbQual) && !verbQual.equals("has") && !ignoreWords.contains(verbQual)) { 
			updateTimestamp(owner,value,tense,"has",entity);
			explanation = temp;
		}
	}
	private static void recomputeSets() {
		for (Entry<String,Set> set : sets.entrySet()) {
			if (!set.getKey().equals(Set.Empty.name)) {
				HashMap<String,Set> newComponents = new HashMap<String, Set>();
				Set current = set.getValue();
				for (Entry<String,Set> component : current.components.entrySet()) {
					newComponents.put(component.getKey(), sets.get(component.getKey()));
				}
				current.components = newComponents;
				current.computeCardinality();
				sets.put(current.name, current);
			}
		}
	}

	private static void reflectChanges(String owner1, String owner2, Entity newEntity,
			   String keyword, String procedure, String tense, String nounQual, String verbQual) {
		System.out.println(keyword+verbQual+"|"+owner1+"|"+owner2);
		if (verbQual.equals("buy") || verbQual.equals("purchase") || verbQual.equals("pay") || verbQual.isEmpty())
			if (entities.contains("dollar") || entities.contains("dollars")) {
				verbQual = "spend";
				procedure = REDUCTION;
			}
		/*if (!verbQual.equals("has") && (procedure == null || procedure.isEmpty()) && ignoreWords.contains(verbQual) && newEntity.name.equals("dollars")) {
			verbQual = "spend";
			procedure = REDUCTION;
		}*/
		Set newSet = new Set();
		if (newEntity.value.equals("some"))
			newSet.cardinality = "x";
		else
			newSet.cardinality = newEntity.value;
		String entity = newEntity.name;
		newSet.name = setName + "";
		newSet.compute = newSet.name;
		sets.put(newSet.name, newSet);
		int next = setName + 1;
		setName = (char) next;
		
		if (!keyword.isEmpty() && !keyword.contains("more") && !keyword.contains("less")) {
			verbQual = keyword;
		}
		if ((verbQual.equals("buy") || verbQual.equals("pay")) && newEntity.name != null && newEntity.name.contains("dollar")) {
			keyword = "spend";
			procedure = keywordMap.get(keyword);
			verbQual = "spends";
			
			//System.out.println("check"+verbQual+procedure);
		}
		
		if (owner1.isEmpty()) {
			if (procedure != null && (procedure.contains("change") || procedure.contains("compare") 
					|| procedure.contains("Eq"))) {
				for (String owner: owners) {
					if (!owner2.isEmpty() && !owner.isEmpty()) {
						if (owner.contains(owner2) || owner2.contains(owner))
							break;
						if (owners.size() > 1 && story.containsKey(owner)) {
							owner1 = owner;
							break;
						}
					}
				}
			}
			else if (!owners.isEmpty()) {
				for (String owner: owners) {
					if (story.containsKey(owner)) {
							owner1 = owner;
							break;
					}
				}
			}
			if (owner1.isEmpty())
				owner1 = UNKNOWN; 
		}
		if (owner2.isEmpty()) {
			if (procedure != null && (procedure.contains("change") || procedure.contains("compare") || procedure.contains("Eq"))) {
				for (String owner: owners) {
					//System.err.println(owner+"|"+owner1);
					if (!owner1.isEmpty() && !owner.isEmpty()) {
						if (owner.equals(owner1))
							break;
						if (owners.size() > 1 && story.containsKey(owner)) {
							owner2 = owner;
							break;
						}
					}
				}	
				//System.err.println(owner2+"|"+owner1);
				if (owner2.isEmpty())
					owner2 = UNKNOWN;
			}
			if (owner2.isEmpty() && verbQual.equals("has") && owner1.isEmpty())
				owner2 = UNKNOWN;
		}
		System.out.println(keyword+verbQual+"|"+owner1+"|"+owner2);
		
		// There is no keyword here, an entity has been assigned a value
		if (procedure != null && procedure.equals(COMPARE_PLUS) && owner2.isEmpty())
			procedure = INCREASE;
		if (owner1.contains(UNKNOWN) && !owners.isEmpty())
			owner1 = owners.iterator().next();
		if (owner2.contains(UNKNOWN) && !owners.isEmpty() && entities.contains(owner1)) {
			for (String owner: owners) {
				if (story.containsKey(owner)) {
						owner2 = owner;
						break;
				}
			}
		}
			
		
		System.out.println("e"+owner1 + "|" + owner2 + "|" + keyword + "|" + procedure + "|" + tense + "|" + newEntity.value +"|"+entities +"|"+verbQual);
		if ((procedure != null && procedure.equals(COMPARE_PLUS)) && (owner2.isEmpty() || owner2.contains(UNKNOWN)) && story.size() == 1)
			owner2 = owner1; 
		if (newEntity.name == null)
			newEntity.name = entities.iterator().next();
		String owner = "";
		if (procedure == null)
			procedure = "";
		if (!procedure.isEmpty() || keywordMap.containsKey(keyword)) {
			if (!procedure.contains(COMPARE_MINUS) && !procedure.equals(COMPARE_PLUS)) {
				timeStep++;
				inertia(owner1,owner2,newEntity.name);
			}
		}
		if (!procedure.contains("Eq") && (keyword.equals(verbQual) || keyword.equals(verbQual.substring(0, verbQual.length()-1)) || keyword.isEmpty())) {	
			if (entities.contains(owner1))
				owner = owner2;
			else
				owner = owner1;
			HashMap<String, State> currentSituation = new HashMap<>();
			if (story.containsKey(owner)) {
				currentSituation = story.get(owner);
				//////////System.out.println("aa"+owner+currentSituation.entrySet().size());
			}
			String verb = verbQual;
			if (verbQual.equals("has") && !currentSituation.isEmpty()) {
					verb = currentSituation.entrySet().iterator().next().getKey();
					if (keywordMap.containsKey(verb) || keywordMap.containsKey(verb.substring(0, verb.length()-1))) 
						verb = "has";
			}
			if (newEntity.value.isEmpty()) {
				newEntity.value = X_VALUE + varCount;
				variables.put(newEntity.value, null);
				varCount++;
			}
			//System.out.println(verbQual+"||"+verb);
			if (!verb.equals("has") && !verb.endsWith("s"))
				updateTimestamp (owner, newSet, tense, verb + "s", entity);
			else
				updateTimestamp (owner, newSet, tense, verb, entity);
			displayStory();
		}
		if (entities.contains(owner1))
			owner = owner2;
		else
			owner = owner1;
		if (procedure.isEmpty() && newEntity.name != null) {
			displayStory();
			return;
		}
		tense = "";
		if (!procedure.contains(COMPARE_MINUS) && !procedure.equals(COMPARE_PLUS))
			timeStep++;
		if (procedure.contains(COMPARE_MINUS)) 
			updateTimestamp (owner1, newSet, tense, "has", entity + " less than " + owner2.toLowerCase());
		if (procedure.contains(COMPARE_PLUS)) 
			updateTimestamp (owner1, newSet, tense, "has", entity + " more than " + owner2.toLowerCase());
		
		String verb = verbQual;
		String newName1 = "", newName2 = "";
		if (!keyword.contains("more") && !keyword.contains("less"))
			verb = "has";
		Set oldValue1 = new Set(), oldValue2 = new Set();
		ArrayList<String> timeStrings = new ArrayList<String>();
		timeStrings.add("today");
		timeStrings.add("yesterday");
		timeStrings.add("tomorrow");
		owner1 = owner1.toLowerCase();
		owner2 = owner2.toLowerCase();
		for (String timeString : timeStrings) {
			if (entity.contains(timeString))
				entity = entity.replace(timeString, "");
		}
		try {
			String retrieve = verb;
			if (keywordMap.containsKey(verb) && keyword.equals(verb))
				retrieve = "has";
			State verbStory = story.get(owner1).get(retrieve);
			//modularize
			for (TimeStamp currentTimeStamp : verbStory) {
				System.out.println("cc"+currentTimeStamp.entity+"|"+entity);
				if (currentTimeStamp.entity.contains(entity) || entity.contains(currentTimeStamp.entity)) {
					oldValue1 = currentTimeStamp.value;
				}
			}
		} catch (NullPointerException ex) {
			if (procedure.equals(REDUCTION) && !verbQual.equals("spend")) {
				Iterator<Entry<String, Situation>> it = story.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Situation> pair = it.next();
					owner1 = pair.getKey();
					if (!pair.getValue().containsKey("has") || owner1.equals(UNKNOWN))
						continue;
					State verbStory = pair.getValue().get("has");
					//modularize
					for (TimeStamp currentTimeStamp : verbStory) {
						System.out.println(currentTimeStamp.entity + "|" + entity);
						if (currentTimeStamp.entity.contains(entity)) {
							oldValue1 = currentTimeStamp.value;
						}
					}
					if (oldValue1 != null) {
						updateTimestamp (owner1, newSet, tense, "lose", entity);
						timeStep++;
						break;
					}
				
				}
			} else {
				Set correctValue = resolveNullEntity(newEntity.name, owner1, verb);
				oldValue1 = correctValue;
			}
		} catch (IndexOutOfBoundsException ex) {
		}
		if (procedure.contains("change") || procedure.contains("compare") || procedure.contains("Eq")) {
			try {
				State verbStory = story.get(owner2).get(verb);
				//modularize
				for (TimeStamp currentTimeStamp : verbStory) {
					if (currentTimeStamp.entity.equals(entity)) {
						oldValue2 = currentTimeStamp.value;
					}
				}
				if (oldValue2.name.equals(Set.Empty.name))
					throw new NullPointerException();
			} catch (NullPointerException ex) {
				System.out.println("hi");
				Set correctValue = resolveNullEntity(entity, owner2, verb);
				oldValue2 = correctValue;
			} catch (IndexOutOfBoundsException ex) {
				System.out.println("hii");
				Set correctValue = resolveNullEntity(entity, owner2, verb);
				oldValue2 = correctValue;
			}
		}
		System.err.println("aa"+oldValue1.name+"|"+oldValue2.name+"|"+owner1);		
		//System.err.println("aa"+timeStep);
		String[] steps = procedureMap.get(procedure).split("\\.");
		System.out.println("AA"+procedure + "|" + procedureMap.get(procedure) + "|" + steps.length + owner1 + "|" + oldValue1.cardinality + "|" + owner2 + "|" + oldValue2.cardinality);
		if (steps.length > NO_OWNERS_SUPPORTED) {
			//System.out.println("Invalid procedure description");
			System.exit(0);
		}
		for (int i = 0; i < steps.length; i++) {
			Set changeSet = new Set();
			String template = steps[i];
			String step = steps[i];
			step.replace(OWNER_1, oldValue1.name);
			step.replace(OWNER_2, oldValue2.name);
			step.replace(ENTITY, newSet.name);
			if (!procedure.contains("Eq")) {
				if (i == 0) {
					owner = owner1;
					if (!newName1.isEmpty() && !newName1.equals(changeSet.name))
						entity = newName1;
				} else {
					owner = owner2;
					if (!newName2.isEmpty() && !newName2.equals(changeSet.name))
						entity = newName2;
				}
				String split = "";
				if (template.contains("+")) {
					split = "\\+";
				}
				else
					split = "-";
				Set A = null, B = null;
				if (template.split(split)[0].equals(OWNER_1))
					A = oldValue1;
				else if (template.split(split)[0].equals(OWNER_2))
					A = oldValue2;
				else if (template.split(split)[0].equals(ENTITY))
					A = newSet;
				if (template.split(split)[1].equals(OWNER_1))
					B = oldValue1;
				else if (template.split(split)[1].equals(OWNER_2))
					B = oldValue2;
				else if (template.split(split)[1].equals(ENTITY))
					B = newSet;
				
				A = sets.get(A.name);
				B = sets.get(B.name);
				//System.out.println(A.name);
				//System.out.println(B.name);
				changeSet = split.equals("\\+") ? Set.union(A, B) : Set.difference(A, B);
				changeSet.components.putAll(A.components);
				changeSet.components.putAll(B.components);
				updateTimestamp (owner, changeSet, tense, verb, entity);
				
			}
			else {
				allEquations.add(step);
			}
		}
		//inertia();
		System.out.println(oldValue1.name+oldValue2.name);
		if (!verbQual.equals(keyword) && keywordMap.containsKey(verbQual)) {
			procedure = keywordMap.get(verbQual);
			steps = procedureMap.get(procedure).split("\\.");
			ArrayList<TimeStamp> verbStory = story.get(owner1).get(verbQual);
			Set value = new Set();
			for (TimeStamp currentTimeStamp : verbStory) {
				if (currentTimeStamp.value.name.equals(newEntity.name) && currentTimeStamp.time.equals(TIMESTAMP_PREFIX+timeStep+"")) {
					value = currentTimeStamp.value;
				}
			}
			////////////System.out.println(procedure + "|" + procedureMap.get(procedure) + "|" + steps.length + owner1 + "|" + oldValue1 + "|" + owner2 + "|" + oldValue2);
			//if (steps.length > NO_OWNERS_SUPPORTED) {
				////////////System.out.println("Invalid procedure description");
				//////System.exit(0);
			//}
			System.out.println(oldValue1.name+oldValue2.name);
			
			for (int i = 0; i < steps.length; i++) {
				Set changeSet = new Set();
				Entity modifiedEntity = new Entity();
				modifiedEntity.name = newEntity.name;
				String step = steps[i];
				step = step.replace(OWNER_1, oldValue1.name);
				step = step.replace(OWNER_2, oldValue2.name);
				step = step.replace(ENTITY, "("+value.name+")");
				System.out.println(step);
				modifiedEntity.value = step;
				if (!procedure.contains("EQ")) {
					if (i == 0) {
						owner = owner1;
						if (!newName1.isEmpty() && !newName1.equals(changeSet.name))
							entity = newName1;
					} else {
						owner = owner2;
						if (!newName2.isEmpty() && !newName2.equals(changeSet.name))
							entity = newName2;
					}
					String split = "";
					if (step.contains("+")) {
						split = "\\+";
					}
					else
						split = "-";
					System.out.println(step);
					Set A = sets.get(step.split(split)[0]);
					Set B = sets.get(step.split(split)[1].replace("(", "").replace(")",""));
					changeSet = split.equals("\\+") ? Set.union(A, B) : Set.difference(A, B);
					updateTimestamp(owner, changeSet, tense, "has", entity);
				}
				else
					allEquations.add(step);
			}
			////////System.out.println("hello1");
			//inertia();
		}
		displayStory();
	}
	
	private static Set resolveNullEntity(String name, String owner, String verb) {
		Set correctValue = Set.Empty;
		try {
			Situation currentSituation = story.get(owner);
			if (!currentSituation.containsKey(verb))
				verb = "has";
			State currentState = currentSituation.get(verb);
			System.out.println("waka"+owner);
			//String time = TIMESTAMP_PREFIX + (timeStep-1);
			for (TimeStamp t : currentState) {
				System.out.println(t.entity);
				if ((t.entity.toLowerCase().contains(name.toLowerCase()) || name.toLowerCase().contains(t.entity.toLowerCase()))) {
					return t.value;
				}
			}
		} catch (Exception e) {
			System.out.println("error");
			return correctValue;
		}
		return correctValue;
	}

	private static void inertia(String owner1, String owner2, String entity) {
		if (timeStep == 0)
			return;
		Iterator<Entry<String, Situation>> it = story.entrySet().iterator();
		String currentTime = TIMESTAMP_PREFIX + (timeStep - 1);
		String nextTime = TIMESTAMP_PREFIX + (timeStep + 1);
		HashMap<String,Situation> newStory = new HashMap<String, Situation>(); 
		while (it.hasNext()) {
			Entry<String, Situation> e = it.next();
			String owner = e.getKey();
			Situation s = e.getValue();
			Situation newSituation = new Situation();
			Iterator<Entry<String, State>> it1 = s.entrySet().iterator();
			while (it1.hasNext()) {
				Entry<String, State> e1 = it1.next();
				State newState = new State();
				String verb = e1.getKey();
				System.out.println("hhhh"+verb);
				if (verb.endsWith("s") && !verb.equals("has"))
					verb = verb.substring(0,verb.length()-1);
				if (keywordMap.containsKey(verb) || keywordMap.containsKey(verb.replace("s", ""))) {
					System.out.println("VV"+verb);
					newState.addAll(e1.getValue());
					newSituation.put(verb, newState);
					continue;
				}
				for (TimeStamp t : e1.getValue()) {
					if (t.time.equals(currentTime)) {
						boolean shouldAdd = true;
						if (owner.equals(owner1.toLowerCase()) || owner.equals(owner2.toLowerCase()))
							if (t.entity.toLowerCase().contains(entity.toLowerCase()) || entity.toLowerCase().contains(t.entity.toLowerCase()))
								shouldAdd = false;
						if (shouldAdd) {
							TimeStamp newTimeStamp = new TimeStamp();
							newTimeStamp.value = t.value;
							newTimeStamp.entity = t.entity;
							newTimeStamp.time = nextTime;
							newState.add(newTimeStamp);
						}
					}
					newState.add(t);
				}
				newSituation.put(verb, newState);
			}
			newStory.put(owner, newSituation);
		}
		story = newStory;
	}
	
	
	static String displayStory() {
		String completeStory = "";
		System.out.println("----------------------------------------------------");
		Iterator<Entry<String,Situation>> storyIterator = story.entrySet().iterator();
		ArrayList<String> dispStory = new ArrayList<String>();
		for (int i = 0; i <= timeStep+1; i++)
			dispStory.add("");
		while (storyIterator.hasNext()) {
			 Entry<String, Situation> pair = storyIterator.next();
			 String owner = pair.getKey();
		     Situation currentSituation = pair.getValue();
		     Iterator<Entry<String,State>> verbIterator = currentSituation.entrySet().iterator();
			 while (verbIterator.hasNext()) {
				Entry<String, State> newPairs = verbIterator.next();
				String verb = newPairs.getKey();
				State verbStory = newPairs.getValue();
				for (TimeStamp currentTimeStamp : verbStory) {
					String ans = owner + " " + verb + " " + currentTimeStamp.value.name + " " + currentTimeStamp.entity;
					String oldStatus = "";
					int index = Integer.parseInt(currentTimeStamp.time.replace(TIMESTAMP_PREFIX, "")); 
					try {
						oldStatus = dispStory.get(index);
						ans = oldStatus + "\n" + ans;
					} catch (Exception e) {
						
					}
					dispStory.set(index, ans);
				}
			} 
		}
		for (int counter = 0; counter <= timeStep; counter++) {
			for (String ans : dispStory) {
				if (dispStory.indexOf(ans) == counter && !ans.isEmpty()) {
					completeStory = completeStory + TIMESTAMP_PREFIX + counter + "\n";  
					completeStory = completeStory + ans + "\n";  
					completeStory = completeStory + "-----------------------------------\n";
				}
			}
		}
		for (Entry<String,Set> set : sets.entrySet()) {
			if (!set.getKey().equals(Set.Empty.name))
				completeStory = completeStory + set.getKey() + " " + set.getValue().cardinality+"\n";
		}
		System.out.println(completeStory);
		return completeStory;
	}	
	public static void represent(LinguisticInfo extractedInformation, String q) {
		loadProcedureLookup();
		question = q;
		entities = extractedInformation.entities;
		owners = extractedInformation.owners;
		//////System.err.println(extractedInformation.sentences.size());
		//set flags
		for (LinguisticStep ls : extractedInformation.sentences) {
			if (ls.isQuestion) {
				//isQuestionAggregator = ls.aggregator;
				isQuestionDifference = ls.difference;
				if (!isQuestionComparator)
					isQuestionComparator = ls.comparator;
				isQuestionSet = ls.setCompletor;
			}
		}
		for (LinguisticStep ls : extractedInformation.sentences) {
			Entity currentEntity = new Entity();
			currentEntity.name = ls.entityName;
			currentEntity.value = ls.entityValue;
			System.out.println("kr" + ls.owner1 + "|" + ls.owner2 + "|" + 
				currentEntity.name + "|" + currentEntity.value + "|" + 
					ls.keyword + "|" + ls.procedureName + "|" + ls.tense +"|"+ls.verbQual);
			if (ls.isQuestion) {
				if (isQuestionAggregator)
					continue;
				//System.out.println("waka");
				questionOwner = questionOwner1 = ls.owner1;
				if (!ls.owner2.isEmpty())
					questionOwner2 = ls.owner2;
				questionEntity = ls.entityName;
				if (ls.tense.equals(PAST) && storyTense.contains(PRESENT))
					questionTime = 0;
				else
					questionTime = timeStep;
				isQuestionAggregator = ls.aggregator;
				isQuestionDifference = ls.difference;
				if (!isQuestionComparator)
					isQuestionComparator = ls.comparator;
				isQuestionSet = ls.setCompletor;
				questionVerb = ls.verbQual;
				System.out.println("krq" + ls.owner1 + "|" + ls.owner2 + "|" + 
				   currentEntity.name + "|" + currentEntity.value + "|" + 
				    ls.keyword + "|" + ls.procedureName + "|" + ls.tense +"|"+ls.verbQual+ ls.aggregator);
				if (ls.entityValue == null)
					continue;
			}
			String nounQual = ls.nounQual, verbQual = ls.verbQual;
			if (verbQual == null)
				verbQual = "has";
			if (nounQual == null)
				nounQual = "";
			//System.err.println(ls.tense);
			reflectChanges(ls.owner1, ls.owner2, currentEntity, ls.keyword, 
					ls.procedureName, ls.tense, nounQual, verbQual);
		}
	}
	
	
	public static void solve() {
		System.out.println(questionVerb+"|"+questionEntity+"|"+questionOwner+"|"+isQuestionSet+"|"+questionTime+"|"+isQuestionAggregator+"|"+isQuestionComparator);
        fullStory = displayStory();        
                if (questionVerb.equals("spend"))
			questionEntity = "dollars";
		if ((questionVerb.equals("buy") || questionVerb.equals("purchase")) && entities.contains("dollars")) 
			questionEntity = "dollars";
		if (question.contains(" longer ") || question.contains(" farther ")) {
			if (question.contains(" foot "))
				questionEntity = "foot";
			else if (question.contains(" inch "))
				questionEntity = "inch";
			else if (question.contains(" mile "))
				questionEntity = "mile";
		}
		if (questionVerb.equals("weigh"))
			questionEntity = "pound";
		if (questionOwner.isEmpty() && !entities.contains(questionOwner2) && !isQuestionComparator)
			questionOwner = questionOwner1 = questionOwner2;
		if (!story.containsKey(questionOwner) && !questionOwner.isEmpty()) {
			Iterator<Entry<String, Situation>> it1 = story.entrySet().iterator();
			while (it1.hasNext()) {
				String owner = it1.next().getKey();
				if (questionOwner.contains(owner) || owner.contains(questionOwner)) {
					if (story.containsKey(owner)) {
						questionOwner = owner;
						break;
					}
				}
			}
		}
		String verbTemp = questionVerb;
                
		System.out.println(questionVerb+"|"+questionEntity+"|"+questionOwner+"|"+isQuestionSet+"|"+questionTime+"|"+isQuestionAggregator);
		boolean isEvent = keywordMap.containsKey(questionVerb.substring(0, questionVerb.length()-1));
		if (isQuestionComparator && doesStory("lose"))
			questionVerb = "lose";
		else if (isQuestionComparator) {
			questionOwner1 = questionOwner;
			if (!story.containsKey(questionOwner1)) 
				questionOwner1 = story.entrySet().iterator().next().getKey();
			if (questionOwner2.isEmpty() || !story.containsKey(questionOwner2)) {
				questionOwner2 = "";
				Iterator<Entry<String, Situation>> it = story.entrySet().iterator();
				while (it.hasNext()) {
					String potentialOwner = it.next().getKey();
					if (!potentialOwner.equals(questionOwner1))
						questionOwner2 = potentialOwner;
				}
			}
			System.out.println("aa"+questionOwner2+questionOwner1);
			if (questionOwner2.isEmpty()) {
				questionOwner2 = questionOwner1;
				isEvent = true;
			}
			if (!story.get(questionOwner1).containsKey(questionVerb))
				questionVerb = story.get(questionOwner1).entrySet().iterator().next().getKey();
			State currentState = story.get(questionOwner1).get(questionVerb);
			String v1 = "", v2 = "";
			for (TimeStamp t : currentState) {
				if (!isEvent && !t.time.equals(TIMESTAMP_PREFIX+questionTime))
					continue;
				if (sets.get(t.value.name).cardinality.contains("x") || sets.get(t.value.name).components.containsKey(Set.Empty))
					continue;
				v1 = sets.get(t.value.name).cardinality;
			}
			if (!story.get(questionOwner2).containsKey(questionVerb))
				questionVerb = story.get(questionOwner2).entrySet().iterator().next().getKey();
			currentState = story.get(questionOwner2).get(questionVerb);
			for (TimeStamp t : currentState) {
				if (!isEvent && !t.time.equals(TIMESTAMP_PREFIX+questionTime))
					continue;
				if (sets.get(t.value.name).cardinality.contains("x") || sets.get(t.value.name).components.containsKey(Set.Empty))
					continue;
				if (!sets.get(t.value.name).cardinality.equals(v1))
					v2 = sets.get(t.value.name).cardinality;
			}
			if (v2.isEmpty()) {
				Iterator<Entry<String, State>> it = story.get(questionOwner2).entrySet().iterator();
				while (it.hasNext()) {
					String verb = it.next().getKey();
					if (!questionVerb.equals(verb)) {
						questionVerb = verb;
						break;
					}
				}
				currentState = story.get(questionOwner2).get(questionVerb);
				for (TimeStamp t : currentState) {
					if (!isEvent && !t.time.equals(TIMESTAMP_PREFIX+questionTime))
						continue;
					if (sets.get(t.value.name).cardinality.contains("x") || sets.get(t.value.name).components.containsKey(Set.Empty))
						continue;
					if (!sets.get(t.value.name).cardinality.equals(v1))
						v2 = sets.get(t.value.name).cardinality;
				}
				
			}
			String ans = "";
			System.out.println("hi"+v1+"|"+v2);
			if (v1.contains("+") || v1.contains("-") || v2.contains("+") || v2.contains("-")|| v1.isEmpty() || v2.isEmpty()) {
				Pattern numPattern = Pattern.compile("\\d*\\.?\\d+");
				Matcher varMatcher = numPattern.matcher(question);
				v1 = ""; v2 = "";
				String val = "";
				while (varMatcher.find()) {
					val = varMatcher.group();
					if (v1.isEmpty() && question.contains(" " + val + " " + questionEntity))
						v1 = val;
					else if (!v1.isEmpty() && question.contains(" " + val + " " + questionEntity))
						v2 = val;
				} 
			}
			System.out.println("hi"+v1+"|"+v2);
			if (v1.isEmpty() || v2.isEmpty()) {
				Pattern numPattern = Pattern.compile("\\d*\\.?\\d+");
				Matcher varMatcher = numPattern.matcher(question);
				v1 = ""; v2 = "";
				String val = "";
				while (varMatcher.find()) {
					val = varMatcher.group();
					if (v1.isEmpty())
						v1 = val;
					else 
						v2 = val;
				}
			}
			if (Double.parseDouble(v1) > Double.parseDouble(v2))
				ans = v1 + "-" + v2;
			else
				ans = v2 + "-" + v1;
			explanation = explanation + questionOwner1 + " " + questionVerb + " " + ans + " " + questionEntity + "than" + questionOwner2;
			finalAns = questionOwner1 + " " + questionVerb + " " + EquationSolver.getSolution(ans) + " " + questionEntity + "than" + questionOwner2;
			return;
		}
		if (!story.containsKey(questionOwner))
			questionOwner = "";
		System.out.println(questionOwner+"|"+questionVerb);
		if (isQuestionAggregator && !questionOwner.isEmpty() && !questionVerb.isEmpty() && story.get(questionOwner).containsKey(questionVerb)) {
			State currentState = story.get(questionOwner).get(questionVerb);
			String ans = "";
			for (TimeStamp t : currentState) {
				if (!isEvent && !t.time.equals(TIMESTAMP_PREFIX+questionTime))
					continue;
				if (sets.get(t.value.name).cardinality.contains("x") || sets.get(t.value.name).components.containsKey(Set.Empty))
					continue;
				ans = sets.get(t.value.name).cardinality + "+" + ans;
			}
			if (ans != null) {
				ans = ans.substring(0,ans.length()-1);
				if (!question.contains(ans)) {
					if (ans.contains("+") && !question.contains(EquationSolver.getSolution(ans).replace(".0", ""))) {
						explanation = explanation + "Altogether " + ans + " " + questionEntity + ".\n";
						finalAns = "Altogether " + EquationSolver.getSolution(ans) + " " + questionEntity;
						explanation = explanation + "Hence, a" + finalAns.substring(1,finalAns.length()).replace(".0", "") + ".";
						System.out.println(explanation);
						return;
					}
				}
			}
			
		}
		String big = "0", small = "0";
		if (isQuestionDifference) {
			Iterator<Entry<String, Situation>> it = story.entrySet().iterator();
			int counter = 0;
			while (it.hasNext()) {
			     Entry<String, Situation> pairs = it.next();
			     Situation currentSituation = pairs.getValue();
			     Iterator<Entry<String, State>> it1 = currentSituation.entrySet().iterator();
				 while (it1.hasNext()) {
					Entry<String, State> newPairs = it1.next();
					State newState = newPairs.getValue();
					for (TimeStamp t : newState) {
						if (!questionEntity.isEmpty() && !(questionEntity.contains(t.entity) || t.entity.contains(questionEntity)) && entities.contains(questionEntity) && !t.entity.contains(questionEntity))
							continue;
						
						if (!sets.get(t.value.name).cardinality.contains("x") && !t.value.name.contains(Set.Empty.name)) {
							if (counter == 0) {
								big = sets.get(t.value.name).cardinality;
								counter++;
							} else if (!sets.get(t.value.name).cardinality.contains("x")){
								small = sets.get(t.value.name).cardinality;
							}
						}
					}
				 }
			}	
			if (big.equals("0") || small.equals("0")) {
				big = "0"; small = "0";
				counter = 0;
				it = story.entrySet().iterator();
				while (it.hasNext()) {
				     Entry<String, Situation> pairs = it.next();
				     Situation currentSituation = pairs.getValue();
				     Iterator<Entry<String, State>> it1 = currentSituation.entrySet().iterator();
					 while (it1.hasNext()) {
						Entry<String, State> newPairs = it1.next();
						State newState = newPairs.getValue();
						for (TimeStamp t : newState) {
							if (!sets.get(t.value.name).cardinality.contains("x") && !t.value.name.contains(Set.Empty.name)) {
								if (counter == 0) {
									big = sets.get(t.value.name).cardinality;
									counter++;
								} else if (!sets.get(t.value.name).cardinality.contains("x")){
									small = sets.get(t.value.name).cardinality;
								}
							}
						}
					 }
				}
			}
			String ans = big + "-" + small;
			if (ans.contains(X_VALUE)) {
				////////////System.out.println("Cannot be solved!");
				////////////System.out.println("Assuming initial conditions");
				ans = ans.replaceAll(VAR_PATTERN, "");
			}
			if (ans.contains("+") || ans.contains("-"))
				finalAns = EquationSolver.getSolution(ans) + " " + questionEntity + " left";
			else
				finalAns = ans + " " + questionEntity + " left";
			////////////System.out.println("false");
			return;
		}
		if (isQuestionAggregator && !questionOwner.isEmpty()) {
			Situation currentSituation = story.get(questionOwner);
			Iterator<Entry<String, State>> it = currentSituation.entrySet().iterator();
			String ans = "", entity = "";
			while (it.hasNext()) {
				Entry<String, State> pair = it.next();
				State candidate = pair.getValue();
				String verb = pair.getKey();
				//if (story.get(questionOwner).containsKey("has") && !verb.equals("has") && !questionEntity.contains("dollars"))
					//continue;
				//System.out.println(verb+"|"+candidate.get(0).value.name);
				isEvent = keywordMap.containsKey(verb);
				if (keywordMap.containsKey(questionVerb.substring(0,questionVerb.length()-1)) && !isEvent)
					continue;
				
				for (TimeStamp t : candidate) {
					if (questionEntity.isEmpty())
						entity = t.entity;
					else
						entity = questionEntity;
					if (sets.get(t.value.name).cardinality.contains("x"))
						continue;
					boolean checkEntity = false;
					if ((t.entity.contains(entity) || entity.contains(t.entity)))
							checkEntity = true;
					else {
						String tName = t.entity.toLowerCase();
						String[] names = entity.toLowerCase().split(" ");
						checkEntity = true;
						for (String name : names) {
							if (!tName.contains(name))
								checkEntity = false;
						}
					}
					if (checkEntity) {
						if (!isEvent) {
							if (t.time.equals(TIMESTAMP_PREFIX+questionTime)) {
								ans = sets.get(t.value.name).cardinality + "+" + ans;
								//questionEntity = entity;
							}
						}
						else {
							ans = sets.get(t.value.name).cardinality + "+" + ans;
							//questionEntity = entity;
						}
					}
				}
			}
			if (ans.isEmpty()) {
				questionVerb = "has";
				State currentState = story.get(questionOwner).get(questionVerb);
				if (currentState == null)
					questionVerb = story.get(questionOwner).entrySet().iterator().next().getKey();
				currentState = story.get(questionOwner).get(questionVerb);
				for (TimeStamp t : currentState) {
					if (!isEvent && !t.time.equals(TIMESTAMP_PREFIX+questionTime))
						continue;
					if (sets.get(t.value.name).cardinality.contains("x") || sets.get(t.value.name).components.containsKey(Set.Empty))
						continue;
					ans = sets.get(t.value.name).cardinality + "+" + ans;
				}
				if (ans.endsWith("+"))
					ans = ans.substring(0,ans.length()-1);
				if (!ans.isEmpty() && !question.contains(ans)) {
					if (ans.contains("+") && !question.contains(EquationSolver.getSolution(ans).replace(".0", ""))) {
						finalAns = "Altogether " + EquationSolver.getSolution(ans) + " " + questionEntity;
						return;
					}
				}
			}
			if (ans.endsWith("+"))
				ans = ans.substring(0,ans.length()-1);
			System.out.println(ans);
			if (!ans.isEmpty() && !question.contains(" "+ans+" ")) {
				if (ans.contains("+") && !question.contains(EquationSolver.getSolution(ans).replace(".0", ""))) {
				finalAns = "Altogether " + EquationSolver.getSolution(ans) + " " + questionEntity;
				return;
				}
			}
			System.out.println(question);
			Pattern numPattern = Pattern.compile("\\d*\\.?\\d+\\s");
			Matcher varMatcher = numPattern.matcher(question);
			String sum = "0";
			while (varMatcher.find()) {
				sum = sum + "+" + varMatcher.group();
				System.out.println(sum);
			}
			System.out.println("s" + sum);
			if (questionEntity.isEmpty() && !entities.isEmpty())
				questionEntity = entities.iterator().next();
			finalAns = "Altogether " + EquationSolver.getSolution(sum) + " " + questionEntity;
			return;
		}
		if (isQuestionAggregator && questionOwner.isEmpty() && !questionVerb.isEmpty()) {
			String ans = "";
			Iterator<Entry<String, Situation>> it1 = story.entrySet().iterator();
			while (it1.hasNext()) {
				Situation currentSituation = it1.next().getValue();
				State currentState = currentSituation.get(questionVerb);
				if (currentState == null)
					continue;
				for (TimeStamp t : currentState) {
					boolean checkEntity = false;
					String entity = questionEntity;
					if ((t.entity.contains(entity) || entity.contains(t.entity)))
							checkEntity = true;
					else {
						String tName = t.entity.toLowerCase();
						String[] names = entity.toLowerCase().split(" ");
						checkEntity = true;
						for (String name : names) {
							if (!tName.contains(name))
								checkEntity = false;
						}
					}
					
					if (!isEvent && !t.time.equals(TIMESTAMP_PREFIX+questionTime))
						continue;
					if (sets.get(t.value.name).cardinality.contains("x") || t.value.name.contains(Set.Empty.name+"-"))
						continue;
					if (questionEntity.isEmpty())
						ans = sets.get(t.value.name).cardinality + "+" + ans;
					else if (checkEntity)
						ans = sets.get(t.value.name).cardinality + "+" + ans;
				}
				
			}
			if (!ans.isEmpty() && ans.endsWith("+"))
				ans = ans.substring(0,ans.length()-1);
			System.out.println("a"+ans);
			if (!ans.isEmpty() && !question.contains(ans)) {
				if (ans.contains("+") && !(question.contains(EquationSolver.getSolution(ans).replace(".0", "")+" "+questionEntity))) {
					explanation = explanation + "Altogether " + ans + " " + questionEntity + ".\n";
					finalAns = "Altogether " + EquationSolver.getSolution(ans) + " " + questionEntity;
					explanation = explanation + "Hence, a" + finalAns.substring(1,finalAns.length()).replace(".0","")+ ".";;
					System.out.println(explanation);
					return;
				}
			}
			it1 = story.entrySet().iterator();
			String totalans = "";
			String setNamesTotal = new String();
			while (it1.hasNext()) {
				Situation currentSituation = it1.next().getValue();
				ans = "";
				String setNames = new String();
				Iterator<Entry<String, State>> it = currentSituation.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, State> pair = it.next();
					State currentState = pair.getValue();
					String verb = pair.getKey();
					if (verb.equals(questionVerb))
						continue;
					isEvent = keywordMap.containsKey(verb);
					for (TimeStamp t : currentState) {
						System.out.println(setNames+verb+t.value.cardinality);
						String entity = questionEntity;
						boolean checkEntity = false;
						if ((t.entity.contains(entity) || entity.contains(t.entity)))
								checkEntity = true;
						else {
							String tName = t.entity.toLowerCase();
							String[] names = entity.toLowerCase().split(" ");
							checkEntity = true;
							for (String name : names) {
								if (!tName.contains(name))
									checkEntity = false;
							}
						}
						
						if (!isEvent && !t.time.equals(TIMESTAMP_PREFIX+questionTime) && !setNamesTotal.contains(t.value.name)) {
							if (sets.get(t.value.name).cardinality.contains("x"))
								continue;
							if (sets.get(t.value.name).name.contains(Set.Empty.name))
								continue;
							
							totalans = sets.get(t.value.name).cardinality + "+" + totalans;
							setNamesTotal = setNamesTotal + t.value.name;
							continue;
						}
						if (sets.get(t.value.name).cardinality.contains("x") || t.value.name.contains(Set.Empty.name+"-"))
							continue;
						if (!setNamesTotal.contains(t.value.name)) {
							totalans = sets.get(t.value.name).cardinality + "+" + totalans;
							setNamesTotal = setNamesTotal + t.value.name;
						}
						if (questionEntity.isEmpty() && !setNames.contains(t.value.name)) {
							ans = sets.get(t.value.name).cardinality + "+" + ans;
							setNames = setNames + t.value.name;
						}	
						else if (checkEntity)
							if (!setNames.contains(t.value.name)) {
								ans = sets.get(t.value.name).cardinality + "+" + ans;
								setNames = setNames + t.value.name;
							}
					}
				}
				if (!ans.isEmpty() && ans.endsWith("+"))
					ans = ans.substring(0,ans.length()-1);
				System.out.println("a"+ans);
				if (ans.contains("0+"))
					ans = EquationSolver.getSolution(ans);
				System.out.println("a"+ans);
				if (!ans.isEmpty() && !question.contains(ans) && !ans.contains("+")) {
					if (ans.endsWith("+"))
						ans = ans.substring(0,ans.length()-1);
					if (questionEntity.isEmpty())
						questionEntity = entities.iterator().next();
					explanation = explanation + "Altogether " + ans + " " + questionEntity + ".\n";
					finalAns = "Altogether " + EquationSolver.getSolution(ans) + " " + questionEntity;
					explanation = explanation + "Hence, a" + finalAns.substring(1,finalAns.length()).replace(".0","")+ ".";
					System.out.println(explanation);
					return;
				}
			}
			ans = totalans;
			System.out.println("at"+ans);
			if (ans.endsWith("+"))
				ans = ans.substring(0,ans.length()-1);
			if (ans.startsWith("0+"))
				ans = ans.replace("0+", "");
			if (!ans.isEmpty() && !question.contains(ans) && !ans.contains("x")) {
				explanation = explanation + "Altogether " + ans + " " + questionEntity + "\n";
				finalAns = "Altogether " + EquationSolver.getSolution(ans) + " " + questionEntity;
				explanation = explanation + "Hence, a" + finalAns.substring(1,finalAns.length()).replace(".0","");;
				System.out.println(explanation);
				return;
			}
			ans = "";
			Iterator<Entry<String, Set>> it = sets.entrySet().iterator();
			while (it.hasNext()) {
				String value = it.next().getValue().cardinality;
				if (!value.contains("x"))
					ans = ans + value + "+";
			}
			if (!ans.isEmpty() && ans.endsWith("+"))
				ans = ans.substring(0,ans.length()-1);
			System.out.println("a"+ans);
			if (ans.contains("0+"))
				ans = EquationSolver.getSolution(ans);
			System.out.println("a"+ans);
			if (!ans.isEmpty() && !question.contains(ans)) {
				if (ans.endsWith("+"))
					ans = ans.substring(0,ans.length()-1);
				if (questionEntity.isEmpty())
					questionEntity = entities.iterator().next();
				explanation = explanation + "Altogether " + ans + " " + questionEntity + ".\n";
				finalAns = "Altogether " + EquationSolver.getSolution(ans) + " " + questionEntity;
				explanation = explanation + "Hence, a" + finalAns.substring(1,finalAns.length()).replace(".0","")+ ".";
				System.out.println(explanation);
				return;
			}
			if (ans.isEmpty()) {
				System.out.println(question);
				Pattern numPattern = Pattern.compile("\\s\\d*\\.?\\d+\\s");
				Matcher varMatcher = numPattern.matcher(question);
				String sum = "0";
				while (varMatcher.find()) {
					sum = sum + "+" + varMatcher.group();
					System.out.println(sum);
				}
				System.out.println("s" + sum);
				if (questionEntity.isEmpty() && !entities.isEmpty())
					questionEntity = entities.iterator().next();
				explanation = explanation + "Altogether " + ans + " " + questionEntity + "\n";
				finalAns = "Altogether " + EquationSolver.getSolution(ans) + " " + questionEntity;
				explanation = explanation + "Hence, a" + finalAns.substring(1,finalAns.length()).replace(".0","");;
				System.out.println(explanation);
				return;
			}
		}
		if (isQuestionSet && !questionOwner.isEmpty()) {
			Set complete = null, subset = null;
			Iterator<Entry<String, State>> it = story.get(questionOwner).entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, State> pair = it.next();
				State candidate = pair.getValue();
				String verb = pair.getKey();
				if (candidate.get(0).value.cardinality.contains("x"))
					continue;
				if (candidate.get(0).value.name.contains(Set.Empty.name))
					continue;
				//System.err.print(candidate.get(0).value.name);
				if (verb.equals("has") && complete == null)
					complete = candidate.get(0).value; 
				if (!verb.equals("has") && subset == null) {
					subset = candidate.get(0).value;
					//break;
				}
			}
			//System.out.println(complete.name+"|"+subset.name);
			if (complete == null || subset == null || complete.name.equals(subset.name)) {
				Iterator<Entry<String, Situation>> it1 = story.entrySet().iterator();
				while (it1.hasNext()) {
					Situation currentSituation = it1.next().getValue();
					it = currentSituation.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, State> pair = it.next();
						State candidate = pair.getValue();
						String verb = pair.getKey();
						if (candidate.get(0).value.cardinality.contains("x"))
							continue;
						if (candidate.get(0).value.name.contains(Set.Empty.name))
							continue;
						if (verb.equals("has"))
							complete = candidate.get(0).value; 
						//System.out.println(complete);
						if (!verb.equals("has")) {
							subset = candidate.get(0).value;
							break;
						}
						
					}
				}
			}
			System.out.println(complete+"|"+subset+"aa");
			if (complete != null && subset != null && !complete.name.equals(subset.name)) {
			String ans = EquationSolver.getSolution(complete.cardinality + "-" + subset.cardinality);
			if (!question.contains(ans)) {
				finalAns = story.entrySet().iterator().next().getKey() + " " + questionVerb + " " + ans + " " + questionEntity;
				return;
			}}
			questionOwner = "";
		}
		if (isQuestionSet && questionOwner.isEmpty()) {
			Set complete = null, subset = null;
			Iterator<Entry<String, Situation>> it1 = story.entrySet().iterator();
			while (it1.hasNext()) {
				Situation currentSituation = it1.next().getValue();
				Iterator<Entry<String, State>> it = currentSituation.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, State> pair = it.next();
					State candidate = pair.getValue();
					String verb = pair.getKey();
					//System.out.println(verb+"|"+candidate.get(0).value.name);
					if (candidate.get(0).value.cardinality.contains("x"))
						continue;
					if (verb.equals("has") && !candidate.get(0).value.name.contains(Set.Empty.name))
						complete = candidate.get(0).value; 
					if (complete != null && !candidate.get(0).value.name.equals(complete.name)) {
						subset = candidate.get(0).value;
					}
				}
			}
			System.out.println("aa"+complete+"|"+subset);
			if (complete == null || subset == null || complete.name.equals(subset.name)) { 
				Iterator<Entry<String, Set>> it = sets.entrySet().iterator();
				complete = null; subset = null;
				while (it.hasNext()) {
					Set candidate = it.next().getValue();
					if (candidate.cardinality.contains("x"))
						continue;
					if (candidate.name.contains("-"))
						continue;
					if (!candidate.name.contains(Set.Empty.name) && complete == null)
						complete = candidate;
					if (!candidate.name.contains(Set.Empty.name) && complete != null && subset == null && !candidate.name.equals(complete.name)) {
						subset = candidate;
						System.out.println(subset.name);
						break;
					}
				}
			}
			System.out.println(complete+"|"+subset);
			String ans = EquationSolver.getSolution(complete.cardinality + "-" + subset.cardinality);
			explanation = explanation + story.entrySet().iterator().next().getKey() + " " + questionVerb + " " + complete.cardinality + "-" + subset.cardinality + " " + questionEntity;
			finalAns = story.entrySet().iterator().next().getKey() + " " + questionVerb + " " + ans + " " + questionEntity;
			return;
		}
		if (questionOwner.isEmpty()) {
			Iterator<Entry<String, Situation>> it1 = story.entrySet().iterator();
			String entity = "", ans = "", totalAns = "";
			System.out.println("check");
			while (it1.hasNext()) {
				Entry<String,Situation> entry = it1.next();
				String owner = entry.getKey();
				Situation currentSituation = entry.getValue();
				ans = "";totalAns = "";
				isEvent = keywordMap.containsKey(questionVerb.substring(0,questionVerb.length()-1));
				System.out.println(owner+"a");
				if (currentSituation.containsKey(questionVerb)) {
					questionOwner = owner;
					System.out.println("b"+owner);
					for (TimeStamp t : currentSituation.get(questionVerb)) {
						if (questionEntity.isEmpty())
							entity = t.entity;
						else
							entity = questionEntity;
						if (sets.get(t.value.name).cardinality.contains("x") || t.value.name.contains(Set.Empty.name+"-"))
							continue;
						boolean inQues = question.contains((EquationSolver.getSolution(sets.get(t.value.name).cardinality)).replace(".0", ""));
						System.out.println(inQues+"|"+sets.get(t.value.name).cardinality);
						if (inQues && !sets.get(t.value.name).cardinality.contains("+") && !sets.get(t.value.name).cardinality.contains("-")) {
							if (isEvent || !isEvent && t.time.equals(TIMESTAMP_PREFIX+questionTime))
								totalAns = sets.get(t.value.name).cardinality + "+" + totalAns;
							continue;
						}
						if (inQues && sets.get(t.value.name).cardinality.startsWith("0+")) {
							if (isEvent || !isEvent && t.time.equals(TIMESTAMP_PREFIX+questionTime))
								totalAns = sets.get(t.value.name).cardinality + "+" + totalAns;
							continue;
						}
						boolean checkEntity = false;
						if ((t.entity.contains(entity) || entity.contains(t.entity)))
								checkEntity = true;
						else {
							String tName = t.entity.toLowerCase();
							String[] names = entity.toLowerCase().split(" ");
							checkEntity = true;
							for (String name : names) {
								if (!tName.contains(name)) 
									checkEntity = false;
							}
						}
						System.out.println("w"+t.entity+entity+checkEntity);
						if (checkEntity) {
							if (!isEvent) {
								if (t.time.equals(TIMESTAMP_PREFIX+questionTime)) {
									ans = sets.get(t.value.name).cardinality;
									totalAns = ans + "+" + totalAns;
									questionEntity = entity;
								}
							}
							else {
								ans = sets.get(t.value.name).cardinality;
								totalAns = ans + "+" + totalAns;
								questionEntity = entity;
							}
						}
					}
					System.out.println(ans+"aaaaa"+totalAns);
					if (!ans.isEmpty()) {
						finalAns = questionOwner + " " + questionVerb + " " + EquationSolver.getSolution(ans) + " " + questionEntity;
						return;	
					}
					if (!totalAns.isEmpty() && ans.isEmpty() && isQuestionAggregator)
						ans = totalAns;
					if (!ans.isEmpty() && ans.endsWith("+"))
						ans = ans.substring(0,ans.length()-1);
					boolean checkques = question.contains(EquationSolver.getSolution(ans).replace(".0", ""));
					//System.out.println("aaaaa"+ans+"|"+checkques+"|"+EquationSolver.getSolution(ans));
					if (!ans.isEmpty() && !checkques) {
						finalAns = questionOwner + " " + questionVerb + " " + EquationSolver.getSolution(ans) + " " + questionEntity;
						return;
					}
					//continue;
				}
				System.out.println("aabb");
			}
			System.out.println("aa");
			it1 = story.entrySet().iterator();
			entity = ""; ans = "";
			while (it1.hasNext()) {
				Entry<String,Situation> entry = it1.next();
				String owner = entry.getKey();
				if (owner.isEmpty())
					continue;
				Situation currentSituation = entry.getValue();
				Iterator<Entry<String, State>> it = currentSituation.entrySet().iterator();
				totalAns = "";
				while (it.hasNext()) {
					Entry<String, State> pair = it.next();
					State candidate = pair.getValue();
					String verb = pair.getKey();
					System.out.println(verb+"|"+candidate.get(0).value.name);
					isEvent = keywordMap.containsKey(verb);
					for (TimeStamp t : candidate) {
						if (questionEntity.isEmpty())
							entity = t.entity;
						else
							entity = questionEntity;
						System.out.println(verb+"|"+t.value.cardinality);
						
						String checkAns = sets.get(t.value.name).cardinality;
						if (checkAns.contains("x") || t.value.name.contains(Set.Empty.name+"-") || checkAns.contains("0+") && question.contains(EquationSolver.getSolution(checkAns).replace(".0", "")))
							continue;
						if (question.contains(checkAns) && isEvent)
							continue;
						if (question.contains(checkAns) && !isEvent && question.contains(questionOwner + " " + verb +" "+checkAns))
							continue;
						System.out.println("waka");
						boolean checkEntity = false;
						if ((t.entity.contains(entity) || entity.contains(t.entity)))
								checkEntity = true;
						else {
							String tName = t.entity.toLowerCase();
							String[] names = entity.toLowerCase().split(" ");
							checkEntity = true;
							for (String name : names) {
								if (!tName.contains(name))
									checkEntity = false;
							}
						}
						System.out.println(checkEntity);
						if (checkEntity) {
							if (!isEvent) {
								if (t.time.equals(TIMESTAMP_PREFIX+questionTime)) {
									ans = sets.get(t.value.name).cardinality;
									totalAns = ans + "+" + totalAns;
									//questionEntity = entity;
								}
							}
							else {
								ans = sets.get(t.value.name).cardinality;
								//questionEntity = entity;
							}
						}
					}
					System.out.println("aa"+ans);
					if (!ans.isEmpty() && !question.contains(ans)) {
						questionOwner = owner;
						questionVerb = verb;
						finalAns = questionOwner + " " + questionVerb + " " + EquationSolver.getSolution(ans) + " " + questionEntity;
						return;
					}
					System.out.println("aa"+ans);
					if (totalAns.endsWith("+"))
						totalAns = totalAns.substring(0,totalAns.length()-1);
					ans = totalAns;
					if (!ans.isEmpty()) {
						questionOwner = owner;
						questionVerb = verb;
						finalAns = questionOwner + " " + questionVerb + " " + EquationSolver.getSolution(ans) + " " + questionEntity;
						return;
					}
				}
			}
			System.out.println("aa");
			if (ans.isEmpty()) {
				System.out.println(question);
				Pattern numPattern = Pattern.compile("\\s\\d*\\.?\\d+\\s");
				Matcher varMatcher = numPattern.matcher(question);
				String sum = "0";
				while (varMatcher.find()) {
					sum = sum + "+" + varMatcher.group();
					System.out.println(sum);
				}
				System.out.println(sum);
				if (questionEntity.isEmpty() && !entities.isEmpty())
					questionEntity = entities.iterator().next();
				finalAns = "Altogether " + EquationSolver.getSolution(sum) + " " + questionEntity;
			}
			return;
		}
	
		State ansState = story.get(questionOwner).get(questionVerb);
		if (ansState == null && story.containsKey(questionOwner)) {
			Iterator<Entry<String, State>> it = story.get(questionOwner).entrySet().iterator();
			while (it.hasNext()) {
				questionVerb = it.next().getKey();
				if (!questionVerb.equals("has"))
					break;
			}
		}
		System.out.println(questionVerb+"AA");
		ansState = story.get(questionOwner).get(questionVerb);
		String ans = "", entity = "";
		isEvent = keywordMap.containsKey(questionVerb.substring(0,questionVerb.length()-1));
		for (TimeStamp t : ansState) {
			if (questionEntity.isEmpty())
				entity = t.entity;
			else
				entity = questionEntity;
			String checkAns = sets.get(t.value.name).cardinality; 
			if (checkAns.contains("x") || t.value.name.contains(Set.Empty.name+"-"))
				continue;
			System.out.println(sets.get(t.value.name).cardinality+t.time+entity);
			t.entity = t.entity.toLowerCase();
			entity = entity.toLowerCase();
			boolean checkEntity = false;
			if ((t.entity.contains(entity) || entity.contains(t.entity)))
					checkEntity = true;
			else {
				String tName = t.entity.toLowerCase();
				String[] names = entity.toLowerCase().split(" ");
				checkEntity = true;
				for (String name : names) {
					System.out.println(name+"|"+tName);
					if (!tName.contains(name))
						checkEntity = false;
				}
			}
			System.out.println(checkEntity);
			if (checkEntity) {
				if (!isEvent) {
					if (t.time.equals(TIMESTAMP_PREFIX+questionTime)) {
						ans = sets.get(t.value.name).cardinality + "+" + ans;
						questionEntity = entity;
					}
				}
				else {
					ans = sets.get(t.value.name).cardinality + "+" + ans;
					questionEntity = entity;
				}
			}	
		}
		if (ans.endsWith("+"))
			ans = ans.substring(0,ans.length()-1);
		System.out.println(ans);
		if (question.contains(ans) && !questionVerb.equals("has")  || question.contains(EquationSolver.getSolution(ans).replace(".0", "")) && question.contains("0+") || ans.isEmpty()) {
			questionVerb = "has";
			ansState = story.get(questionOwner).get(questionVerb);
			System.out.println("ccc");
			if (ansState == null)
				questionVerb = story.get(questionOwner).entrySet().iterator().next().getKey();
			ansState = story.get(questionOwner).get(questionVerb);
			ans = ""; entity = "";
			for (TimeStamp t : ansState) {
				if (questionEntity.isEmpty())
					entity = t.entity;
				else
					entity = questionEntity;
				if (sets.get(t.value.name).cardinality.contains("x") || t.value.name.contains(Set.Empty.name+"-"))
					continue;
				boolean checkEntity = false;
				if ((t.entity.contains(entity) || entity.contains(t.entity)))
						checkEntity = true;
				else {
					String tName = t.entity.toLowerCase();
					String[] names = entity.toLowerCase().split(" ");
					checkEntity = true;
					for (String name : names) {
						if (!tName.contains(name))
							checkEntity = false;
					}
				}
				
				System.out.println("ans"+questionTime+sets.get(t.value.name).cardinality);
				if (checkEntity) {
					if (t.time.equals(TIMESTAMP_PREFIX+questionTime)) {
						ans = sets.get(t.value.name).cardinality + "+" + ans;
						questionEntity = entity;
					}
				}		
			}
			if (ans.endsWith("+"))
				ans = ans.substring(0,ans.length()-1);
		}
		System.out.println("prefinal"+ans);
		if (question.contains(ans) || ans.isEmpty()) {
			Iterator<Entry<String, State>> it = story.get(questionOwner).entrySet().iterator();
			String temp = questionVerb;
			while (it.hasNext()) {
				questionVerb = it.next().getKey();
				if (!questionVerb.equals("has") && !questionVerb.equals(temp))
					break;
			}
			ansState = story.get(questionOwner).get(questionVerb);
			ans = ""; entity = "";
			for (TimeStamp t : ansState) {
				if (questionEntity.isEmpty())
					entity = t.entity;
				else
					entity = questionEntity;
				if (sets.get(t.value.name).cardinality.contains("x") || t.value.name.contains(Set.Empty.name+"-") || question.contains(EquationSolver.getSolution(sets.get(t.value.name).cardinality).replace(".0", "")))
					continue;
				System.out.println(sets.get(t.value.name).cardinality);
				boolean checkEntity = false;
				if ((t.entity.contains(entity) || entity.contains(t.entity)))
						checkEntity = true;
				else {
					String tName = t.entity.toLowerCase();
					String[] names = entity.toLowerCase().split(" ");
					checkEntity = true;
					for (String name : names) {
						if (!tName.contains(name))
							checkEntity = false;
					}
				}
				if (checkEntity) {
					ans = sets.get(t.value.name).cardinality;
					questionEntity = entity;
				}		
			}
		}
		System.out.println("vv"+ans+questionOwner+questionOwner2+questionVerb);
		questionVerb = verbTemp;
		if (questionOwner2.isEmpty() || questionOwner.equals(questionOwner2)) {
			for (String owner : owners) {
				if (!owner.equals(questionOwner)) {
					questionOwner2 = owner;
					break;
				}
			}
		}
		System.out.println("vv"+ans+questionOwner+questionOwner2);
		if ((question.contains(ans) || ans.isEmpty()) && !questionOwner2.isEmpty() && story.containsKey(questionOwner2)) {
			questionOwner = questionOwner2;
			System.out.println(questionOwner2 + questionVerb);
			ansState = story.get(questionOwner).get(questionVerb);
			Iterator<Entry<String, State>> it2 = story.get(questionOwner).entrySet().iterator();
			String verb = questionVerb;
			while ((ansState == null || verb.equals("has")) && it2.hasNext()) {
				Entry<String,State> pair = it2.next();
				verb = pair.getKey();
				if (!verb.equals("has"))
					ansState = pair.getValue();
			}
			ans = ""; entity = "";
			questionVerb = verb;
			for (TimeStamp t : ansState) {
				if (questionEntity.isEmpty())
					entity = t.entity;
				else
					entity = questionEntity;
				boolean checkEntity = false;
				if ((t.entity.contains(entity) || entity.contains(t.entity)))
						checkEntity = true;
				else {
					String tName = t.entity.toLowerCase();
					String[] names = entity.toLowerCase().split(" ");
					checkEntity = true;
					for (String name : names) {
						if (!tName.contains(name))
							checkEntity = false;
					}
				}
				if (sets.get(t.value.name).cardinality.contains("x"))
					continue;
				if (checkEntity) {
					if (!isEvent) {
						if (t.time.equals(TIMESTAMP_PREFIX+questionTime)) {
							ans = sets.get(t.value.name).cardinality;
							questionEntity = entity;
						}
					}
					else {
						ans = sets.get(t.value.name).cardinality;
						questionEntity = entity;
					}
				}
			}
			
		}
		System.out.println(ans);
		if (ans.isEmpty()) {
			Iterator<Entry<String, State>> it = story.get(questionOwner).entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, State> entry = it.next();
				String verb = entry.getKey();
				ansState = entry.getValue();
				for (TimeStamp t : ansState) {
					if (questionEntity.isEmpty())
						entity = t.entity;
					else
						entity = questionEntity;
					if (sets.get(t.value.name).cardinality.contains("x") || t.value.name.contains(Set.Empty.name+"-"))
						continue;
					boolean checkEntity = false;
					if ((t.entity.contains(entity) || entity.contains(t.entity)))
							checkEntity = true;
					else {
						String tName = t.entity.toLowerCase();
						String[] names = entity.toLowerCase().split(" ");
						checkEntity = true;
						for (String name : names) {
							if (!tName.contains(name))
								checkEntity = false;
						}
					}
					isEvent = !verb.contains("has");
					System.out.println(sets.get(t.value.name).cardinality + verb + "|" + t.entity + entity + t.time + isEvent + questionTime);
					if (checkEntity) {
						if (!isEvent) {
							if (t.time.equals(TIMESTAMP_PREFIX+questionTime)) {
								ans = sets.get(t.value.name).cardinality;// + "+" + ans;
								questionEntity = entity;
							}
						}
						else {
							ans = sets.get(t.value.name).cardinality;//+ "+" + ans;
							questionEntity = entity;
						}
					}		
				}
				//if (!ans.isEmpty())
					//break;
			}
		}
		System.out.println("final"+ans);
		if (ans.endsWith("+"))
			ans = ans.substring(0,ans.length() - 1);
		if (ans.contains("x") || ans.isEmpty()) {
			Iterator<Entry<String, State>> it = story.get(questionOwner).entrySet().iterator();
			while (it.hasNext()) {
				questionVerb = it.next().getKey();
				if (!questionVerb.equals("has"))
					break;
			}
		} else if (!question.contains(ans)){
			finalAns = questionOwner + " " + questionVerb + " " + EquationSolver.getSolution(ans) + " " + questionEntity;
			return;
		}
		if (isQuestionComparator) {
			questionOwner1 = questionOwner;
			if (!story.containsKey(questionOwner1)) 
				questionOwner1 = story.entrySet().iterator().next().getKey();
			if (questionOwner2.isEmpty() || !story.containsKey(questionOwner2)) {
				questionOwner2 = "";
				Iterator<Entry<String, Situation>> it = story.entrySet().iterator();
				while (it.hasNext()) {
					String potentialOwner = it.next().getKey();
					if (!potentialOwner.equals(questionOwner1))
						questionOwner2 = potentialOwner;
				}
			}
			System.out.println("aa"+questionOwner2+questionOwner1);
			if (questionOwner2.isEmpty()) {
				questionOwner2 = questionOwner1;
				isEvent = true;
			}
			if (!story.get(questionOwner1).containsKey(questionVerb))
				questionVerb = story.get(questionOwner1).entrySet().iterator().next().getKey();
			State currentState = story.get(questionOwner1).get(questionVerb);
			String v1 = "", v2 = "";
			for (TimeStamp t : currentState) {
				if (!isEvent && !t.time.equals(TIMESTAMP_PREFIX+questionTime))
					continue;
				if (sets.get(t.value.name).cardinality.contains("x") || sets.get(t.value.name).components.containsKey(Set.Empty))
					continue;
				v1 = sets.get(t.value.name).cardinality;
			}
			if (!story.get(questionOwner2).containsKey(questionVerb))
				questionVerb = story.get(questionOwner2).entrySet().iterator().next().getKey();
			currentState = story.get(questionOwner2).get(questionVerb);
			for (TimeStamp t : currentState) {
				if (!isEvent && !t.time.equals(TIMESTAMP_PREFIX+questionTime))
					continue;
				if (sets.get(t.value.name).cardinality.contains("x") || sets.get(t.value.name).components.containsKey(Set.Empty))
					continue;
				if (!sets.get(t.value.name).cardinality.equals(v1))
					v2 = sets.get(t.value.name).cardinality;
			}
			if (v2.isEmpty()) {
				Iterator<Entry<String, State>> it = story.get(questionOwner2).entrySet().iterator();
				while (it.hasNext()) {
					String verb = it.next().getKey();
					if (!questionVerb.equals(verb)) {
						questionVerb = verb;
						break;
					}
				}
				currentState = story.get(questionOwner2).get(questionVerb);
				for (TimeStamp t : currentState) {
					if (!isEvent && !t.time.equals(TIMESTAMP_PREFIX+questionTime))
						continue;
					if (sets.get(t.value.name).cardinality.contains("x") || sets.get(t.value.name).components.containsKey(Set.Empty))
						continue;
					if (!sets.get(t.value.name).cardinality.equals(v1))
						v2 = sets.get(t.value.name).cardinality;
				}
				
			}
			ans = "";
			System.out.println("hi"+v1+"|"+v2);
			if (v1.contains("+") || v1.contains("-") || v2.contains("+") || v2.contains("-")|| v1.isEmpty() || v2.isEmpty()) {
				Pattern numPattern = Pattern.compile("\\d*\\.?\\d+");
				Matcher varMatcher = numPattern.matcher(question);
				v1 = ""; v2 = "";
				String val = "";
				while (varMatcher.find()) {
					val = varMatcher.group();
					if (v1.isEmpty() && question.contains(" " + val + " " + questionEntity))
						v1 = val;
					else if (!v1.isEmpty() && question.contains(" " + val + " " + questionEntity))
						v2 = val;
				} 
			}
			System.out.println("hi"+v1+"|"+v2);
			if (v1.isEmpty() || v2.isEmpty()) {
				Pattern numPattern = Pattern.compile("\\d*\\.?\\d+");
				Matcher varMatcher = numPattern.matcher(question);
				v1 = ""; v2 = "";
				String val = "";
				while (varMatcher.find()) {
					val = varMatcher.group();
					if (v1.isEmpty())
						v1 = val;
					else 
						v2 = val;
				}
			}
			if (Double.parseDouble(v1) > Double.parseDouble(v2))
				ans = v1 + "-" + v2;
			else
				ans = v2 + "-" + v1;
			finalAns = questionOwner1 + " " + questionVerb + " " + EquationSolver.getSolution(ans) + " " + questionEntity + "than" + questionOwner2;
			return;
		}
		isEvent = keywordMap.containsKey(questionVerb.substring(0,questionVerb.length()-1));
		ans = "";
		ansState = story.get(questionOwner).get(questionVerb);
		for (TimeStamp t : ansState) {
			if (t.entity.equals(questionEntity)) {
				if (!isEvent) {
					if (t.time.equals(TIMESTAMP_PREFIX+questionTime))
						ans = sets.get(t.value.name).cardinality;
				}
				else
					ans = sets.get(t.value.name).cardinality;
			}		
		}
		if (ans.isEmpty() || ans.contains("x")) {
			if(!questionOwner.isEmpty() && attempts == 0) {
			questionOwner = "";attempts= 1;
			solve();}
			return;
		}
		finalAns = questionOwner + " " + questionVerb + " " + EquationSolver.getSolution(ans) + " " + questionEntity;
	}

	private static boolean doesStory(String verb) {
		Iterator<Entry<String, Situation>> it1 = story.entrySet().iterator();
		while (it1.hasNext()) {
			Entry<String,Situation> entry = it1.next();
			String owner = entry.getKey();
			Situation currentSituation = entry.getValue();
			if (currentSituation.containsKey(verb))
				return true;
		}
		return false;
	}
}