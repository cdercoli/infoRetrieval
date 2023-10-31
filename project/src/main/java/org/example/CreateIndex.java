package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Text;

import static java.nio.file.Paths.*;
// import org.apache.lucene.store.RAMDirectory;

public class CreateIndex
{
    // Directory where the search index will be saved
    private static String INDEX_DIRECTORY = "src/main/resources/index";
    public static void main(String[] args) throws IOException
    {
        // Analyzer that is used to process TextField
        Analyzer analyzer = new StandardAnalyzer();
        //Analyzer analyzer = new EnglishAnalyzer();

        // storing the index on disk
        Directory directory = FSDirectory.open(get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        //creating the new index
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter iwriter = new IndexWriter(directory, config);

        BufferedReader objReader = null;
        try {
            objReader = new BufferedReader(new FileReader("src/main/resources/cran.all.1400"));
            String strCurrentLine = objReader.readLine();

            while (strCurrentLine != null) {
                String title = "";
                String text = "";
                String biblio = "";
                String author = "";
                Document doc = new Document();
                if(strCurrentLine.contains(".I")){
                    strCurrentLine = strCurrentLine.substring(strCurrentLine.indexOf("I") + 2);
                    doc.add(new TextField("id", strCurrentLine, Field.Store.YES));
                    strCurrentLine = objReader.readLine();

                }

                if(strCurrentLine.contains(".T")){
                    strCurrentLine = objReader.readLine();
                    while(strCurrentLine != null && !strCurrentLine.startsWith(".A")) {
                        title += " " + strCurrentLine;
                        strCurrentLine = objReader.readLine();
                    }
                    doc.add(new TextField("title", title, Field.Store.YES));
                }

                if(strCurrentLine.contains(".A")){
                    strCurrentLine = objReader.readLine();
                    while(strCurrentLine != null && !strCurrentLine.startsWith(".B")) {
                        author += " " + strCurrentLine;
                        strCurrentLine = objReader.readLine();
                    }
                    doc.add(new TextField("author", author, Field.Store.YES));
                }
                if(strCurrentLine.contains(".B")){
                    strCurrentLine = objReader.readLine();
                    while(strCurrentLine != null && !strCurrentLine.startsWith(".W")) {
                        biblio += " " + strCurrentLine;
                        strCurrentLine = objReader.readLine();
                    }
                    doc.add(new TextField("bibliography", biblio, Field.Store.YES));
                }

                if(strCurrentLine.contains(".W")){
                    strCurrentLine = objReader.readLine();
                    while(strCurrentLine != null && !strCurrentLine.startsWith(".I")) {
                        text += " " + strCurrentLine;
                        strCurrentLine = objReader.readLine();
                    }
                    doc.add(new TextField("text", text, Field.Store.YES));

                }

                // Save the document to the index
                iwriter.addDocument(doc);

                /*printing to see contents
                for (IndexableField field : doc.getFields()) {
                    System.out.println(field.name() + ": " + field.stringValue());
                }*/
            }
            // Commit changes and close everything
            iwriter.close();
            directory.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}