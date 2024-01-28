import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
Removes comments, white space 
seperate it into Jack tokens
 */
public class JackTokenizer {

    //type
    public static enum TYPE {
        KEYWORD,
        SYMBOL,
        IDENTIFIER,
        INT_CONST,
        STRING_CONST,
        NONE;
    }
    //constant for keyword
    public static enum KEYWORD {
        CLASS,
        METHOD, FUNCTION, CONSTRUCTOR,
        INT, BOOLEAN, CHAR, VOID,
        VAR,
        STATIC, FIELD,
        LET, DO, IF, ELSE, WHILE, RETURN,
        TRUE, FALSE, NULL,
        THIS;
    }

    private String currentToken;
    private TYPE currentTokenType;
    private int pointer;
    private ArrayList<String> tokens;

    private static Pattern tokenPatterns;
    private static String keyWordReg;
    private static String symbolReg;
    private static String intReg;
    private static String strReg;
    private static String idReg;

    private static HashMap<String,KEYWORD> keyWordMap = new HashMap<String, KEYWORD>(); //keywords
    private static HashSet<Character> ops = new HashSet<Character>(); // ops

    static {
        //creating KEYWORD hashmap and OPS hashmap
        keyWordMap.put("class",KEYWORD.CLASS);keyWordMap.put("constructor",KEYWORD.CONSTRUCTOR);keyWordMap.put("function",KEYWORD.FUNCTION);
        keyWordMap.put("method",KEYWORD.METHOD);keyWordMap.put("field",KEYWORD.FIELD);keyWordMap.put("static",KEYWORD.STATIC);
        keyWordMap.put("var",KEYWORD.VAR);keyWordMap.put("int",KEYWORD.INT);keyWordMap.put("char",KEYWORD.CHAR);
        keyWordMap.put("boolean",KEYWORD.BOOLEAN);keyWordMap.put("void",KEYWORD.VOID);keyWordMap.put("true",KEYWORD.TRUE);
        keyWordMap.put("false",KEYWORD.FALSE);keyWordMap.put("null",KEYWORD.NULL);keyWordMap.put("this",KEYWORD.THIS);
        keyWordMap.put("let",KEYWORD.LET);keyWordMap.put("do",KEYWORD.DO);keyWordMap.put("if",KEYWORD.IF);
        keyWordMap.put("else",KEYWORD.ELSE);keyWordMap.put("while",KEYWORD.WHILE);keyWordMap.put("return",KEYWORD.RETURN);

        ops.add('+');ops.add('-');ops.add('*');ops.add('/');ops.add('&');ops.add('|');
        ops.add('<');ops.add('>');ops.add('=');
    }


    /**
    Opens the input file and tokenize it*/
    public JackTokenizer(File inFile) {
        try {
            Scanner reader = new Scanner(inFile);
            String preprocessed = "";
            String line = "";
            
            while(reader.hasNext()){
                line = removeComments(reader.nextLine()).trim();

                if (line.length() > 0) {
                    preprocessed += line + "\n"; //accumulate all proper lines
                }
            }
            preprocessed = removeBlockComments(preprocessed).trim(); // remove all the commentsblocks and trim all the white spaces

            //init all regex
            initRegs();

            Matcher m = tokenPatterns.matcher(preprocessed);
            tokens = new ArrayList<String>();  
            pointer = 0;

            while(m.find()){
                tokens.add(m.group()); // all the tokens seperated in array
            }

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        currentToken = "";
        currentTokenType = TYPE.NONE;
    }

    // init regex we need in tokenizer
    private void initRegs(){
        keyWordReg = "";

        for (String seg: keyWordMap.keySet()){
            keyWordReg += seg + "|";
        }

        symbolReg = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
        intReg = "[0-9]+";
        strReg = "\"[^\"\n]*\"";
        idReg = "[a-zA-Z_]\\w*";

        tokenPatterns = Pattern.compile(idReg + "|" + keyWordReg + symbolReg + "|" + intReg + "|" + strReg); // all what we need in one place seperated by "|" between each other
    }

    public boolean hasMoreTokens() {
        return pointer < tokens.size();
    }

    public void advance(){
        if (hasMoreTokens()) {
            currentToken = tokens.get(pointer);
            pointer++;
        }else {
            throw new IllegalStateException("No more tokens");
        }

        //System.out.println(currentToken);
        //updating currentToken's type 
        if (currentToken.matches(keyWordReg)){
            currentTokenType = TYPE.KEYWORD;
        }else if (currentToken.matches(symbolReg)){
            currentTokenType = TYPE.SYMBOL;
        }else if (currentToken.matches(intReg)){
            currentTokenType = TYPE.INT_CONST;
        }else if (currentToken.matches(strReg)){
            currentTokenType = TYPE.STRING_CONST;
        }else if (currentToken.matches(idReg)){
            currentTokenType = TYPE.IDENTIFIER;
        }else {
            throw new IllegalArgumentException("Unknown token: " + currentToken);
        }
    }

    public String getCurrentToken() {
        return currentToken;
    }

    public TYPE tokenType(){
        return currentTokenType;
    }

    
    //Returns the keyword which is the current token
    public KEYWORD keyWord(){
        if (currentTokenType == TYPE.KEYWORD){

            return keyWordMap.get(currentToken);

        }else {

            throw new IllegalStateException("Current token is not a keyword!");
        }
    }

    //Returns the symbol which is the current token
    public char symbol(){
        if (currentTokenType == TYPE.SYMBOL){

            return currentToken.charAt(0);

        }else{
            throw new IllegalStateException("Current token is not a symbol!");
        }
    }

    //Returns the identifier which is the current token
    public String identifier(){
        if (currentTokenType == TYPE.IDENTIFIER){

            return currentToken;

        }else {
            throw new IllegalStateException("Current token is not an identifier! current type:" + currentTokenType);
        }
    }

    //Returns the integer value which is the current token
    public int intVal(){
        if(currentTokenType == TYPE.INT_CONST){

            return Integer.parseInt(currentToken);
        }else {
            throw new IllegalStateException("Current token is not an integer constant!");
        }
    }

    //Returns the string which is the current token
    public String stringVal(){
        if (currentTokenType == TYPE.STRING_CONST){

            return currentToken.substring(1, currentToken.length() - 1);

        }else {
            throw new IllegalStateException("Current token is not a string constant!");
        }
    }

    //pointer back 
    public void pointerBack(){
        if (pointer > 0) {
            pointer--;
            currentToken = tokens.get(pointer);
        }
    }

    // if currentToken is Op
    public boolean isOp(){
        return ops.contains(symbol());
    }

    //delete comments( after "//") from line
    public static String removeComments(String line){

        int position = line.indexOf("//");

        if (position != -1){
            line = line.substring(0, position);
        }
        return line;
    }

    //delete block comment
    public static String removeBlockComments(String strIn){

        int startIndex = strIn.indexOf("/*");

        if (startIndex == -1) return strIn;

        String result = strIn;
        int endIndex = strIn.indexOf("*/");

        while(startIndex != -1){
            if (endIndex == -1){
                return strIn.substring(0,startIndex - 1);
            }

            result = result.substring(0,startIndex) + result.substring(endIndex + 2);

            startIndex = result.indexOf("/*");
            endIndex = result.indexOf("*/");
        }
        return result;
    }

    /**
     * Delete spaces from a String
     * @param strIn
     * @return
     */
    public static String noSpaces(String strIn){
        String result = "";

        if (strIn.length() != 0){

            String[] segs = strIn.split(" ");

            for (String s: segs){
                result += s;
            }
        }

        return result;
    }

}