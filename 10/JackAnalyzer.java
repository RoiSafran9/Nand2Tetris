import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;


public class JackAnalyzer{

    public static void main(String[] args) throws FileNotFoundException {
        
        File fileOut; 
        String resultFileName = ""; 
        File inFile = new File(args[0]); 
        
        ArrayList<File> inFiles = new ArrayList<>(); 

        //inFile is file
        if (inFile.isFile()) {
            // checking if inputFile ends with .jack
            if(args[0].endsWith(".jack")) {
                
                inFiles.add(inFile); // adding input file to the arrays list of file, 
            }
        }
        //directory
        else if(inFile.isDirectory()) { 
            inFiles = allFiles(inFile); 
            if (inFiles.size() == 0) {
                throw new IllegalArgumentException("Directory is empty");
            }
        }
    
        // for every file in arraylist of inputfiles
        for (File file : inFiles) {
            
            int endSubStringLoop = file.toString().length()-5;
            resultFileName = file.toString().substring(0, endSubStringLoop) + ".xml";
            
            fileOut = new File(resultFileName); 

            // compile the files in compilaitionEngin
            CompilationEngine compilationEngine = new CompilationEngine(file, fileOut);
            compilationEngine.compileClass();
        }
    }


    public static ArrayList<File> allFiles(File jackFileOrDir) {
        File[] files = jackFileOrDir.listFiles();
        ArrayList<File> fileList = new ArrayList<>();
        System.out.println(fileList.toString());
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".jack")){
                    fileList.add(file);
                }
            } 
        }
        return fileList;
    }
}