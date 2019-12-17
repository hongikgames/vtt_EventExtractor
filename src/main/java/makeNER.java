import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;

public class makeNER {
	private static StanfordCoreNLP pipeline;
	private static Properties props;
	private static LexicalizedParser lp;
	private static GrammaticalStructureFactory gsf;
	
	
	public static void main(String[] args) {
		try {
			File rfile = new File("E:\\u.hyeyeon\\Project\\stanfordNLP\\corenlp\\results\\rocstory_simple_sentence_iteration_svo.csv");
			FileReader filereader = new FileReader(rfile);
			BufferedReader bufReader = new BufferedReader(filereader);
			
			FileOutputStream wfile = new FileOutputStream("E:\\u.hyeyeon\\Project\\stanfordNLP\\corenlp\\results\\rocstory_simple_sentence_iteration_svo_ner.csv");
			BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(wfile,Charset.forName("UTF-8")));
			
    	    props = new Properties();
    	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
    	    pipeline = new StanfordCoreNLP(props);

    	    
			String line = "";
			List<String> list = null;
    	    CoreDocument doc = null;
			String reText = null;
			
    	    while((line = bufReader.readLine()) != null) {
    	    	list = Arrays.asList(line.split("\t"));
    	    	
				//	header continue
				if (list.get(0).equals("storyid")) {
					bufWriter.write("storyid\tseq\tsub\tverb\td_obj\ti_obj\tsubj_pp\trelation_pp\tpp\r\n");
					bufWriter.flush();
					continue;
					
				} else {
					reText = list.get(0)+"\t"+list.get(1)+"\t";

					for (int i=2;i<list.size();i++) {
	    	    		if (list.get(i).length() > 0  && list.get(i) != null && !list.get(i).contentEquals("")) {
		    	    		doc = new CoreDocument(list.get(i));
		    	    		pipeline.annotate(doc);
		    	    		
		    	    		if (doc.entityMentions().size() > 0) {
		    	    			for (CoreEntityMention em : doc.entityMentions()) 
		    	    				reText += list.get(i).replace(em.text(), em.text()+"["+em.entityType()+"]");
		    	    		} else {
		    	    			reText += list.get(i);	
		    	    		}
	    	    		}
	    	    		reText += "\t";
	    	    	}
					
					
					System.out.println(reText.substring(0,reText.length()-1));
				}
				
				bufWriter.write(reText+"\n");
				bufWriter.flush();
				reText = "";
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

}
