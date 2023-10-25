/*
* 
Scrivere un programma che dato in input una lista di directories, comprima tutti i file in esse contenuti, con l'utility gzip 

Ipotesi semplificativa:  zippare solo i file contenuti nelle directories passate in input
Non considerare ricorsione su eventuali sottodirectories 
Il riferimento ad ogni file individuato viene passato ad un task, che deve essere eseguito in un threadpool 
Individuare nelle API JAVA la classe di supporto adatta per la compressione 

NOTA: l'utilizzo dei threadpool è indicato, perchè I task presentano un buon mix tra I/O e computazione 
           I/O heavy: tutti i file devono essere letti e scritti
          CPU-intensive: la compressione richiede molta computazione 

facoltativo: comprimere ricorsivamente i file in tutte le sottodirectories
* 
*/


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class App {
    //task del thread pool
    public static class Compressore implements Runnable{

        /*
         * Prende un file name specifico e comprime quel file e lo posizione nella cartella corrente
         */
        private String absolutePath;
        private String parent;
        private String name;

        public Compressore(String absolutePath,String parent, String name){
            this.absolutePath = absolutePath;
            this.parent = parent;
            this.name = name;

            // Trova l'indice dell'ultimo punto nel nome del file
            int lastIndex = this.name.lastIndexOf(".");
            if (lastIndex != -1) {
                // Estrai la parte del nome del file prima del punto
                String nomeFileSenzaEstensione = this.name.substring(0, lastIndex);
                // Crea il nuovo nome del file con la nuova estensione
                this.name = nomeFileSenzaEstensione + "." + "gz";
            }
           
            //System.out.println(this.absolutePath);
        }
        public void run(){
            System.out.println("Compressione di "+absolutePath);
            //comprimo poi il file con filename specificato

            try {
                //modifica path assoluto per il nuovo file compresso
                String newFilePath = this.absolutePath;
                int lastIndex = this.absolutePath.lastIndexOf(".");
                 if (lastIndex != -1) {
                    newFilePath = this.absolutePath.substring(0, lastIndex) + "." + "gz";
                    //System.out.println("Nuovo percorso con la nuova estensione: " + newFilePath);
                } 

                //inizio compressione file singolo
                FileInputStream fileInputStream = new FileInputStream(absolutePath);
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(newFilePath));
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fileInputStream.read(buffer)) > 0) {
                    gzipOutputStream.write(buffer, 0, len);
                }
            
                fileInputStream.close();
                gzipOutputStream.finish();
                gzipOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void stampaArr(ArrayList<String> arr){
        //stampo array di stringhe
        System.out.print("[ ");
        for(String s : arr){
            System.out.print(s+" : ");
        }
        System.out.println("EOF ]\n\n");
    }

    public static void visitaRic(String dirname,ExecutorService threadPool){
        //stampa dirname
        System.out.println("Lista file nella directory "+dirname+":");

        //visito a partire dalla cartella corrente tutti i file e mando al thread pool il file

        //ora capisco che file ci sono dentro ogni cartella inserita in input e stampo i loro nomi
        File directory = new File(dirname);
        String[] nameList = directory.list();
        try {
            for(int i=0; i< nameList.length; i++)
            {
                File f = new File(dirname, nameList[i]);
                //statFile(f);
                if(f.isFile()==true){
                    //System.out.print("getName: "+f.getName()+" , getAbPath: "+f.getAbsolutePath()+" , getCanPath: "+f.getCanonicalPath()+"\n\n"+"parent: "+f.getParent()+"\n\n\n");
                    Compressore c = new Compressore(f.getAbsolutePath(),f.getParent(),f.getName());
                    threadPool.submit(c);
                }
                if(f.isDirectory()==true){
                    visitaRic(f.getAbsolutePath(), threadPool);
                }
                //System.out.println("-> "+nameList[i]);
            }
              
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("La dirname "+dirname+" non ha file al suo interno");
        }
    
    }
    private static void statFile(File f) { 
        System.out.println("PROPRIETA' FILE: "+f.getName());
        System.out.println("Path Assoluta: "+f.getAbsolutePath());
        System.out.println("Spazio Occupato: "+f.length()+" bytes");
        if(f.isDirectory()==true){
        System.out.println("è una Directory\n");
        }
       
        if(f.isFile()==true){
        System.out.println("è un File\n");
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        ExecutorService threadPool = Executors.newFixedThreadPool(8); 
        String dir;
        ArrayList<String> dir_arr = new ArrayList<String>();
        System.out.println("Inizio inserimento nomi directory nella lista");
       
        //carico l'Arraylist
        do{
            dir = sc.nextLine();
            
            if(dir.equals("-")==false){
                dir_arr.add(dir);
                //System.out.println("dir inserita: "+dir);
            }else{
                System.out.println("Fine inserimento");
            }

        }
        while(dir.equals("-")==false);

        //System.out.println("Stampa finale dell'array caricato");
        stampaArr(dir_arr);
        
     
        //visita di ogni cartella inserita in input
        while(dir_arr.size()>0){
            String drun = dir_arr.getFirst();
            System.out.println("--> valore prelevato "+drun);
            visitaRic(drun,threadPool);
            dir_arr.removeFirst();
        }


        sc.close();
        threadPool.close();
    }
}

