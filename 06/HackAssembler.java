import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class HackAssembler {

    private static SymbolTable symbolTable = new SymbolTable();
    private static PrintWriter writer;

    public static void main(String[] args) throws FileNotFoundException, IOException{
        String source = args[0]; //input 
        StringBuilder resultFile = new StringBuilder(source.substring(0, source.length() - 3));
        resultFile.append("hack"); // new file with the .hack ending instead of asm 
        writer = new PrintWriter(new FileWriter(resultFile.toString())); // this will write oin the result file
        firstPass(source); // run firstpass on the file 
    }


        public static void firstPass(String file) throws FileNotFoundException, IOException{
            int cnt = 0 ; // where to put the symbol in the table
            Parser parser = new Parser(file);
            while(parser.hasMoreCommands()){ // if theres more instructions to read 
                if(parser.instructionType() == Parser.instruction.L_INSTRUCTION){
                    symbolTable.addEntry(parser.symbol(), cnt); // add the symbol to the table 
                } else{ // not a symbol
                    cnt++;
                }
                parser.advance();
            }
            parser.close();
            secondPass(file); // run the second pass on the file and start translate all to binary 
        } 

        public static void secondPass(String file) throws FileNotFoundException{
            Parser parser = new Parser(file);
            int cnt = 16 ; // available free place in table to put the next thing 
            String newInst = ""; // this will be the oupput of every translation
          
            while(parser.hasMoreCommands()){// if theres more instructions to read
                if(parser.instructionType() == Parser.instruction.A_INSTRUCTION){ 
                    String symbol = parser.symbol(); 
                    if(!symbolTable.contains(symbol)){ // does the symbol already in the table?
                        symbolTable.addEntry(parser.symbol(), cnt); // add to the symboltable the new symbol 
                    
                        if(Character.isDigit(parser.symbol().charAt(0))){ // digits that needs to be translated to binary 
                            newInst = AtoBinary(symbol);
                        }else{ //not a digit 
                            String symaddress = Integer.toString(cnt);
                            newInst = AtoBinary(symaddress);  
                            cnt++;
                        }
                    }
                    else{ //symbol is in the table - already known symbol - lets find the address of the symbol 
                        if(Character.isDigit(parser.symbol().charAt(0))){ // a digit
                            newInst = AtoBinary(symbol); 
                        }
                        else{ //not a digit 
                            String symaddress = Integer.toString(symbolTable.getAddress(symbol));
                            newInst = AtoBinary(symaddress); //translate to binary the address of the already known symbol 
                        }
                    }
                writer.println("0" + newInst);
                }else{ // C - instruction 
                    if(parser.instructionType() == Parser.instruction.C_INSTRUCTION){
                        String comp = parser.comp();
				        String dest = parser.dest();
				        String jmp = parser.jmp();
                        writer.println("111" + Code.comp(comp) + Code.dest(dest) + Code.jmp(jmp));
                        
                    }
                }
                parser.advance();
            }

        if(parser.instructionType() == Parser.instruction.A_INSTRUCTION){ // checking for last instruction of file
            String symbol = parser.symbol();
            if(!symbolTable.contains(parser.symbol())){ // does the symbol already in the table?
                symbolTable.addEntry(parser.symbol(), cnt); // add to the symboltable the new symbol 
            
                if(Character.isDigit(parser.symbol().charAt(0))){ // digits that needs to be translated to binary 
                    newInst = AtoBinary(symbol);
                }else{ //not a digit 
                    String symaddress = Integer.toString(cnt);
                    newInst = AtoBinary(symaddress);
                }
                cnt++;
            }else{
                    if(Character.isDigit(parser.symbol().charAt(0))){ // a digit
                        newInst = AtoBinary(symbol); 
                    }
                    else{ //not a digit 
                        String symaddress = Integer.toString(symbolTable.getAddress(symbol));
                        newInst = AtoBinary(symaddress); //translate to binary the address of the already known symbol 
                    }                    
                }  
                writer.println("0" + newInst);
            }
            else{ // C - instruction 
                if(parser.instructionType() == Parser.instruction.C_INSTRUCTION){
                    String comp = parser.comp();
                    String dest = parser.dest();
                    String jmp = parser.jmp();
                    writer.println("111" + Code.comp(comp) + Code.dest(dest) + Code.jmp(jmp));
                    
                }

            }
            writer.close();
            parser.close();
        }

 

        public static String AtoBinary(String address){ // translate A instruction to binary 
            int num = Integer.parseInt(address);
            String binary = Integer.toBinaryString(num);
            String binary2 = String.format("%15s", binary).replace(' ', '0');
            return binary2;
        }
            
        /*public static String pad15(String binarysString){ 
            return String.format("%1%15s", binarysString).replace(' ', '0');
        }*/
        
}