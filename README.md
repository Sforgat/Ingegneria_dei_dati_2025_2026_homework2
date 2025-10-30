# Homework 2

Questo progetto implementa un motore di ricerca per file `.txt` locali utilizzando la libreria Apache Lucene. È composto da due programmi principali:
* `Indexer.java`: Legge i file da una directory, li analizza e crea un indice.
* `Searcher.java`: Legge una query da console, interroga l'indice e restituisce i risultati con i tempi di ricerca.

## Analyzer Utilizzati

Per questo progetto sono stati utilizzati due analyzer diversi, assegnati ai campi specifici tramite un `PerFieldAnalyzerWrapper`:

#### Campo `nome`: `SimpleAnalyzer`
* Per il nome del file (es. `lista_spesa.txt`) non necessitano di un'analisi linguistica complessa come lo stemming (ridurre "relazioni" a "relazion") o la rimozione di stop-word. L'obiettivo qui è trovare un file in base a una parola chiave nel suo nome. Il `SimpleAnalyzer` converte tutto in minuscolo e divide i termini sui caratteri non alfabetici (come `_` o `.`). Questo permette di cercare `nome lista` e trovare il file. 

#### Campo `contenuto`: `ItalianAnalyzer`
* Questo analyzer è specifico per la lingua italiana e applica una catena di filtri:

    * Tokenizzazione: Utilizza lo `StandardTokenizer`, che gestisce correttamente punteggiatura, email, ecc.

    * Filtri linguistici: Applica filtri per gestire le elisioni (es. l'amico diventa amico).

    * Lowercase: Converte tutto in minuscolo.

    * Rimozione Stop-word: Rimuove le parole comuni italiane (articoli, preposizioni come il, di, a, che) che non portano valore semantico alla ricerca.

    * Stemming: Applica un algoritmo di stemming (il `ItalianLightStemmer`) che riduce le parole alla loro radice morfologica (es. correvano, correndo, corso diventano tutte corr).

Il contenuto dei file è testo in linguaggio naturale. Per ottenere una ricerca pertinente, non possiamo limitarci a cercare le parole esatte. Un utente che cerca "gatto che corre" si aspetta di trovare un documento che contiene "i gatti correvano". L'`ItalianAnalyzer` gestisce questa complessità, permettendo al motore di ricerca di associare le query alle loro varianti grammaticali presenti nei documenti, migliorando drasticamente la qualità dei risultati.

## Dati di Indicizzazione

* **File Indicizzati:** 5 file `.txt`.
* **Tempo di Indicizzazione medio:** 



## 3. Query di Test

Di seguito sono riportate le 10 query utilizzate per testare il sistema. 

| Query | Obbiettivo | File trovati | Tempo di Ricerca (ms) |
| :--- | :--- | :--- | :--- |
| `nome "Citazione.txt"` | Testare la ricerca case-insensitive sul nome (funziona grazie al SimpleAnalyzer) | 1 |  0.000759 s|
| `nome spesa`| Testare la ricerca di un termine parziale nel nome (es. per trovare lista_spesa.txt). | 1 | 0.003761 s |
| `contenuto architetture` | Ricerca semplice di un termine singolo nel contenuto. | 1 | 0.000612 s |
| `contenuto giorni`| Testare lo stemming (dovrebbe trovare documenti che contengono "giorno", "giorni"). | 3 | 0.000725 s |
| `contenuto il programma`| Testare la rimozione delle stop-word. L'ItalianAnalyzer rimuoverà "il", cercando di fatto solo "programma". | 2 | 0.035440 s|
| `contenuto "giorni prossimi"` | Testare una Phrase Query (ricerca esatta della sequenza). Ad esempio cercando `contenuto "prossimi giorni"` viene trovato 1 documento (bozza_email). | 0 | 0.001568 s|
| `nome "progetto appunti"` | Testare una Phrase Query sul nome, che il SimpleAnalyzer gestirà dividendo sui non alfabetici. Come nel caso precedente cercando `nome "appunti progetto"` trova il documento. | `n_file`  | `[tempo]` |
| `contenuto progetto AND lucene` | Testare una query booleana. Funziona in quanto  `bozza_email.txt` viene escluso perché, pur contenendo "progetto", non contiene "lucene". | 3 | 0.011300 s |
| `contenuto progetto NOT lucene` | Testare una query booleana. In questo caso prendiamo solo il file che contiene progetto e non lucene.  | 1 | 0.024146 s |
| `contenuto Gatto` | Testare un "miss" (nessun risultato), per verificare che il sistema non dia falsi positivi. | 0 | 0.000436 s |


---

## 4. Come Eseguire il Progetto

1.  Assicurarsi di avere i file `.jar` di Lucene 10.3.1 nella cartella `lib/` e che siano aggiunti al classpath.
2.  Modificare le variabili `DIR_FILE` (in `Indexer.java`) e `DIR_INDICE` (in `Indexer.java` e `Searcher.java`) con i path assoluti del proprio sistema.
3.  Eseguire `Indexer.java` per creare l'indice.
4.  Eseguire `Searcher.java` per iniziare a fare query.