import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class Parser {

    private Scanner reader1;
    private String inst = "";
    
    public Parser(String filePath)throws FileNotFoundException{ 
        reader1 = new Scanner(new File(filePath));
        inst = reader1.nextLine().replaceAll("//.*$", "").trim();
        if(inst.isEmpty()){ //blank line
            advance();
        }
    }

    public boolean hasMoreCommands(){
        return reader1.hasNextLine();
    }

    public void advance(){
        if(hasMoreCommands()){ // not empty yet
            inst = reader1.nextLine().replaceAll("//.*$", "").trim();
        
            if(inst.equals("") || inst.startsWith("//")){
                advance();
            }
        }
    }
    
    public instruction instructionType(){
        String st = "(";
        String end = ")";
        String At = "@";
        if(inst.startsWith(st) && inst.endsWith(end)){  //L_instruction
            return instruction.L_INSTRUCTION;
        }
        if(inst.startsWith(At)){ //A - instruction 
            return instruction.A_INSTRUCTION;
        }
        else{ // C - instruction
            return instruction.C_INSTRUCTION;
        }
    }

    public String symbol(){ //Returns the instruction’s symbol (string)
        if(instructionType() == instruction.L_INSTRUCTION) { //L -instruction
            return inst.substring(1, inst.length() -1); 
        }
        else{
            if(instructionType() == instruction.A_INSTRUCTION){ // A - instruction
                return inst.substring(1);
            }
            else{ //not contains symbols
                return "";
            }
        }
    }

    public String dest(){ //Returns the instruction’s dest field (string)
        if(instructionType() == instruction.C_INSTRUCTION){
            int index = inst.indexOf('=');
            if( index != -1){ //instruction contains '='
                 return inst.substring(0, index);
            }
        }
        return  "";
    }


    public String comp(){ //Returns the instruction’s comp field (string)
        if(instructionType() == instruction.C_INSTRUCTION){
            int index1 = inst.indexOf('=');
            int index2 = inst.indexOf(';');
            if(index1 != -1 && index2 != -1){ // instruction contains '=' and ';'
                return inst.substring(index1 + 1, index2);
                }else{
                    if(index1 == -1){ // no '='
                        return inst.substring(0, index2); //from beggining to ';'
                    }
                    else{ 
                        if(index2 == -1){//no ";" no jmp
                            return inst.substring(index1 + 1);
                        } 
                    }   
                }
            }
            return "";
        }
    
    public String jmp(){ // Returns the instruction’s jump field (string)
        if(instructionType() == instruction.C_INSTRUCTION){
            int index = inst.indexOf(';');
            if(index == -1){
                return "";
            }else{
                return inst.substring(index + 1);
            }
        }
        return "";
    }
 
    public enum instruction{ // all the different instruction types
        A_INSTRUCTION,
        C_INSTRUCTION,
        L_INSTRUCTION;
    }

    public void close(){
        reader1.close();
    }
}
        
    



        
        
        
