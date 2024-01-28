import java.util.HashMap;

public class SymbolTable{
    
    
    private static HashMap<String, Integer> Symboltable;

    public SymbolTable(){
        SymbolTable.Symboltable = new HashMap<>();
        Symboltable.put("R0" , 0);
        Symboltable.put("R1" , 1);
        Symboltable.put("R2" , 2);
        Symboltable.put("R3" , 3);
        Symboltable.put("R4" , 4);
        Symboltable.put("R5" , 5);
        Symboltable.put("R6" , 6);
        Symboltable.put("R7" , 7);
        Symboltable.put("R8" , 8);
        Symboltable.put("R9" , 9);
        Symboltable.put("R10" , 10);
        Symboltable.put("R11" , 11);
        Symboltable.put("R12" , 12);
        Symboltable.put("R13" , 13);
        Symboltable.put("R14" , 14);
        Symboltable.put("R15" , 15);
        Symboltable.put("SCREEN" , 16384);
        Symboltable.put("KBD" , 24576);
        Symboltable.put("STOP" , 18);
        Symboltable.put("SP" , 0);
        Symboltable.put("LCL" , 1);
        Symboltable.put("ARG" , 2);
        Symboltable.put("THIS" , 3);
        Symboltable.put("THAT" , 4);
    }

    public void addEntry(String symbol, int address){
        Symboltable.put(symbol, address);

    } 
    public boolean contains(String symbol){
        return Symboltable.containsKey(symbol);
        }

    public int getAddress(String symbol){
        return Symboltable.get(symbol);
    }
    /*public int getNextfree(){
        return nextFree;
    }
    public void incrementnextfree(){
        nextFree++;
        if(programAddress > 16384){
            throw new RuntimeException();
        }
    }
    public void incrementProgramaddress(){
        programAddress++;
        if(programAddress > 32767){
            throw new RuntimeException(); 
        }
    }
    public int getProgramAddress(){
        return programAddress;
    }*/
    

/*public static void main(String[] args) {

    SymbolTable table = new SymbolTable();
    System.out.println(table.Symboltable);

    
 }*/

}
