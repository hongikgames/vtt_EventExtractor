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

import javax.lang.model.element.Element;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.lucene.store.Directory;
import org.apache.xalan.lib.sql.ObjectArray;
import nu.xom.*;
import edu.stanford.nlp.util.TypesafeMap.Key;

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
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraph.OutputFormat;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;


import org.apache.commons.lang3.text.WordUtils;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;



public class makeSimpleSentenceROCStory {
	
	private static StanfordCoreNLP pipeline;
	private static Properties props;
	private static LexicalizedParser lp;
	private static GrammaticalStructureFactory gsf;
	private static ArrayList retText = new ArrayList();
	
	public static void main(String[] args) {
		//	coref, none (
		String runType = "coref";
		FileOutputStream wfile = null;
		BufferedWriter bufWriter = null;
		FileOutputStream wfileCoRef = null;
		BufferedWriter bufWriterCoRef = null;
		
		try {
			
			
			File rfile = new File("E:\\u.hyeyeon\\Project\\dataset\\ROCStories\\ROCStories_winter2017 - ROCStories_winter2017_re.csv");
			FileReader filereader = new FileReader(rfile);
			BufferedReader bufReader = new BufferedReader(filereader);
			
			if (runType.equals("none")) {
				wfile = new FileOutputStream("E:\\u.hyeyeon\\Project\\stanfordNLP\\corenlp\\results\\rocstory_simple_sentence_iteration.csv");
	            bufWriter = new BufferedWriter(new OutputStreamWriter(wfile,Charset.forName("UTF-8")));
			} else if (runType.equals("coref")) {
				wfileCoRef = new FileOutputStream("E:\\u.hyeyeon\\Project\\stanfordNLP\\corenlp\\results\\rocstory_simple_sentence_coref_iteration.csv");
				bufWriterCoRef = new BufferedWriter(new OutputStreamWriter(wfileCoRef,Charset.forName("UTF-8")));
			}
			
    		/* StanfordCoreNLP 관련 선언 ==========================================*/
    		//	sentence tokenize
    	    props = new Properties();
    	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, depparse, coref");
    	    pipeline = new StanfordCoreNLP(props);            
            
    		//	dependencies parse
            lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
            lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });

            //	dependencies parse pos-tag
            TreebankLanguagePack tlp = new PennTreebankLanguagePack();
            gsf = tlp.grammaticalStructureFactory();
            /* ========================================== StanfordCoreNLP 관련 선언 */
            
			String line = "";
			List<String> list = null;
			
			List<String> simpleSent = new ArrayList(); 
			List<String> simpleSentCoRef = new ArrayList(); 
			while((line = bufReader.readLine()) != null) {
				
				//	0 : id, 1 : plot (plot per one movie ID)
				//		,를 사용하면 문장 중간에 ,가 있을 때 문제가 되고, |를 사용하면 정규식 때문에 문제가 됨
				//System.out.println(String.join(" ", line.split("\t")));
				
				
				list = Arrays.asList(line.split("\t"));
				if (list.size() > 0) {
					//	header continue
					if (list.get(0).equals("storyid")) {
						continue;
					}
					
					System.out.println("ID : "+list.get(0)+" / Title : "+list.get(1));
							
					if (runType.equals("none")) {
						simpleSent = GetSimpleSentencesStory(list.get(2)+" "+list.get(3)+" "+list.get(4)+" "+list.get(5)+" "+list.get(6));
					} else if (runType.equals("coref")) {
						simpleSentCoRef = GetSimpleSentencesStory(getCoReferenceText(list.get(2)+" "+list.get(3)+" "+list.get(4)+" "+list.get(5)+" "+list.get(6)));
					}

					//System.out.println("Final Sentence : "+String.join("\r\n",simpleSent));

					if (runType.equals("none")) {
						for(int i=0; i<simpleSent.size();i++) {
							//id,seq,sentence
							bufWriter.write(list.get(0)+"\t"+i+"\t"+simpleSent.get(i).toString()+"\t"+"\r\n");
						}
						bufWriter.flush();
					} else if (runType.equals("coref")) {
						for(int i=0; i<simpleSentCoRef.size();i++) {
							//id,seq,sentence
							bufWriterCoRef.write(list.get(0)+"\t"+i+"\t"+simpleSentCoRef.get(i).toString()+"\t"+"\r\n");
						}
						bufWriterCoRef.flush();
					}
					//GetSentences(list.get(1));
					//	Line 10
					//if (list.get(0).equals("0d4cecd0-3e87-4042-b064-c7fe913fe1e0")) 
					//	break;						
				}
			}
			
			bufReader.close();
			filereader.close();
			
