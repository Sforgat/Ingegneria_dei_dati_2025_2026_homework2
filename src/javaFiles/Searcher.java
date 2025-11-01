package javaFiles;



import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.it.ItalianAnalyzer;

public class Searcher {
    
    private static final String DIR_INDEX = "homework2/index";
    public static void main(String[] args) throws Exception {
        
        Path indexPath = Paths.get(DIR_INDEX);

        // Controllo 1: Esiste?
        if (!Files.exists(indexPath)) {
            System.err.println("Errore: La directory dell'indice non è stata trovata.");
            // Stampa il path assoluto che stava cercando, utilissimo per il debug!
            System.err.println("Path cercato: " + indexPath.toAbsolutePath());
            System.err.println("Assicurati di eseguire il programma dalla directory corretta o che la cartella '" + DIR_INDEX + "' esista.");
            return; // Esce dal programma
        }
        
        // Controllo 2: È una directory?
        if (!Files.isDirectory(indexPath)) {
            System.err.println("Errore: Il path specificato non è una directory.");
            System.err.println("Path: " + indexPath.toAbsolutePath());
            return;
        }

        // Controllo 3: È leggibile?
        if (!Files.isReadable(indexPath)) {
            System.err.println("Errore: Non si hanno i permessi di lettura per la directory dell'indice.");
            System.err.println("Path: " + indexPath.toAbsolutePath());
            return;
        }

        // Apertura indice in lettura
        Directory indexDir = FSDirectory.open(indexPath);
        IndexReader reader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = new IndexSearcher(reader);

        // definisco gli stessi analyzer che ho usato in Indexer
        Map<String,Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put("nome", new SimpleAnalyzer()); // fa lowercase e divide sui non alfabetici. Non vogliamo lo stemming
        analyzerMap.put("contenuto", new ItalianAnalyzer()); // fa lowercase, rimuove le stop-words e fa lo stemming
        Analyzer perFieldAnalyzer = new PerFieldAnalyzerWrapper(new ItalianAnalyzer(), analyzerMap);

        // Chiedo input da console
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- Motore di ricerca attivo ---");
        System.out.println("Sintassi: 'nome <termini>' o 'contenuto <termini>' ");
        System.out.println("Usa le virgolette per frasi esatte (es. contenuto \"codice pulito\")");
        System.out.println("Scrivi 'esci' per terminare");
        
        while (true) {
            System.out.println("\nQuery>");
            String line = scanner.nextLine();

            // controllo chiusura
            if("esci".equalsIgnoreCase(line)){
                break;
            }

            // Analisi query
            String campoDefault;
            String testoQuery;

            if(line.startsWith("nome ")){
                campoDefault = "nome"; 
                testoQuery = line.substring(5); // tolgo i primi 5 caratteri della stringa cioè nome
            }else if (line.startsWith("contenuto ")) {
                campoDefault = "contenuto"; 
                testoQuery = line.substring(10);
            }else {
                System.out.println("Errore: La query deve iniziare con 'nome' o con 'contenuto'");
                continue;
            }

            try{
                // creo il QueryParser, serve per usare l'analyzer corretto
                QueryParser parser = new QueryParser(campoDefault, perFieldAnalyzer);
                Query query = parser.parse(testoQuery); 

                Long startTime = System.nanoTime(); // calcolo il tempo impiegato a cercare il documento

                TopDocs hits = searcher.search(query, 10); // Prendo i primi 10 risultati
                StoredFields storedFields = searcher.storedFields();
                
                Long endTime = System.nanoTime();

                Long durationTime = (endTime - startTime);

                double elapseTimeSeconds = (double)durationTime / TimeUnit.SECONDS.toNanos(1);

                System.out.println(String.format("Trovati %d risultati per '%s', Tempo impiegato: %f s", hits.totalHits.value(), testoQuery, elapseTimeSeconds));
                
                for (ScoreDoc scoreDoc : hits.scoreDocs){

                    Document doc =  storedFields.document(scoreDoc.doc);
                    System.out.println(String.format(" - Punteggio: %4f | File: %s", scoreDoc.score, doc.get("nome")));

                }

            }catch (Exception e){
                System.out.println("Errore durante l'analisi della query: " + e.getMessage());
            }
        } 

        
        scanner.close();
        reader.close();
        indexDir.close();
        System.out.println("Ricerca terminata. ");
    }   
    
}
