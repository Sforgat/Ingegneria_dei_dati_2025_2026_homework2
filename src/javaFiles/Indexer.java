package javaFiles;

import java.nio.file.Paths;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.analysis.it.ItalianAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Indexer {
    
    private static final String DIR_INDEX = "/home/vboxuser/IngegneriaDeiDati/homework2/index";
    private static final String DIR_FILE = "/home/vboxuser/IngegneriaDeiDati/homework2/document";

    public static void main(String[] args) throws Exception {
        
        System.out.println("Inizio indicizzazione...");

        Long startTime = System.nanoTime(); // calcolo il tempo impiegato a cercare il documento

        // Creo la directory Lucene per gli accessi in lettura/scrittura
        Directory indexDir = FSDirectory.open(Paths.get(DIR_INDEX));

        // Imposto gli analyzer
        Map<String,Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put("nome", new SimpleAnalyzer()); // fa lowercase e divide sui non alfabetici. Non vogliamo lo stemming
        analyzerMap.put("contenuto", new ItalianAnalyzer()); // fa lowercase, rimuove le stop-words e fa lo stemming

        // Asseganzione analyzer ad ogni campo
        Analyzer perFieldAnalyzer = new PerFieldAnalyzerWrapper(new ItalianAnalyzer(), analyzerMap);

        // Configurazione dell'indexWriter
        IndexWriterConfig config = new IndexWriterConfig(perFieldAnalyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // ricrea l'indice ogni volta che eseguo il programma

        // per salvare direttamente le modifiche (commit) uso il try-with-resources, così che sblocca anche ad altri processi dopo il close
        try (IndexWriter writer = new IndexWriter(indexDir, config)) {

            // scansiono tutti i file .txt
            File dFile = new File(DIR_FILE);
            for (File file : dFile.listFiles()){
                System.out.println("Indicizzo: " + file.getName());
                indexFile(writer,file);
                //Long endTimeFile = System.nanoTime();
                //Long durationTimeFile = endTimeFile - startTime;
                //double elapseTimeSecondsFile = (double)durationTimeFile/ TimeUnit.SECONDS.toNanos(1);
                //System.out.println(String.format("Tempo impiegato: %s s", elapseTimeSecondsFile));
            }
        }

        System.out.println("Indicizzazione completata.");
        Long endTime = System.nanoTime();
        Long durationTime = (endTime - startTime);
        double elapseTimeSeconds = (double)durationTime / TimeUnit.SECONDS.toNanos(1);
        System.out.println(String.format("Tempo totale impiegato: %s s", elapseTimeSeconds));
    }

    private static void indexFile(IndexWriter writer, File file) throws IOException{

        // Leggo il contenuto
        String fileContent = Files.readString(file.toPath());
        String fileName = file.getName();

        //Creazione documento
        Document doc = new Document();

        // Aggiungo i fields, uso TextFields per renderli analizzabili e ricercabili, con Field.Store.YES si possono recuperare nei risultati di ricerca
        doc.add(new TextField("nome", fileName, Field.Store.YES));
        doc.add(new TextField("contenuto", fileContent, Field.Store.YES)); // per pochi file va bene

        writer.addDocument(doc);

    }
}
