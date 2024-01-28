import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class JackTokenizer{

    private static final ArrayList<String> keywords;
    static{
        keywords = new ArrayList<>();
        keywords.add("class");
        keywords.add("constructor");
        keywords.add("function");
        keywords.add("method");
        keywords.add("field");
        keywords.add("static");
        keywords.add("var");
        keywords.add("int");
        keywords.add("char");
        keywords.add("boolean");
        keywords.add("void");
        keywords.add("true");
        keywords.add("false");
        keywords.add("null");
        keywords.add("this");
        keywords.add("let");
        keywords.add("do");
        keywords.add("if");
        keywords.add("else");
        keywords.add("while");
        keywords.add("return");
    }
    private static final ArrayList<String> symbols;
    static{
        symbols = new ArrayList<>();
        symbols.add("{");
        symbols.add("}");
        symbols.add("(");
        symbols.add(")");
        symbols.add("[");
        symbols.add("]");
        symbols.add(".");
        symbols.add(",");
        symbols.add(";");
        symbols.add("+");
        symbols.add("-");
        symbols.add("*");
        symbols.add("/");
        symbols.add("&");
        symbols.add("|");
        symbols.add("<");
        symbols.add(">");
        symbols.add("=");
        symbols.add("~");
    }
    private static final ArrayList<String> Ops;
    static{
        Ops = new ArrayList<>();
        Ops.add("+");
        Ops.add("-");
        Ops.add("*");
        Ops.add("/");
        Ops.add("&");
        Ops.add("|");
        Ops.add("<");
        Ops.add(">");
        Ops.add("=");
        Ops.add("~");
    }

    private int pointer;
    private ArrayList<String> tokens;
    private String currentToken;
    private Scanner reader;
    private File file;
    private boolean midCommentblock;

public JackTokenizer(File source) throws FileNotFoundException{ //constructor
    currentToken = "class"; // currenttoken 
    file = source;
    reader = new Scanner(file); //scanner
    pointer = 0; // to get to the current token
    midCommentblock = false;
    tokens = new ArrayList<>(); // all the tokens seperated according the rules
    String fixedLine;
    reader = new Scanner(file);
    String line = reader.nextLine();
    try{
        do{
            if(OK(line)){ // a proper line 
                line = line.trim(); // delete unnecessary spcaes
                fixedLine = deleteEndSlashes(line); // if necessary delete "//" or "/*" and further
                tokens.addAll(seperate(fixedLine)); // add to tokens array the new line seperate by tokens
            }
            line = reader.nextLine();
        }
        while(reader.hasNextLine());
        //}//last line
        if(OK(line)){ // a proper line 
            line = line.trim(); // delete unnecessary spcaes
            fixedLine = deleteEndSlashes(line); // if necessary delete "//" or "/*" and further
            tokens.addAll(seperate(fixedLine)); // add to tokens array the new line seperate by tokens
        }
    }
    catch(Exception e){
        System.err.println(e);
    }      
}

public ArrayList<String> seperate(String line){
    ArrayList<String> retArr = new ArrayList<>();
    String[] QuotationArr = line.split("\""); // split the line by Quotation 
    for (int i = 0; i < QuotationArr.length; i++) {
        if(i % 2 == 1){ // only gets inside the cells with the quots  
            retArr.add(addQuotations(QuotationArr[i]));
            //retArr.add(QuotationArr[i]);
        }else{ // all the other parts of line that are not a quote
            //for (String word : QuotationArr[i].split(" ")) {
            //    retArr.addAll(seperateWords(word));
            //}
            String[] subarr = QuotationArr[i].split(" ");
            for (int j = 0; j < subarr.length; j++){ // to every cell - split by spces
                 retArr.addAll(seperateWords(subarr[j])); // got each word - checks if we need more seperation and does it 
            }
        }
    }
    return retArr;
}

public String addQuotations(String quote){
    return "\"" + quote + "\"";
}

public ArrayList<String> seperateWords(String word){
    ArrayList<String> arr = new ArrayList<>();
    if(word.length() == 1){
        arr.add(word);
    }
    else{
        String subword = "";
        for (int i = 0; i < word.length(); i++) {
            Character letter = word.charAt(i);
            if( (letter.equals('(')) || (letter.equals(')')) || (letter.equals('[')) || (letter.equals(']')) ||
            (letter.equals('{')) || (letter.equals('}')) || (letter.equals(',')) || (letter.equals('=')) || (letter.equals('='))
            || (letter.equals('.')) || (letter.equals('+')) || (letter.equals('-')) || (letter.equals('*')) || (letter.equals('&'))
            || (letter.equals('|')) || (letter.equals('~')) || (letter.equals('<')) || (letter.equals('>')) || (letter.equals(';'))){
            //if(symbols.contains(letter)){     // checks whether letter is s symbol - if symbol - seperate it  
                if(!subword.equals("")){         // if subword is not empty - if not empty it means that before the symbol there was a word that needs to be seperated from the symbol
                    arr.add(subword);         //  add the the return arr
                    subword = "";             // initiate
                }
                arr.add(letter.toString());   //adds the symbol to the return arr alone in the cell
            }else{
                subword = subword + letter;   // not symbol - letter is part of a word that started already
            }
        }
        if(!subword.equals("")){                 //last letter is part of a word 
            arr.add(subword);                // add to the return arr
        }
    }
    return arr;
}

public boolean OK(String line){ //checks if line is not empty or comment
    if(line.length() == 0){ return false;} //line is empty 
    if(line.startsWith("//")){ return false;} //line is a comment
    if(line.startsWith("/*")){
        midCommentblock = true;
        if(line.endsWith("*/")){ // line had /* and */. else -- line had /* but not the finish  */
            midCommentblock = false;
            return false;
        }
    }
    if(line.endsWith("*/")){ // end of /*  */ part
        midCommentblock = false;
    } 
    return true;
}

public String deleteEndSlashes(String line){
    if(line.contains("//")){// the line has commnets in the end of the line
        int startofslashe = line.indexOf("//");
        line = line.substring(0, startofslashe);
        return line;
    }if(line.contains("/*")){// the line has commnets in the end of the line
        int startofslashe = line.indexOf("/*");
        line = line.substring(0, startofslashe);
        return line;
    } 
    return line; // line doesnt have slashes in the end - no need to delete anything
}

public boolean hasMoreTokens(){
    return pointer < tokens.size()-1;
}

public void advance(){
    if(hasMoreTokens()){
        pointer++;
        currentToken = tokens.get(pointer);
    }else{
        throw new IllegalThreadStateException("no more tokens left ");
    }
}

public String tokenType(){
    if(!currentToken.isEmpty()){
        if(keywords.contains(currentToken)){return "keyword"; }
        else if(symbols.contains(currentToken)){return "symbol"; }
        else if(Character.isDigit(currentToken.charAt(0))){ return "integerConstant";}
        else if(currentToken.startsWith("\"")){ return "stringConstant";}
        else return "identifier";
    }else{
        return null;
    }
}
    
public String getCurToken(){
    return currentToken;
}

public String KeyWord(){ // returns the keyword which is the currentToken
    //if(tokenType() == "keyword"){
        return getCurToken();
    //}else{
        //throw new IllegalThreadStateException("KeyWord was expected"+ " the problem was token: " + getCurToken() + " token number :" + pointer);
    //}
}

public String symbol(){ // returns the symbol which is the currentToken
    //if(tokenType() == "symbol"){
        //return getCurToken().charAt(0);
        return getCurToken();
    //}else{
        //throw new IllegalThreadStateException("Symbol was expected"+ " the problem was token: " + getCurToken() + " token number :" + pointer);
    //}
}

public String identifier(){ // returns the indentifier which is the currentToken
    //if(tokenType() == "identifier"){
        return getCurToken();
    //}else{
        //throw new IllegalThreadStateException("Identifier was expected" + " the problem was token: " + getCurToken() + " token number :" + pointer);
    //}
}

public int intVal(){ // returns the integer value which is the currentToken
    //if(tokenType() == "integerConstant"){
        return Integer.parseInt(getCurToken());
    //}else{
      //  throw new IllegalThreadStateException("Integer was expected"+ " the problem was token: " + getCurToken() + " token number :" + pointer);
    //}
}

public String stringVal(){ // returns the string which is the currentToken
    //if(tokenType() == "stringConstant"){
        return getCurToken().substring(1, getCurToken().length()-1);
   // }else{
   //     throw new IllegalThreadStateException("String was expected"+ " the problem was token: " + getCurToken() + " token number :" + pointer);
   // }
} 

public String getTokenType(){
    return tokenType();
}

public void pointerBack(){
    if (pointer > 0) {
        pointer--;
    }
}

public void printTokensFromArrayList(ArrayList<String> Tokens) {
    for (String token : tokens) {
        System.out.print(token + " -> ");
    }
}

public void printTokens() {
    /*for (String token : tokens) {
        System.out.println(token);
    }*/
    for (int i = 0; i < tokens.size(); i++) {
        System.out.println(tokens.get(i));
    }
}

public boolean isOp(){
    /*if((currentToken.equals("<")) || currentToken == ">" || currentToken == "=" || currentToken == "|" || currentToken == "&" || currentToken == "/" ||
    currentToken == "*" ||  currentToken == "+" || currentToken == "-") return true;
    else return false;*/
    if(Ops.contains(currentToken)){ return true;}
    else { return false;}
}

public int getPointer(){
    return pointer;
}

public int getTokensSize(){
    return tokens.size();
}

    /*public static void main(String[] args) throws FileNotFoundException {
        String filepath = "C:\\Users\\Roi Safran\\Desktop\\Year2\\NAND2TETRIS\\nand2tetris\\nand2tetris\\projects\\10\\Proj10\\MainS.jack";
        //String filepath = args[0];
        File source = new File(filepath);
        String resultFileName = filepath.substring(0, filepath.length() - 5);
        resultFileName = resultFileName + "NEWT.xml";
        File result = new File(resultFileName);
        PrintWriter writer = new PrintWriter(result);
        JackTokenizer jk = new JackTokenizer(source);
        //jk.printTokens();
        writer.println("<tokens>");
        //writer.println("FIRST TOKEN");
        //writer.println("<"+jk.tokenType()+">" + " " + jk.getCurToken() + " " + "</"+jk.tokenType()+">");
        while(jk.hasMoreTokens()){
            writer.println("<"+jk.tokenType()+">" + " " + jk.getCurToken() + " " + "</"+jk.tokenType()+">");
            jk.advance();
        }
        //last line
        writer.println("<"+jk.tokenType()+">" + " " + jk.getCurToken() + " " + "</"+jk.tokenType()+">");
        writer.println("</tokens>");
        writer.close();
    }*/
}