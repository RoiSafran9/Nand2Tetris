//import java.util.HashMap;

public class Code {
    /*private static HashMap<String, Integer> destTable;
    private static HashMap<String, Integer> compTable;
    private static HashMap<String, Integer> jmpTable;*/

    /*public Code(){//constructor
        this.destTable = new HashMap<>();
        this.initDest();
        compTable = new HashMap<>();
        this.initComp();
        jmpTable = new HashMap<>();
        this.initJmp();
    }*/

        public static String dest(String dest){
            if(dest.equals("")) return "000";
            if(dest.equals("M")) return "001";
            if(dest.equals("D")) return "010";
            if(dest.equals("DM") || dest.equals("MD")) return "011";
            if(dest.equals("A")) return "100";
            if(dest.equals("AM") || dest.equals("MA")) return "101";
            if(dest.equals("AD") || dest.equals("DA")) return "110";
            if(dest.equals("ADM") || dest.equals("MAD") || dest.equals("DMA")) return "111";
            return "";
        }
    
        public static String jmp(String jmp){
            if(jmp.equals("")) return "000";
            if(jmp.equals("JGT")) return "001";
            if(jmp.equals("JEQ")) return "010";
            if(jmp.equals("JGE")) return "011";
            if(jmp.equals("JLT")) return "100";
            if(jmp.equals("JNE")) return "101";
            if(jmp.equals("JLE")) return "110";
            if(jmp.equals("JMP")) return "111";
            return "";
        }
    
        public static String comp(String comp){
            
            if(comp.equals("0")) return "0101010"; // a = 0
            if(comp.equals("1")) return "0111111"; //  a = 0 
            if(comp.equals("-1")) return "0111010";// a = 0 
            if(comp.equals("D")) return "0001100"; // a = 0 
            if(comp.equals("A")) return "0110000"; // a = 0 
            if(comp.equals("!D")) return "0111010"; // a = 0
            if(comp.equals("!A")) return "0110001"; // a = 0
            if(comp.equals("-D")) return "0001111"; //  a = 0 
            if(comp.equals("-A")) return "0110011";// a = 0 
            if(comp.equals("D+1")) return "0011111"; // a = 0 
            if(comp.equals("A+1")) return "0110111"; // a = 0 
            if(comp.equals("D-1")) return "0001110"; // a = 0
            if(comp.equals("A-1")) return "0110010"; // a = 0
            if(comp.equals("D+A")) return "0000010"; // a = 0
            if(comp.equals("D-A")) return "0010011"; // a = 0 
            if(comp.equals("A-D")) return "0000111"; // a = 0
            if(comp.equals("D&A") || comp.equals("A&D")) return "0000000"; // a = 0
            if(comp.equals("D|A") || comp.equals("A|D")) return "0010101"; // a = 0
            if(comp.equals("M")) return "1110000"; // a = 1
            if(comp.equals("!M")) return "1110001"; // a = 1
            if(comp.equals("-M")) return "1110011";// a = 1
            if(comp.equals("M+1")) return "1110111"; // a = 1
            if(comp.equals("M-1")) return "1110010"; // a = 1
            if(comp.equals("D+M")) return "1000010"; // a = 1
            if(comp.equals("D-M")) return "1010011"; // a = 1
            if(comp.equals("M-D")) return "1000111";// a = 1
            if(comp.equals("D&M") || comp.equals("M&D")) return "1000000"; // a = 1
            if(comp.equals("D|M") || comp.equals("M|D")) return "1010101"; // a = 1 
            return "";
        }
        
    /*public static void main(String[] args) {
        Code code = new Code();
        System.out.println("D|M = " + code.comp("D|M")); //1010101
        System.out.println("D-A = " + code.comp("D-A")); //0010011
        System.out.println("JLE = " + code.jmp("JLE")); //111
        System.out.println("D = " + code.dest("D")); //0000010
        System.out.println("111" + Code.comp("D-A") + Code.dest("D") + Code.jmp("JLE")); // 111-0010011-010-111
    
    }*/
}