			if (runType.equals("none")) {
				wfile.close();
				bufWriter.close();
			} else if (runType.equals("coref")) {
				wfileCoRef.close();
				bufWriterCoRef.close();
			}
			
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	//	inputText : story text
	private static ArrayList GetSimpleSentencesStory(String inputText) {
		retText.clear();

	    CoreDocument exampleDocument = new CoreDocument(inputText);
	    pipeline.annotate(exampleDocument);
	    
	    /*
	     * CoReference
	     * 
	     * 
	    Map<Integer, CorefChain> corefChains = exampleDocument.corefChains();
	    List<CoreEntityMention> entities = exampleDocument.entityMentions();  

        Annotation document = new Annotation(inputText);

	    System.out.println("coref : "+corefChains + " / size : "+corefChains.size());
	    System.out.println(entities.toString());
	    System.out.println(document.toString());
	    //System.out.println(coref.toString());
	    
	    if (entities.size() > 0) {
	    	for (int i=0; i<entities.size(); i++) {
	    		System.out.println(i+" : "+entities.get(i));
	    	}
	    	
	    }
	    
        System.out.println("\t\t resolveCoRef =====>>>>> " + resolveCoRef(inputText));
        */
        
	    // 1. Each sentence of story
	    for (CoreSentence sentence : exampleDocument.sentences()) {
	    	//System.out.println();
	    	//System.out.println("sentence : "+sentence);
	    	
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

	        //System.out.println(tdlMSD);
	        
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
    				List<TypedDependency> depList = getDependencyList(tdlMSD, dependency.gov().value().toString().replaceAll( "\\+", "_plus" ), dependency.dep().value().toString().replaceAll( "\\+", "_plus" ));
    				
    				//	simple sentence를 생성하기 위한 List
    				List<TypedDependency> newList = new ArrayList<TypedDependency>();
    				newList.addAll(depList);
    				
    				for(int j=0; j<depList.size(); j++) {
    					depList = getDependencyList(tdlMSD, depList);
        				newList.addAll(depList);
    				}
    				
//    				List<String> nerTags = sentence.nerTags();
//    				sentence.nerTags();
//    				System.out.println("ner Tags : "+nerTags);
//    				
    				
    				//System.out.println("최종 ============");
    				//System.out.println("sentence subj : "+dependency.toString()+"/"+dependency.dep().value().toString());
    				//System.out.println("newList : "+newList);
    				
    				if (newList.size() > 0) {
	    				//	Extract simple sentence
	    				String simpleSent = sortDependenciesByDependentIndex(newList);
	    				//System.out.println("simple sentence ======>>>>> "+simpleSent);
	    				
	    				
	    				//List<String> test = Arrays.asList(simpleSent.split(" "));
	    				//System.out.println(test.get(test.size()-1));
	    				
	    				
	    				
	    				
	    				//	생성된 simple sentence 에서 subject 개수를 다시 확인하고 반복
	    				while (true) {
	    					if (GetSimpleSentences(simpleSent) < 2) {
	    						break;
	    					}
	    				}
	    				
	    				
	    				
	    				
	    				

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
		
		List<TypedDependency> govList = tdl.stream().filter(s->s.gov().value().toString().replace( "*", ".*" ).replace( '?', '.' ).replace("{","").matches(govWord)).collect(Collectors.toList());
		List<TypedDependency> depList = tdl.stream().filter(s->s.gov().value().toString().replace( "*", ".*" ).replace( '?', '.' ).replace("{","").matches(depWord)).collect(Collectors.toList());
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
	
	private static int GetSimpleSentences(String sentence) {
		
		Tree parse = lp.apply(SentenceUtils.toWordList(sentence.split(" ")));
		
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        
        Collection<TypedDependency> chkCollapsed = gs.typedDependenciesEnhanced();
        float chkNum = chkCollapsed.stream().filter(n->n.reln().toString().startsWith("nsubj")).count();
        System.out.println("\t\t\t\t\tCheckSentence Subject Num : "+ chkNum);
        
        if (chkNum > 1) {
	        Collection<TypedDependency> chkBasic = gs.typedDependencies();
	        Collection<TypedDependency> chkSubj = chkCollapsed.stream().filter(s->s.reln().toString().startsWith("nsubj")).collect(Collectors.toList());
	        Collection<TypedDependency> chkMSD = chkBasic.stream().filter(s->!s.reln().toString().startsWith("nsubj")).collect(Collectors.toList());
	        chkMSD.addAll(chkSubj);

	        chkMSD = chkMSD.stream().filter(s->!s.reln().toString().startsWith("acl")).filter(s->!s.reln().toString().startsWith("appos")).filter(s->!s.reln().toString().startsWith("advcl"))
	        		.filter(s->!s.reln().toString().startsWith("cc")).filter(s->!s.reln().toString().startsWith("ccomp")).filter(s->!s.reln().toString().startsWith("conj")).filter(s->!s.reln().toString().startsWith("dep"))
	        		.filter(s->!s.reln().toString().startsWith("mark")).filter(s->!s.reln().toString().startsWith("parataxis")).filter(s->!s.reln().toString().startsWith("ref")).collect(Collectors.toList());

	        List subjList = new ArrayList(chkSubj);
	        for (int i = subjList.size() - 1; i >= 0; i--) {
	        	final TypedDependency dependency = (TypedDependency) subjList.get(i);
	        	List<TypedDependency> depList = getDependencyList(chkMSD, dependency.gov().value().toString().replaceAll( "\\+", "_plus" ), dependency.dep().value().toString().replaceAll( "\\+", "_plus" ));

				List<TypedDependency> newList = new ArrayList<TypedDependency>();
				newList.addAll(depList);
				
				for(int j=0; j<depList.size(); j++) {
					depList = getDependencyList(chkMSD, depList);
    				newList.addAll(depList);
				}	    		    	
				if (newList.size() > 0) {
					String simpleSent = sortDependenciesByDependentIndex(newList);
					if (retText.contains(simpleSent)) {
						continue;
					}
					
					Tree parse2 = lp.apply(SentenceUtils.toWordList(simpleSent));
			        GrammaticalStructure gs2 = gsf.newGrammaticalStructure(parse2);
			        
			        Collection<TypedDependency> tblCollapsed = gs2.typedDependenciesEnhanced();
			        float reNum = tblCollapsed.stream().filter(n->n.reln().toString().startsWith("nsubj")).count();
					
			        
			        System.out.println("\t\t\t"+simpleSent+":"+sentence);
			        if (reNum > 1 && !simpleSent.equals(sentence)) {
			        	GetSimpleSentences(simpleSent);
			        	return (int)reNum;
			        } else {
			        	retText.add(simpleSent);
			        }
				}
        	}
	        
        } else {
        	retText.add(sentence);
        }
    	return 0; 
	}
	
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
	
	
	//	Ref. https://stackoverflow.com/questions/30182138/how-to-replace-a-word-by-its-most-representative-mention-using-stanford-corenlp-coreferences-module
	 private static String getCoReferenceText(String text){
		    Annotation doc = new Annotation(text);
		    pipeline.annotate(doc);

		    Map<Integer, CorefChain> corefs = doc.get(CorefCoreAnnotations.CorefChainAnnotation.class);
		    List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);


		    List<String> resolved = new ArrayList<String>();

		    for (CoreMap sentence : sentences) {

		        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

		        for (CoreLabel token : tokens) {

		            Integer corefClustId= token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
		            //System.out.println(token.word() +  " --> corefClusterID = " + corefClustId);


		            CorefChain chain = corefs.get(corefClustId);
		            //System.out.println("matched chain = " + chain);


		            if(chain==null){
		                resolved.add(token.word());
		                //System.out.println("Adding the same word "+token.word() + " / " + token.tag() + " / " + token.ner());
		            }else{

		                int sentINdx = chain.getRepresentativeMention().sentNum -1;
		                //System.out.println("sentINdx :"+sentINdx);
		                CoreMap corefSentence = sentences.get(sentINdx);
		                List<CoreLabel> corefSentenceTokens = corefSentence.get(TokensAnnotation.class);
		                String newwords = "";
		                CorefMention reprMent = chain.getRepresentativeMention();
		                //System.out.println("reprMent :"+reprMent);
		                //System.out.println("Token index "+token.index());
		                //System.out.println("Start index "+reprMent.startIndex);
		                //System.out.println("End Index "+reprMent.endIndex);
		                if (token.index() <= reprMent.startIndex || token.index() >= reprMent.endIndex) {

		                        for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
		                            CoreLabel matchedLabel = corefSentenceTokens.get(i - 1);
		                            
		                            if (!token.tag().contentEquals("PRP$")) {
		                            	resolved.add(matchedLabel.word().replace("'s", ""));
		                            } else {
		                            	resolved.add(matchedLabel.word());
		                            }
		                            
		                            System.out.println("matchedLabel : "+matchedLabel.word() + (matchedLabel.tag().equals("PRP$") ? "Yes":"No"));
		                            newwords += matchedLabel.word() + " ";
		                        }
		                 } else {
		                       resolved.add(token.word());
		                        //System.out.println("token.word() : "+token.word());
		                 }



		                System.out.println("converting " + token.word()   + " / " + token.tag() + " / " + token.ner() + " to " + newwords);
		                System.out.println(token.tag().equals("PRP$") ? "Yes":"No");
		                
		                 
		            }


		            //System.out.println();
		            //System.out.println();
		            //System.out.println("-----------------------------------------------------------------");

		        }

		    }


		    String resolvedStr ="";
		    //System.out.println();
		    for (String str : resolved) {
		        resolvedStr+=str+" ";
		    }
		    //System.out.println("-----------------------------------------------------------------");
		    //System.out.println("doTest : "+resolvedStr);

		    return resolvedStr;
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
