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
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;

public class makeLemma {
	private static StanfordCoreNLP pipeline;
	private static Properties props;
	private static LexicalizedParser lp;
	private static GrammaticalStructureFactory gsf;
	
	
	public static void main(String[] args) {
		try {
			File rfile = new File("E:\\u.hyeyeon\\Project\\stanfordNLP\\corenlp\\results\\rocstory_coref_simple_sentence_iteration_svo_ner.csv");
			FileReader filereader = new FileReader(rfile);
			BufferedReader bufReader = new BufferedReader(filereader);
			
			FileOutputStream wfile = new FileOutputStream("E:\\u.hyeyeon\\Project\\stanfordNLP\\corenlp\\results\\rocstory_coref_simple_sentence_iteration_svo_ner_lemma.csv");
			BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(wfile,Charset.forName("UTF-8")));
			
    	    props = new Properties();
    	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
    	    pipeline = new StanfordCoreNLP(props);
    	    
			String line = "";
			List<String> list = null;
    	    Document doc = null;
    	    Sentence sent = null;
			String reText = null;
			
    	    while((line = bufReader.readLine()) != null) {
    	    	list = Arrays.asList(line.split("\t"));
    	    	
				//	header continue
				if (list.get(0).equals("storyid")) {
					bufWriter.write("storyid\tseq\tsub\tverb\tverb_lemma\td_obj\ti_obj\tsubj_pp\trelation_pp\trelation_pp_lemma\tpp\r\n");
					bufWriter.flush();
					continue;
					
				} else {
					//	stroryid, seq, sub, verb
					reText = list.get(0)+"\t"+list.get(1)+"\t"+list.get(2)+"\t"+list.get(3)+"\t";

					//	verb
	    	    	if (list.get(3).length() > 0  && list.get(3) != null && !list.get(3).contentEquals("")) {
		    	    	sent = new Sentence(list.get(3));
	    	    		
	    	    		reText += String.join(" ", sent.lemmas())+"\t";  
	    	    	} else {
	    	    		reText += "\t";
	    	    	}
    	    		reText += list.get(4)+"\t"+list.get(5)+"\t"+list.get(6)+"\t"+list.get(7)+"\t";

					//	relation_pp
	    	    	if (list.get(7).length() > 0  && list.get(7) != null && !list.get(7).contentEquals("")) {
		    	    	sent = new Sentence(list.get(7));
	    	    		
	    	    		reText += String.join(" ", sent.lemmas())+"\t";
	    	    	} else {
	    	    		reText += "\t";
	    	    	}
	    	    	
	    	    	if (list.size() > 8) 
	    	    		reText += list.get(8)+"\n";
	    	    	else
	    	    		reText += "\n";
    	    	}
				
				System.out.print(reText);

				//if (list.get(0).equals("c71bb23b-7731-4233-8298-76ba6886cee1")) break;
				bufWriter.write(reText);
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
