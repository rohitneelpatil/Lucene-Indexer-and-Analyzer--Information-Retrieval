import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class generateIndex {

	private static String DOCNO = "DOCNO";
	private static String HEAD = "HEAD";
	private static String BYLINE = "BYLINE";
	private static String DATELINE = "DATELINE";
	private static String TEXT = "TEXT";

	// main method where the object for the generateIndex class is instantiated
	public static void main(String[] args) throws Exception {

		// this has the path where the index needs to be created
		File indexDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\Index\\");

		// this is the path from which the documents to be indexed
		File dataDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\Data\\");


		

		// this object will call the index method to generate the indexing
		generateIndex indexObj = new generateIndex();

		indexObj.createIndex(indexDirectory, dataDirectory);

	}

	private void createIndex(File indexDirectory, File dataDirectory)
			throws Exception {

		Directory dir = FSDirectory.open(Paths.get(String.valueOf(indexDirectory)));
		Analyzer indexanalyzer = new StandardAnalyzer();
		

		IndexWriterConfig iwc = new IndexWriterConfig(indexanalyzer);

		iwc.setOpenMode(OpenMode.CREATE);

		// to write the documents to the index
		IndexWriter writer = new IndexWriter(dir, iwc);

		process(writer, dataDirectory);

		writer.forceMerge(1);
		writer.commit();

		System.out.println("Index Commited");
		writer.close();
		System.out.println("Index Closed");
		indexInfo(indexDirectory);
		
		

	}

	
	private void indexInfo(File indexDirectory)throws Exception
	{
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(String.valueOf(indexDirectory))));
		
				System.out.println("Total number of documents in the corpus: "+reader.maxDoc());
				//Print the number of documents containing the term "new" in <field>TEXT</field>.
				System.out.println("Number of documents containing the term \"new\" for field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));
				//Print the total number of occurrences of the term "new" across all documents for <field>TEXT</field>.
				System.out.println("Number of occurrences of \"new\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","new")));
				
				Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
				//Print the size of the vocabulary for <field>TEXT</field>, applicable when the index has only one segment.
				System.out.println("Size of the vocabulary for this field: "+vocabulary.size());
				
				//Print the total number of documents that have at least one term for <field>TEXT</field>
				System.out.println("Number of documents that have at least one term for this field: "+vocabulary.getDocCount());
				//Print the total number of tokens for <field>TEXT</field>
				System.out.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());
				//Print the total number of postings for <field>TEXT</field>
				System.out.println("Number of postings for this field: "+vocabulary.getSumDocFreq());
				//Print the vocabulary for <field>TEXT</field>
//				TermsEnum iterator = vocabulary.iterator();
//				BytesRef byteRef = null;
//				System.out.println("\n*******Vocabulary-Start**********");
//				while((byteRef = iterator.next()) != null) {
//				String term = byteRef.utf8ToString();
//				System.out.print(term+"\t");
//				}
//				System.out.println("\n*******Vocabulary-End**********");
				reader.close();
				
	}
	private void process(IndexWriter writer, File dataDirectory) throws Exception {
		
		ArrayList<String> tags= new ArrayList<String>();
		tags.add(DOCNO);
		tags.add(HEAD);
		tags.add(BYLINE);
		tags.add(DATELINE);
		tags.add(TEXT);

		File[] files = dataDirectory.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory() && !files[i].isHidden() && files[i].canRead() && files[i].exists()) {
				
				System.out.println("\nIndexing is going on with file : "+ files[i].getCanonicalPath());

				String fileContents = new String(Files.readAllBytes(Paths.get(files[i].getCanonicalPath())));
				String[] splitFileContent = fileContents.split("</DOC>");

				//since the last split will be space subtracting one from the doc count in a single .trectext file
				//String[] documents = new String[splitFileContent.length-1];
				
				String[] documents =Arrays.copyOfRange(splitFileContent, 0,splitFileContent.length);
				
				//for(int k=0;k<splitFileContent.length-1;k++) documents[k]  = splitFileContent[k];

				String content = null;

				for (String doc : documents) {

					Document document = new Document();
					for (int j = 0; j < tags.size(); j++) {
						String tagsContent = "";
						int startIndex = 0;
						int endIndex= 0;
						StringBuffer contentBuffer = new StringBuffer();
						
							while ((startIndex = doc.indexOf("<" + tags.get(j)+">", startIndex)) != -1) {
								startIndex += tags.get(j).length() + 2;  // plus to count for '<' and '>'
								endIndex = doc.indexOf("</" + tags.get(j)+ ">", startIndex);
								content = doc.substring(startIndex,endIndex);
								contentBuffer.append(content);
								startIndex += content.length();
							}
							
						tagsContent = contentBuffer.toString();
						if (j == 0)
							document.add(new StringField(DOCNO, tagsContent,Field.Store.YES));
						else
							document.add(new TextField(tags.get(j), tagsContent,Field.Store.YES));
					}
					writer.addDocument(document);
				}

				
			}

			
		}
			System.out.println("Indexing Successful");
			
		}

}
