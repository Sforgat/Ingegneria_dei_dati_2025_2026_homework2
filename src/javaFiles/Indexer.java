package javaFiles;

import java.nio.file.Paths;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Indexer {
    
    private static final String DIR_INDEX = "homework2/index";
    private static final String DIR_FILE = "homework2/document";

    public static void main(String[] args) throws Exception {
        
        System.out.println("Inizio indicizzazione...");

        Long startTime = System.nanoTime(); // calcolo il tempo impiegato a cercare il documento

        Path indexPath = Paths.get(DIR_INDEX);

        //CONTROLLI PER IL PATH
        if (!Files.exists(indexPath)) {
            // Se non esiste, proviamo a crearlo
            try {
                Files.createDirectories(indexPath);
                System.out.println("Directory indice creata: " + indexPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Errore: Impossibile creare la directory dell'indice.");
                System.err.println("Path: " + indexPath.toAbsolutePath());
                e.printStackTrace();
                return;
            }
        }
        
        // Controllo se è una directory
        if (!Files.isDirectory(indexPath)) {
            System.err.println("Errore: Il path dell'indice non è una directory.");
            System.err.println("Path: " + indexPath.toAbsolutePath());
            return;
        }

        // Controllo se si può scrivere
        if (!Files.isWritable(indexPath)) {
            System.err.println("Errore: Non si hanno i permessi di scrittura per la directory dell'indice.");
            System.err.println("Path: " + indexPath.toAbsolutePath());
            return;
        }
        //System.out.println("Path indice OK: " + indexPath.toAbsolutePath());


        //CONTROLLO PATH DOCUMENTI 
        Path docPath = Paths.get(DIR_FILE);

        
        if (!Files.exists(docPath)) {
            System.err.println("Errore: La directory dei documenti non è stata trovata.");
            System.err.println("Path cercato: " + docPath.toAbsolutePath());
            System.err.println("Assicurati di eseguire il programma dalla directory corretta.");
            return; // Esce dal programma
        }
        
        
        if (!Files.isDirectory(docPath)) {
            System.err.println("Errore: Il path dei documenti non è una directory.");
            System.err.println("Path: " + docPath.toAbsolutePath());
            return;
        }

        // Controllo se su può leggere
        if (!Files.isReadable(docPath)) {
            System.err.println("Errore: Non si hanno i permessi di lettura per la directory dei documenti.");
            System.err.println("Path: " + docPath.toAbsolutePath());
            return;
        }
        //System.out.println("Path documenti OK: " + docPath.toAbsolutePath());

        
        // Creo la directory Lucene per gli accessi in lettura/scrittura
        Directory indexDir = FSDirectory.open(indexPath);

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
