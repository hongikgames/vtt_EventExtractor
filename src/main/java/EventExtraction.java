import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.LabeledScoredConstituentFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraph.OutputFormat;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class EventExtraction {
	public static void main(String[] args) {
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, ner, coref");

	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    CoreDocument exampleDocument = null;

		//	dependencies parse
        LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });
	    
        //	dependencies parse pos-tag
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        
	    try {
			File file = new File("E:\\u.hyeyeon\\Project\\stanfordNLP\\corenlp\\results\\rocstory_simple_sentence_dash.csv");
//			File file = new File("E:\\u.hyeyeon\\Project\\stanfordNLP\\corenlp\\results\\plot_simple_sentence_dash.csv");
			FileReader filereader = new FileReader(file);
			
			BufferedReader bufReader = new BufferedReader(filereader);

			String line = "";
			List<String> list = null;

			int a = 0;
			
			while((line = bufReader.readLine()) != null) {
				
				//	0 : id, 1 : plot (plot per one movie ID)
				list = new ArrayList<String>(Arrays.asList(line.split("\\t")));
				
				if (list.size() != 3) {
					//System.out.println(line);
					System.out.println(list.get(0));
				} else {
				    exampleDocument = new CoreDocument(list.get(2));
				    pipeline.annotate(exampleDocument);
				    
				    for (CoreSentence sentence : exampleDocument.sentences()) {
				        List<CoreLabel> rawWords = SentenceUtils.toCoreLabelList(sentence.tokens());
				        Tree parse = lp.apply(rawWords);
				        
				        
				        
				        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);	        
				        Collection<TypedDependency> tdlBasic = gs.typedDependencies();
				        Collection<TypedDependency> tdlCollapsed = gs.typedDependenciesEnhanced();	//	Collapsed
				        
				        System.out.println(sentence.toString());
				        System.out.println(tdlBasic);
				        System.out.println(tdlCollapsed);
				        System.out.println(sentence.nerTags().toString());
				        System.out.println(sentence.dependencyParse());
				    }
				    a++;
				    if (a == 50) break;
				}
			}
			
			bufReader.close();
			filereader.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println(e);
		}
	}
	
	private static ArrayList GetSimpleSentences(String inputText) {
		ArrayList retText = new ArrayList();
		
		//	sentence tokenize
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, depparse");
	    	    
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    CoreDocument exampleDocument = new CoreDocument(inputText);
	    pipeline.annotate(exampleDocument);

		//	dependencies parse
        LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });

        //	dependencies parse pos-tag
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

	    // 1. Each sentence of plot
	    for (CoreSentence sentence : exampleDocument.sentences()) {
	    	System.out.println(sentence);
	    	
	        List<CoreLabel> rawWords = SentenceUtils.toCoreLabelList(sentence.tokens());
	        Tree parse = lp.apply(rawWords);
	        //parse.pennPrint();

	        //SemanticGraph dependencyParse = sentence.dependencyParse();
	        //List<String> sentenceTag = Arrays.asList(dependencyParse.toList().split("\n"));
	        //System.out.println(dependencyParse.toString(SemanticGraph.OutputFormat.READABLE));
	        //System.out.println(dependencyParse.toString(SemanticGraph.OutputFormat.LIST));

	        // 2. Stanford Dependency 
	        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);	        
	        Collection<TypedDependency> tdlBasic = gs.typedDependencies();
	        Collection<TypedDependency> tdlCollapsed = gs.typedDependenciesEnhanced();	//	Collapsed
	        System.out.println(tdlBasic);
	        //System.out.println(tdlCollapsed);
	        //	[nsubj(realize-24, Shlykov-1), det(driver-6, a-3), amod(driver-6, hard-working-4), compound(driver-6, taxi-5), conj:and(Shlykov-1, driver-6), nsubj(realize-24, driver-6), cc(Shlykov-1, and-7), conj:and(Shlykov-1, Lyosha-8), nsubj(realize-24, Lyosha-8), det(saxophonist-11, a-10), appos(Lyosha-8, saxophonist-11), ccomp(realize-24, develop-13), det(relationship-17, a-14), amod(love-hate-16, bizarre-15), amod(relationship-17, love-hate-16), dobj(develop-13, relationship-17), cc(relationship-17, and-19), amod(prejudices-22, despite-20), nmod:poss(prejudices-22, their-21), dobj(develop-13, prejudices-22), conj:and(relationship-17, prejudices-22), root(ROOT-0, realize-24), nsubj(different-29, they-25), cop(different-29, are-26), neg(different-29, n't-27), advmod(different-29, so-28), ccomp(realize-24, different-29), case(all-31, after-30), nmod:after(different-29, all-31)]
	        
	        
	        // 3. MSD Generation
	        Collection<TypedDependency> tdlSubj = tdlCollapsed.stream().filter(s->s.reln().toString().startsWith("nsubj")).collect(Collectors.toList());
	        Collection<TypedDependency> tdlMSD = tdlBasic.stream().filter(s->!s.reln().toString().startsWith("nsubj")).collect(Collectors.toList());
	        tdlMSD.addAll(tdlSubj);
	        
	        tdlMSD = tdlMSD.stream().filter(s->!s.reln().toString().startsWith("acl")).filter(s->!s.reln().toString().startsWith("appos")).filter(s->!s.reln().toString().startsWith("advcl"))
	        		.filter(s->!s.reln().toString().startsWith("cc")).filter(s->!s.reln().toString().startsWith("ccomp")).filter(s->!s.reln().toString().startsWith("conj")).filter(s->!s.reln().toString().startsWith("dep"))
	        		.filter(s->!s.reln().toString().startsWith("mark")).filter(s->!s.reln().toString().startsWith("parataxis")).filter(s->!s.reln().toString().startsWith("ref")).collect(Collectors.toList());
	        System.out.println(tdlMSD);
	        //Collection<TypedDependency> newTdl = tdlSubj + tdlDepn;

	        // 4. collapsed dependencies 중에 nsubj로 시작하는 개수 (nsubj, nsubjpass)
	        long subjNum = tdlCollapsed.stream().filter(s->s.reln().toString().startsWith("nsubj")).count();
	        if (subjNum > 1) {											//	complex or compound sentences
	        	//	simple sentences 만들기... 어떻게??
	        	while (subjNum > 0) {
	        		
	        		subjNum--;
	        	}
	        	
	        	System.out.println(subjNum);
	        } else {													//	Simple Sentence
	        	System.out.println("Simple : "+sentence.toString());
	        	retText.add(sentence.toString());
	        }
	        
	    }
	    
	    return retText;
	}
	
	public static Boolean inKeyword(String currentSentence){ 
	    String[] keyword = {"nsubj","nsubpass"}; 

	    for(String each: keyword){ 
	     if(currentSentence.toLowerCase().contains(each)) return true; 
	    } 

	    return false; 
	} 
	
	
	
    public static void GetSentences(String inputText){		//	inputText는 영화 한 편의 plot
    	
    	List<String> retVal = null;
    	
    	Document document = new Document(inputText);
    	
    	Properties props = new Properties();
    	props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse");
//    	props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
		props.setProperty("coref.algorithm", "neural");
    	
    	
		props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
	    props.setProperty("parse.maxlen", "100");
	    
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    	

//      String sentenceText = document.sentences().get(0).text();
//      System.out.println("Example: sentence");
//      System.out.println(sentenceText);
//      System.out.println();

    	if (document.sentences().size() > 0) {
    		for (Sentence sent : document.sentences()) {
    			System.out.println("orignal : " + sent);
    			System.out.println("tokenize : " + sent.words());
    			System.out.println("lemmas : " + sent.lemmas());
    			System.out.println("parse : " + sent.parse());
    			System.out.println();
    			
    			Annotation annotation = new Annotation(sent.toString());
    			pipeline.annotate(annotation);
    			
    			Tree tree = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
    			System.out.println("tree : " + tree);
    			
    			Set<Constituent> treeConstituents = tree.constituents(new LabeledScoredConstituentFactory());
				for (Constituent constituent : treeConstituents) {
					if (constituent.label() != null &&
						(constituent.label().toString().equals("VP") || constituent.label().toString().equals("NP"))) {
						System.err.println("found constituent: "+constituent.toString());
						System.err.println(tree.getLeaves().subList(constituent.start(), constituent.end()+1));
						
						System.out.println();
					}
				}
    		}
    		
    	}
    	

//        return retVal;
    }	
}
