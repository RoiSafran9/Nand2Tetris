import java.io.File;
import java.io.IOException;

public class JackCompiler {

    public static void main(String[] args) throws IOException {
        String filepath = args[0]; //input
        File source = new File(filepath);
        if (source.isDirectory()){
            File[] fileList = fileFilter(source);
            for (File file : fileList) {
                run(file);
                //System.out.println(file.getName() + " Has compiled");
            }
        } else {
            if (source.getName().endsWith(".jack")){
                run(source);
                //System.out.println(source.getName() + " Has compiled");
            } else {
                System.out.println("unable to compile such file");
            }
        }
    }

    private static void run(File file) throws IOException {
        String sourceName = file.getAbsolutePath();
        String resultFileName = sourceName.substring(0, (sourceName.length()-4));
        resultFileName = resultFileName + "vm";
        File result = new File(resultFileName);
        CompilationEngine compiler = new CompilationEngine(file, result);
        compiler.compileClass();
        
    }

    public static File[] fileFilter(File directory){
        File[] fileList = directory.listFiles();
        int size = 0;
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].getName().endsWith(".jack")) size++;
        }
        File[] filteredList = new File[size];
        int counter = 0;
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].getName().endsWith(".jack")){
                filteredList[counter] = fileList[i];
                counter++;
            } 
        }
        return filteredList;
    }
}