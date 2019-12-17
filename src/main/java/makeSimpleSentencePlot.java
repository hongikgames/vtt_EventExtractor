import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.store.Directory;
import org.apache.xalan.lib.sql.ObjectArray;

import edu.stanford.nlp.util.StringUtils;
import jdk.internal.joptsimple.internal.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

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

public class makeSimpleSentencePlot {
	public static void main(String[] args) {
		try {
			File rfile = new File("E:\\u.hyeyeon\\Project\\dataset\\CMU_MovieSummaries\\plot_summaries2.txt");
			FileReader filereader = new FileReader(rfile);
			BufferedReader bufReader = new BufferedReader(filereader);
			
			FileOutputStream wfile = new FileOutputStream("E:\\u.hyeyeon\\Project\\stanfordNLP\\corenlp\\results\\plot_simple_sentence2.csv");
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(wfile,Charset.forName("UTF-8")));
			
            /* StanfordCoreNLP 관련 선언 ==========================================*/
    		//	sentence tokenize
    	    Properties props = new Properties();
    	    props.setProperty("annotators", "tokenize, ssplit, pos, depparse");
    	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    		//	dependencies parse
            LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
            lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });

            //	dependencies parse pos-tag
            TreebankLanguagePack tlp = new PennTreebankLanguagePack();
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            /* ========================================== StanfordCoreNLP 관련 선언 */
            
			String line = "";
			List<String> list = null;
			
			List<String> simpleSent = new ArrayList(); 
			while((line = bufReader.readLine()) != null) {
				
				//	0 : id, 1 : plot (plot per one movie ID)
				list = new ArrayList<String>(Arrays.asList(line.split("\t")));
				if (list.size() > 0) {
					System.out.println(list.get(0));
					simpleSent = GetSimpleSentences(pipeline, lp, gsf, list.get(1));

					System.out.println("Final Sentence : "+String.join("\r\n",simpleSent));
					
					for(int i=0; i<simpleSent.size();i++) {
						bufWriter.write(list.get(0)+"\t"+i+"\t"+simpleSent.get(i).toString()+"\r\n");
					}
					bufWriter.flush();
					//GetSentences(list.get(1));
					//	Line 10
					//if (list.get(0).equals("595909")) 
					//	break;						
				}
			}
			
			bufReader.close();
			filereader.close();
			
			wfile.close();
			bufWriter.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	private static ArrayList GetSimpleSentences(StanfordCoreNLP pipeline, LexicalizedParser lp, GrammaticalStructureFactory gsf, String inputText) {
		ArrayList retText = new ArrayList();
		
	    CoreDocument exampleDocument = new CoreDocument(inputText);
	    pipeline.annotate(exampleDocument);


	    // 1. Each sentence of plot
	    for (CoreSentence sentence : exampleDocument.sentences()) {
	    	System.out.println();
	    	System.out.println("sentence : "+sentence);
	    	
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
	        //System.out.println(tdlBasic);
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
	        
	        // 4. collapsed dependencies 중에 nsubj로 시작하는 개수 (nsubj, nsubjpass), 즉 subject가 2개 이상일 때
	        long subjNum = tdlCollapsed.stream().filter(s->s.reln().toString().startsWith("nsubj")).count();
	        if (subjNum > 1) {			
        		
    	        // 4. 각 simple sentence 만들기 (종속적 관계 단어 모두 찾아서 연결)
    	        //System.out.println("==========>>>>> "+subjNum);
    	        //for (int i = subjList.size() - 1; i >= 0; i--) {
    	        //	System.out.println("subjList ==========>>>>> "+subjList.get(i).toString());    	        
    	        //}
	        	
	        	
    			//	subj 개수만큼 반복
    	        List subjList = new ArrayList(tdlSubj);
    	        for (int i = subjList.size() - 1; i >= 0; i--) {

    				final TypedDependency dependency = (TypedDependency) subjList.get(i);
    				
    				//	 subj에 해당하는 gov, dep 를 포함하는 리스트 생성 (getDependencyList(TypedDependency source, govWord, depWord)
    				//List<TypedDependency> depList = getDependencyList(tdlMSD, dependency.gov().value().toString(), dependency.dep().value().toString());
    				List<TypedDependency> depList = getDependencyList(tdlMSD, dependency.gov().value().toString().replaceAll( "\\+", "_plus" ), dependency.dep().value().toString().replaceAll( "\\+", "_plus" ));
    				
    				//	simple sentence를 생성하기 위한 List
    				List<TypedDependency> newList = new ArrayList<TypedDependency>();
    				newList.addAll(depList);
    				
    				for(int j=0; j<depList.size(); j++) {
    					depList = getDependencyList(tdlMSD, depList);
        				newList.addAll(depList);
    				}
    				
    				//System.out.println("최종 ============");
    				System.out.println("sentence subj : "+dependency.toString());
    				//System.out.println("newList : "+newList);
    				
    				if (newList.size() > 0) {
	    				newList.get(0).dep().value().toString();
	
	    				//	Extract simple sentence
	    				String simpleSent = sortDependenciesByDependentIndex(newList);
	    				retText.add(simpleSent);
	    				System.out.println("simple sentence ======>>>>> "+simpleSent);
    				} else {
    					System.out.println("Dependency List is NULL ====== //////// ");    					
    				}	    				
    			}
    	    	
    			
    			
    			
    			
    			

	        	/*
    	        //	 주어의 개수만큼... tdlMSD  에서  주어의 gov를 dep로 가지고 있는 단어들 가져와서.. 정렬하면 simple한 한 문장
    	        for(TypedDependency dependency : tdlSubj) {
	    	            String rel = dependency.reln().toString();						//	relation (nsubj)
	    	            String gov = dependency.gov().value();							//	governor word, 없으면 -1 (realize)
	    	            String dep = dependency.dep().value();							//	dependent word (Shlykov)
	    	            String depTag = dependency.dep().tag().toString();				//	dependent word - pos tag (NNP)
	    	            int depPos = dependency.dep().beginPosition();					//	dependent word - (-1)
	    	            String depBefore = dependency.dep().before();					//	dependent word - 
	    	            String value = dependency.dep().value();						//	dependent word - (Shlykov)
	    	            String word = dependency.dep().word();							//	dependent word - (Shlykov) 
	    	            String backingLabel = dependency.dep().backingLabel().toString();//	Shlykov-1
	    	            
	    	            System.out.println(dependency.toString());
	    	            System.out.println(dependency.gov().getOriginal().beginPosition());
	    	            //String depTag = dependency.dep().label().tag();
//	    	            Pattern.Relation relation = Pattern.asRelation(rel);
//	    	            if (relation != null) {
//	    	                Pattern pattern = new Pattern(gov, govTag, dep, depTag, relation);
//	    	                if (pattern.isPrimaryPattern()) {
//	    	                    return pattern;
//	    	                }
//	    	            }
	    	            
	    	            System.out.println(rel+":::"+gov+":::"+dep+":::"+depTag+":::"+depPos+":::"+depBefore+":::"+value+":::"+word+":::"+backingLabel);
    	        }
	    	            */
	        } else {													//	Simple Sentence
	        	retText.add(sentence.toString());
	        	System.out.println("simple sentence ======>>>>> "+sentence.toString());
	        }
	        
	    }
	    
	    //	중복제거 (순서 바뀌지 않음)
	    retText.parallelStream().distinct().collect(Collectors.toList());

	    return retText;
	}
	
	public static List<String> removeDuplicates(String[] words) {
        List<String> retWords = new ArrayList();
        
        if(words.length > 1) {
	        retWords.add(words[0]); 
	        		
	        for(int i = 1; i < words.length; i++) {
	            if(words[i-1] == words[i]) {
	            	continue;
	            } else if (words[i] != null) {
	            	retWords.add(words[i]);
	            }
	        }
        }
        return retWords;
	}
	
	
	public static List<TypedDependency> getDependencyList(Collection<TypedDependency> tdl, String govWord, String depWord) {
		
		List<TypedDependency> govList = tdl.stream().filter(s->s.gov().value().toString().replace( "*", ".*" ).replace("?", " ?" ).replace("{","").matches(govWord.replaceAll( "[\\+]", "_plus" ).replace( "*", ".*" ).replace( '?', '.' ).replace(":","").replace("{",""))).collect(Collectors.toList());
		List<TypedDependency> depList = tdl.stream().filter(s->s.gov().value().toString().replace( "*", ".*" ).replace("?", " ?" ).replace("{","").matches(depWord.replaceAll( "[\\+]", "_plus" ).replace( "*", ".*" ).replace( '?', '.' ).replace(":","").replace("{",""))).collect(Collectors.toList());
		depList.addAll(govList);			//	depList 는 simple sentence를 생성하기 위한 word 모음

		//	중복제거
		depList = new ArrayList<TypedDependency>(new LinkedHashSet<TypedDependency>(depList));

		return depList;
	}
	
	public static List<TypedDependency> getDependencyList(Collection<TypedDependency> tdl, List<TypedDependency> depList) {
		List<TypedDependency> retList = new ArrayList<TypedDependency>();
		
		
		for (int i=0; i<depList.size(); i++) {
			int j = i;
			
			try {
				//List<TypedDependency> newList = tdl.stream().filter(s->s.gov().value().toString().replace( "*", ".*" ).replace( '?', '.' ).replace(":","").replace("{","").matches(depList.get(j).dep().value().toString().replace( "*", ".*" ).replace( '?', '.' ).replace(":","").replace("{",""))).collect(Collectors.toList());
				List<TypedDependency> newList = tdl.stream().filter(s->s.gov().value().toString().replace( "*", ".*" ).replace( '?', '.' ).replace(":","").replace("{","").matches(depList.get(j).dep().value().toString().replaceAll( "[\\+]", "_plus" ).replace( "*", ".*" ).replace( '?', '.' ).replace(":","").replace("{",""))).collect(Collectors.toList());
				
				if (newList != null) {
					retList.addAll(newList);
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		//	중복제거
		retList = new ArrayList<TypedDependency>(new LinkedHashSet<TypedDependency>(retList));

		return retList;
	}
	/*
	public static Boolean inKeyword(String currentSentence){ 
	    String[] keyword = {"nsubj","nsubpass"}; 

	    for(String each: keyword){ 
	     if(currentSentence.toLowerCase().contains(each)) return true; 
	    } 

	    return false; 
	} 
	 */
	private static String sortDependenciesByDependentIndex(List<TypedDependency> deps) {
		
		int[] newOrd = new int[deps.size()*2];
		String[] newWord = new String[deps.size()*2];
				
		//	변수 초기화
		int i = 0, count=0;
		for (TypedDependency dep : deps) {	
			
			newOrd[count] = deps.get(i).dep().index();
			newWord[count] = deps.get(i).dep().value().toString();
			count++;
			
			newOrd[count] = deps.get(i).gov().index();
			newWord[count] = deps.get(i).gov().value().toString();
			i++;
			count++;
		}
		
		//	변수 정렬
		int intTmp = 0;
		String strTmp = "", dupChk = "";
		for (i=0; i<(deps.size()*2-1);i++) {
			for (int j=(deps.size()*2-1);j>i;j--) {
				if (newOrd[i] > newOrd[j]) {
					intTmp = newOrd[j];
					newOrd[j] = newOrd[i];
					newOrd[i] = intTmp;

					strTmp = newWord[j];
					newWord[j] = newWord[i];
					newWord[i] = strTmp;
				}
			}	
		}		
		
		//	중복 제거
		return String.join(" ",removeDuplicates(newWord));
	}
	/*
	
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
    */
}
