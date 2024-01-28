import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

public class VMWriter {

    public static enum SEGMENT {CONST,ARG,LOCAL,STATIC,THIS,THAT,POINTER,TEMP,NONE};
    public static enum COMMAND {ADD,SUB,NEG,EQ,GT,LT,AND,OR,NOT};

    private static HashMap<SEGMENT,String> segmentStringHashMap = new HashMap<SEGMENT, String>(); //segments
    private static HashMap<COMMAND,String> commandStringHashMap = new HashMap<COMMAND, String>(); //commands
    private PrintWriter printWriter;

    static {
        //create the SEGMENT hashmap 
        segmentStringHashMap.put(SEGMENT.CONST,"constant");
        segmentStringHashMap.put(SEGMENT.ARG,"argument");
        segmentStringHashMap.put(SEGMENT.LOCAL,"local");
        segmentStringHashMap.put(SEGMENT.STATIC,"static");
        segmentStringHashMap.put(SEGMENT.THIS,"this");
        segmentStringHashMap.put(SEGMENT.THAT,"that");
        segmentStringHashMap.put(SEGMENT.POINTER,"pointer");
        segmentStringHashMap.put(SEGMENT.TEMP,"temp");
        //create the COMMAND hashmap
        commandStringHashMap.put(COMMAND.ADD,"add");
        commandStringHashMap.put(COMMAND.SUB,"sub");
        commandStringHashMap.put(COMMAND.NEG,"neg");
        commandStringHashMap.put(COMMAND.EQ,"eq");
        commandStringHashMap.put(COMMAND.GT,"gt");
        commandStringHashMap.put(COMMAND.LT,"lt");
        commandStringHashMap.put(COMMAND.AND,"and");
        commandStringHashMap.put(COMMAND.OR,"or");
        commandStringHashMap.put(COMMAND.NOT,"not");
    }

     //creates a new file and prepares it for writing
    public VMWriter(File fOut) {
        try {
            printWriter = new PrintWriter(fOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * writes a VM push command
     * @param segment
     * @param index
     */
    public void writePush(SEGMENT segment, int index){
        writeCommand("push",segmentStringHashMap.get(segment), String.valueOf(index)); // push-SEGMENT-INDEX
    }

    /**
     * writes a VM pop command
     * @param segment
     * @param index
     */
    public void writePop(SEGMENT segment, int index){
        writeCommand("pop", segmentStringHashMap.get(segment), String.valueOf(index)); // pop-SEGMENT-INDEX
    }

    /**
     * writes a VM arithmetic command
     * @param command
     */
    public void writeArithmetic(COMMAND command){
        writeCommand(commandStringHashMap.get(command),"",""); //command
    }

    /**
     * writes a VM label command
     * @param label
     */
    public void writeLabel(String label){
        writeCommand("label",label,""); //label-LABEL
    }

    /**
     * writes a VM goto command
     * @param label
     */
    public void writeGoto(String label){
        writeCommand("goto",label,""); //goto -- LABEL
    }
    /**
     * writes a VM if-goto command
     * @param label
     */
    public void writeIf(String label){
        writeCommand("if-goto",label,""); //if-goto -- LABEL
    }

    /**
     * writes a VM call command
     * @param name
     * @param nArgs
     */
    public void writeCall(String name, int nArgs){
        writeCommand("call", name, String.valueOf(nArgs)); //call-NAME-nARGS
    }

    /**
     * writes a VM function command
     * @param name
     * @param nLocals
     */
    public void writeFunction(String name, int nLocals){
        writeCommand("function", name, String.valueOf(nLocals)); //function-NAME-nLocals
    }

    /**
     * writes a VM return command
     */
    public void writeReturn(){
        writeCommand("return","","");
    }

    //MAIN METHOD
    //the writer - gets 3 arguments from every methods - some args will be empty 
    // cmd - arg1 - arg2
    public void writeCommand(String cmd, String arg1, String arg2){
        printWriter.print(cmd + " " + arg1 + " " + arg2 + "\n");

    }
   
    //close 
    public void close(){
        printWriter.close();
    }

}