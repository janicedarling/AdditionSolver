package nlp_adder;

import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class WordProblemSolver {
	
	public static void main(String[] main) {
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner,parse,dcoref");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		solveWordProblems("Kendall is learning to drive , so this weekend she practiced driving 0.16666666666666666 mile with her mother and another 0.5 mile with her father . How far did Kendall drive in all ? ", pipeline);
	}

	public static String solveWordProblems(String problem, StanfordCoreNLP pipeline) {
		String simplifiedProblem = Parser.parse(problem, pipeline);
		System.out.println("ha"+simplifiedProblem);
	    LinguisticInfo extractedInformation = (new SentencesAnalyzer()).extract(simplifiedProblem, pipeline);
	    extractedInformation = OrderSteps.order(extractedInformation);
	    KnowledgeRepresenter.clear();
	    KnowledgeRepresenter.represent(extractedInformation, simplifiedProblem);
	    KnowledgeRepresenter.solve();
	    System.out.println(KnowledgeRepresenter.finalAns.replace(".0 ", " "));
	    return KnowledgeRepresenter.finalAns.replace(".0 ", "");
	}
}
