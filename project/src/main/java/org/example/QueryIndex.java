// ./trec_eval ../project/src/main/resources/QRelsCorrectedforTRECeval ../project/src/main/resources/output.txt
package org.example;
import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Files;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

public class QueryIndex
{

    // the location of the search index
    private static String INDEX_DIRECTORY = "src/main/resources/index";

    // Limit the number of search results we get
    private static int MAX_RESULTS = 50;

    public static void main(String[] args) throws IOException, ParseException {
        // Analyzer used by the query parser.
        // Must be the same as the one used when creating the index
        Analyzer analyzer = new StandardAnalyzer();
        //Analyzer analyzer = new EnglishAnalyzer();

        // Open the folder that contains our search index
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        // create objects to read and search across the index
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

        //bm25
        //isearcher.setSimilarity(new BM25Similarity());
        isearcher.setSimilarity(new ClassicSimilarity());
        // Create the query parser. The default search field is "content", but
        // we can use this to search across any field
        QueryParser parser = new QueryParser("text", analyzer);

        BufferedReader objReader = null;
        String queryString;
        int queryId = 1;
        FileWriter file = new FileWriter("src/main/resources/output.txt");
        BufferedWriter output = new BufferedWriter(file);

        try {
            objReader = new BufferedReader(new FileReader("src/main/resources/cran.qry"));
            while ((queryString = objReader.readLine()) != null) {
                if(queryString.contains(".W")) {
                    queryString = objReader.readLine();
                    String text = "";
                    while (queryString != null && !queryString.startsWith(".I")) {
                        text += " " + queryString;
                        queryString = objReader.readLine();
                        //System.out.println(queryString);
                    }
                    queryString = text;

                    queryString = queryString.trim();

                    // if there is a querystring
                    if (queryString.length() > 0) {
                        // parse the query with the parser
                        queryString = queryString.replace("?", "");
                        Query query = parser.parse(queryString);

                        // Get the set of results
                        ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;

                        // Print the results
                        //System.out.println("Documents: " + hits.length)

                        for (int i = 0; i < hits.length; i++) {
                            Document hitDoc = isearcher.doc(hits[i].doc);
                            String string = queryId + " " + "Q0" + " " + hitDoc.get("id") + " " + i + " " + hits[i].score + " STANDARD" + "\n";
                            output.write(string);
                        }
                        // Closing the writer
                        queryId++;
                        //System.out.println();
                    }
                }
                //queryString = objReader.readLine();
            }

            // close everything and quit
            ireader.close();
            directory.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

