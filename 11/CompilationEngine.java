import java.io.File;

/**
 * This class does the compilation itself.
 * It reads its input from a JackTokenizer and writes its output into a VMWriter.
 * It is organized as a series of compilexxx ( ) routines, where xxx is a syntactic element of the Jack language.
 * The contract between these routines is that each compilexxx ( ) routine should read the syntactic construct xxx from the input,
 * advance ( ) the tokenizer exactly beyond xxx, and emit to the output VM code effecting the semantics of xxx.
 * Thus compilexxx ( ) may only be called if indeed xxx is the next syntactic element of the input.
 * If xxx is a part of an expression and thus has a value, the emitted code should compute this value and leave it at the top of the VM stack
 */
public class CompilationEngine {

    private VMWriter vmWriter;
    private JackTokenizer tokenizer;
    private SymbolTable symbolTable;
    private String currentClass;
    private String currentSubroutine;

    private int labelIndex; //LABEL counter

    /**
     * Creates a new compilation engine with the given input and output.
     * The next routine called must be compileClass()
     * @param inFile
     * @param outFile
     */
    public CompilationEngine(File inFile, File outFile) {

        tokenizer = new JackTokenizer(inFile); //jk
        vmWriter = new VMWriter(outFile); //vmW
        symbolTable = new SymbolTable(); //ST
        labelIndex = 0;
    }

    /**
     return current function name, className.subroutineName
     */
    private String currentFunction(){
        if(currentClass.length() != 0 && currentSubroutine.length() !=0){
            return currentClass + "." + currentSubroutine;
        }

        return ""; //empty 
    }

    private String compileType(){ // in / char / boolean / className

        tokenizer.advance();

        if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && (tokenizer.keyWord() == JackTokenizer.KEYWORD.INT || tokenizer.keyWord() == JackTokenizer.KEYWORD.CHAR || tokenizer.keyWord() == JackTokenizer.KEYWORD.BOOLEAN)){
            return tokenizer.getCurrentToken();
        }

        if (tokenizer.tokenType() == JackTokenizer.TYPE.IDENTIFIER){
            return tokenizer.identifier();
        }
        error("in|char|boolean|className");
        return "";
    }


    //Complies a complete class
    //class: 
    //'class' className '{' classVarDec* subroutineDec* '}'
    public void compileClass(){
        //'class'
        tokenizer.advance();

        if(tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD || tokenizer.keyWord() != JackTokenizer.KEYWORD.CLASS){
            System.out.println(tokenizer.getCurrentToken());
            error("class");
        }

        //className
        tokenizer.advance();

        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            error("className");
        }

        //classname does not need to be put in symbol table
        currentClass = tokenizer.identifier(); //currentClass = className

        //'{'
        requireSymbol('{');

        //classVarDec* subroutineDec*
        compileClassVarDec();
        compileSubroutine();

        //'}'
        requireSymbol('}');
        
        //end of file 
        if(tokenizer.hasMoreTokens()){
            throw new IllegalStateException("Theres more tokens. unexpected ones");
        }

        //close
        vmWriter.close();
    }

    /**
     * Compiles a static declaration or a field declaration
     * classVarDec:
     * // ('static'|'field') type varName (','varNAme)* ';'
     */
    private void compileClassVarDec(){
        tokenizer.advance();

        //next is a '}'
        //if TRUE - go BACK - theres no classVarDec
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '}'){ 
            tokenizer.pointerBack();
            return;
        }

        //start subroutineDec or classVarDec
        //keyword
        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD){
            error("Keywords");
        }

        //subroutineDec and not classVarDec - go BACK
        if (tokenizer.keyWord() == JackTokenizer.KEYWORD.CONSTRUCTOR || tokenizer.keyWord() == JackTokenizer.KEYWORD.FUNCTION || tokenizer.keyWord() == JackTokenizer.KEYWORD.METHOD){
            tokenizer.pointerBack();
            return;
        }

        //classVarDec exists - expect - ('static'|'field')
        if (tokenizer.keyWord() != JackTokenizer.KEYWORD.STATIC && tokenizer.keyWord() != JackTokenizer.KEYWORD.FIELD){
            error("static or field");
        }

        Symbol.KIND kind = null;
        String type = "";
        String name = "";

        switch(tokenizer.keyWord()){
            case STATIC: kind = Symbol.KIND.STATIC; break; // keyWord = STATIC
            case FIELD: kind = Symbol.KIND.FIELD; break; // keyWord = FIELD
        }

        //type
        type = compileType();

        //at least one varName
        boolean varNamesDone = false;

        do {
            //varName - IDENTIFIER
            tokenizer.advance();
            if(tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
                error("identifier");
            }

            name = tokenizer.identifier(); //varName

            symbolTable.define(name,type,kind); // add to classSymbols table 

            // , or ;
            tokenizer.advance();

            if(tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')){
                error("',' or ';'");
            }

            if(tokenizer.symbol() == ';'){ // no more vars
                break; //end of classVarDec
            }


        }while(true); // more vars ahead

        compileClassVarDec();
    }

    /*subroutineDec:
     * // ('constructor'|'function'|'method') ('void'|type) subroutineName '(' parameterList ')' subroutineBody
    */
    private void compileSubroutine(){
        tokenizer.advance();

        //next is a '}'
        //if TRUE - go BACK - theres no subroutineDec
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        //start of a subroutine
        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD || (tokenizer.keyWord() != JackTokenizer.KEYWORD.CONSTRUCTOR && tokenizer.keyWord() != JackTokenizer.KEYWORD.FUNCTION && tokenizer.keyWord() != JackTokenizer.KEYWORD.METHOD)){
            error("constructor | function | method");
        }

        JackTokenizer.KEYWORD keyword = tokenizer.keyWord();

        symbolTable.startSubroutine(); //initiate Subroutine table 

        //if method - this is the first argument
        if(tokenizer.keyWord() == JackTokenizer.KEYWORD.METHOD){
            symbolTable.define("this",currentClass, Symbol.KIND.ARG); // adds to table
        }

        String type = "";

        //void or type(int/char/boolean)
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.VOID){
            type = "void";
        }else {
            tokenizer.pointerBack();
            type = compileType();
        }

        //subroutineName - identifier
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            error("subroutineName");
        }

        currentSubroutine = tokenizer.identifier();//subroutineName

        //'('
        requireSymbol('(');

        //parameterList
        compileParameterList();

        //')'
        requireSymbol(')');

        //subroutineBody
        compileSubroutineBody(keyword); //??????????????????????????????????

        compileSubroutine();
    }

    /**
     * Compiles the body of a subroutine
     * subroutineBody:
     * '{' varDec* statements '}'
     */
    private void compileSubroutineBody(JackTokenizer.KEYWORD keyword){
        //'{'
        requireSymbol('{');
        //varDec*
        compileVarDec();
        //write VM function declaration
        wrtieFunctionDec(keyword); 
        //statements
        compileStatement();
        //'}'
        requireSymbol('}');
    }

    /**
     * write function declaration, load pointer when keyword is METHOD or CONSTRUCTOR
     */
    private void wrtieFunctionDec(JackTokenizer.KEYWORD keyword){

        vmWriter.writeFunction(currentFunction() ,symbolTable.varCount(Symbol.KIND.VAR));

        //METHOD and CONSTRUCTOR need to load this pointer
        if (keyword == JackTokenizer.KEYWORD.METHOD){
            //A Jack method with k arguments is compiled into a VM function that operates on k + 1 arguments.
            // The first argument (argument number 0) always refers to the this object.
            vmWriter.writePush(VMWriter.SEGMENT.ARG, 0);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,0);

        }else if (keyword == JackTokenizer.KEYWORD.CONSTRUCTOR){
            //A Jack function or constructor with k arguments is compiled into a VM function that operates on k arguments.
            vmWriter.writePush(VMWriter.SEGMENT.CONST, symbolTable.varCount(Symbol.KIND.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,0);
        }
    }

    /** compile a statement */
    private void compileStatement(){
        tokenizer.advance();

        //if '}' - no statement - go BACK 
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        // let | if | while | do | return
        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD){
            error("keyword");
        }else{
            switch (tokenizer.keyWord()){
                case LET:compileLet();break;
                case IF:compileIf();break;
                case WHILE:compilesWhile();break;
                case DO:compileDo();break;
                case RETURN:compileReturn();break;
                default:error("'let'|'if'|'while'|'do'|'return'");
            }
        }
        compileStatement();
    }

    /**
     * Compiles a parameter list - can be empty
     * ParameterList:
     * ((type varName)(',' type varName)*)?
     */
    private void compileParameterList(){
        tokenizer.advance();
        //if '}' - no paramaters - go BACK
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ')'){
            tokenizer.pointerBack();
            return;
        }
        //theres parameter/s
        String type = "";

        //at least one varName
        tokenizer.pointerBack(); // compileType starts with advance()
        do {
            //type
            type = compileType();

            //varName - identifier
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
                error("identifier");
            }

            symbolTable.define(tokenizer.identifier(), type, Symbol.KIND.ARG); //add to table as ARG

            // , or )
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ')')){
                error("',' or ')'");
            }

            if (tokenizer.symbol() == ')'){ // no more parameters
                tokenizer.pointerBack();
                break; // end
            }

        }while(true);// more parameters ahead
    }

    /**
     * Compiles a var declaration
     * VarDec:
     * 'var' type varName (',' varName)* ';'
     */
    private void compileVarDec(){
        tokenizer.advance();

        //if TRUE - no 'var' - go BACK
        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD || tokenizer.keyWord() != JackTokenizer.KEYWORD.VAR){
            tokenizer.pointerBack();
            return;
        }

        //type
        String type = compileType();

        //one varName or more
        boolean varNamesDone = false;

        do {
            //varName - identifier
            tokenizer.advance();

            if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
                error("identifier");
            }

            symbolTable.define(tokenizer.identifier(),type, Symbol.KIND.VAR); // add to table as var

            // , or ;
            tokenizer.advance();

            if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')){
                error("',' or ';'");
            }
            //end of line
            if (tokenizer.symbol() == ';'){
                break;
            }


        }while(true);//theres more vars ahead

        compileVarDec();
    }

    /**
     * Compiles a do statement
     * 'do' subroutineCall ';'
     */
    private void compileDo(){
        //subroutineCall
        compileSubroutineCall();
        //';'
        requireSymbol(';');
        //pop return value
        vmWriter.writePop(VMWriter.SEGMENT.TEMP,0);
    }

    /**
     * Compiles a let statement
     * 'let' varName ('[' expression ']')? '=' expression ';'
     */
    private void compileLet(){
        //varName
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            error("varName");
        }

        String varName = tokenizer.identifier();

        //'[' or '='
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != '[' && tokenizer.symbol() != '=')){
            error("'['|'='");
        }

        boolean expExist = false;

        //'[' expression ']' ,need to deal with array [base+offset]
        if (tokenizer.symbol() == '['){ // array 
            expExist = true;

            //push array variable,base address into stack
            vmWriter.writePush(getSeg(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));

            //calc offset
            compileExpression();

            //']'
            requireSymbol(']');

            //base+offset
            vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);
        }

        if (expExist) tokenizer.advance(); // if FALSE - '='

        //expression
        compileExpression();

        //';'
        requireSymbol(';');

        if (expExist){
            //*(base+offset) = expression
            //pop expression value to temp
            vmWriter.writePop(VMWriter.SEGMENT.TEMP,0);
            //pop base+index into 'that'
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,1);
            //pop expression value into *(base+index)
            vmWriter.writePush(VMWriter.SEGMENT.TEMP,0);
            vmWriter.writePop(VMWriter.SEGMENT.THAT,0);
        }else {
            //pop expression value directly
            vmWriter.writePop(getSeg(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));
        }
    }

    /**
     * return corresponding seg for input kind
     * @param kind
     * @return
     */
    private VMWriter.SEGMENT getSeg(Symbol.KIND kind){
        switch (kind){
            case FIELD:return VMWriter.SEGMENT.THIS;
            case STATIC:return VMWriter.SEGMENT.STATIC;
            case VAR:return VMWriter.SEGMENT.LOCAL;
            case ARG:return VMWriter.SEGMENT.ARG;
            default:return VMWriter.SEGMENT.NONE;
        }
    }

    /**
     * Compiles a while statement
     * 'while' '(' expression ')' '{' statements '}'
     */
    private void compilesWhile(){

        String continueLabel = newLabel();
        String topLabel = newLabel();

        //top label for while loop
        vmWriter.writeLabel(topLabel);

        //'('
        requireSymbol('(');
        //expression while condition: true or false
        compileExpression();
        //')'
        requireSymbol(')');
        //if ~(condition) go to continue label
        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        vmWriter.writeIf(continueLabel);
        //'{'
        requireSymbol('{');
        //statements
        compileStatement();
        //'}'
        requireSymbol('}');
        //if (condition) go to top label
        vmWriter.writeGoto(topLabel);
        //or continue
        vmWriter.writeLabel(continueLabel);
    }

    private String newLabel(){
        return "LABEL_" + (labelIndex++);
    }

    /**
     * Compiles a return statement
     * ‘return’ expression? ';'
     */
    private void compileReturn(){
        tokenizer.advance();
        //if '}' - no return 
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ';'){
            //no expression - push 0 
            vmWriter.writePush(VMWriter.SEGMENT.CONST,0);
        }else{ //theres a return 
            //expression exist
            tokenizer.pointerBack();
            //expression
            compileExpression();
            //';'
            requireSymbol(';');
        }
        vmWriter.writeReturn();
    }

    /**
     * Compiles an if statement
     * possibly with a trailing else clause
     * 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
     */
    private void compileIf(){

        String elseLabel = newLabel();
        String endLabel = newLabel();

        //'('
        requireSymbol('(');
        //expression
        compileExpression();
        //')'
        requireSymbol(')');
        //if ~(condition) go to else label
        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        vmWriter.writeIf(elseLabel);
        //'{'
        requireSymbol('{');
        //statements
        compileStatement();
        //'}'
        requireSymbol('}');
        //if condition after statement finishing, go to end label
        vmWriter.writeGoto(endLabel);

        vmWriter.writeLabel(elseLabel);
        //check if 'else' exists
        // 'else' '{' statements '}'
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.ELSE){
            //'{'
            requireSymbol('{');
            //statements
            compileStatement();
            //'}'
            requireSymbol('}');
        }else{ // no else 
            tokenizer.pointerBack();
        }

        vmWriter.writeLabel(endLabel);
    }

    /**
     * Compiles a term.
     * This routine is faced with a slight difficulty when trying to decide between some of the alternative parsing rules.
     * Specifically, if the current token is an identifier
     *      the routine must distinguish between a variable, an array entry and a subroutine call
     * A single look-ahead token, which may be one of "[" "(" "." suffices to distinguish between the three possibilities
     * Any other token is not part of this term and should not be advanced over
     *
     * integerConstant|stringConstant|keywordConstant|varName|varName '[' expression ']'|subroutineCall|
     * '(' expression ')'|unaryOp term
     */
    private void compileTerm(){
        tokenizer.advance();

        //check if it is an identifier
        if (tokenizer.tokenType() == JackTokenizer.TYPE.IDENTIFIER){
            // varName | varName '[' expression ']' | subroutineCall
            String tempId = tokenizer.identifier();

            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '['){
                //array entry
                //push array variable, base address into stack
                vmWriter.writePush(getSeg(symbolTable.kindOf(tempId)), symbolTable.indexOf(tempId));

                //expression
                compileExpression();
                //']'
                requireSymbol(']');

                //base+offset
                vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);

                //pop into 'that' pointer
                vmWriter.writePop(VMWriter.SEGMENT.POINTER,1);
                //push *(base+index) onto stack
                vmWriter.writePush(VMWriter.SEGMENT.THAT,0);

            }else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')){
                //this is a subroutineCall
                tokenizer.pointerBack();tokenizer.pointerBack();
                compileSubroutineCall();
            }else {
                //this is varName
                tokenizer.pointerBack();
                //push variable directly onto stack
                vmWriter.writePush(getSeg(symbolTable.kindOf(tempId)), symbolTable.indexOf(tempId));
            }

        }else{
            //integerConstant | stringConstant | keywordConstant |'(' expression ')'| unaryOp term
            if (tokenizer.tokenType() == JackTokenizer.TYPE.INT_CONST){
                //integerConstant just push its value onto stack
                vmWriter.writePush(VMWriter.SEGMENT.CONST, tokenizer.intVal());
            }else if (tokenizer.tokenType() == JackTokenizer.TYPE.STRING_CONST){
                //stringConstant new a string and append every char to the new stack
                String str = tokenizer.stringVal();

                vmWriter.writePush(VMWriter.SEGMENT.CONST, str.length());
                vmWriter.writeCall("String.new",1);

                for (int i = 0; i < str.length(); i++){
                    vmWriter.writePush(VMWriter.SEGMENT.CONST,(int)str.charAt(i));
                    vmWriter.writeCall("String.appendChar",2);
                }

            }else if(tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.TRUE){
                //~0 is true
                vmWriter.writePush(VMWriter.SEGMENT.CONST,0);
                vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);

            }else if(tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.THIS){
                //push this pointer onto stack
                vmWriter.writePush(VMWriter.SEGMENT.POINTER,0);

            }else if(tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && (tokenizer.keyWord() == JackTokenizer.KEYWORD.FALSE || tokenizer.keyWord() == JackTokenizer.KEYWORD.NULL)){
                //0 for false and null
                vmWriter.writePush(VMWriter.SEGMENT.CONST,0);
            }else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '('){
                //expression
                compileExpression();
                //')'
                requireSymbol(')');
            }else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')){

                char s = tokenizer.symbol();

                //term
                compileTerm();

                if (s == '-'){
                    vmWriter.writeArithmetic(VMWriter.COMMAND.NEG);
                }else {
                    vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
                }

            }else {
                error("integerConstant|stringConstant|keywordConstant|'(' expression ')'|unaryOp term");
            }
        }

    }

    /**
     * Compiles a subroutine call
     * subroutineName '(' expressionList ')' | (className|varName) '.' subroutineName '(' expressionList ')'
     */
    private void compileSubroutineCall(){
        tokenizer.advance();

        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            error("identifier");
        }

        String name = tokenizer.identifier(); //subroutineName | (className|varName)
        int nArgs = 0;

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '('){
            //push this pointer
            vmWriter.writePush(VMWriter.SEGMENT.POINTER,0);
            //'(' expressionList ')'
            //expressionList
            nArgs = compileExpressionList() + 1;
            //')'
            requireSymbol(')');
            //call subroutine
            vmWriter.writeCall(currentClass + '.' + name, nArgs);

        }else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '.'){
            //(className|varName) '.' subroutineName '(' expressionList ')'

            String objName = name;
            //subroutineName
            tokenizer.advance();
           // System.out.println("subroutineName:" + tokenizer.identifier());
            if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
                error("identifier");
            }

            name = tokenizer.identifier(); //subroutineName

            //check for if it is built-in type
            String type = symbolTable.typeOf(objName);

            if (type.equals("int")||type.equals("boolean")||type.equals("char")||type.equals("void")){
                error("no built-in type");
            }else if (type.equals("")){
                name = objName + "." + name;
            }else {
                nArgs = 1;
                //push variable directly onto stack
                vmWriter.writePush(getSeg(symbolTable.kindOf(objName)), symbolTable.indexOf(objName));
                name = symbolTable.typeOf(objName) + "." + name;
            }

            //'('
            requireSymbol('(');
            //expressionList
            nArgs += compileExpressionList();
            //')'
            requireSymbol(')');
            //call subroutine
            vmWriter.writeCall(name,nArgs);
        }else {
            error("'('|'.'");
        }
    }

    /**
     * Compiles an expression
     * term (op term)*
     */
    private void compileExpression(){
        //term
        compileTerm();
        //(op term)*
        do {
            tokenizer.advance();
            //op
            if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.isOp()){

                String opCmd = "";

                switch (tokenizer.symbol()){
                    case '+':opCmd = "add";break;
                    case '-':opCmd = "sub";break;
                    case '*':opCmd = "call Math.multiply 2";break;
                    case '/':opCmd = "call Math.divide 2";break;
                    case '<':opCmd = "lt";break;
                    case '>':opCmd = "gt";break;
                    case '=':opCmd = "eq";break;
                    case '&':opCmd = "and";break;
                    case '|':opCmd = "or";break;
                    default:error("Unknown op!");
                }
                //term
                compileTerm();

                vmWriter.writeCommand(opCmd,"","");

            }else {
                tokenizer.pointerBack();
                break;
            }

        }while (true); 

    }

    /**
     * Compiles a (possibly empty) comma-separated list of expressions
     * (expression(','expression)*)?
     * @return nArgs
     */
    private int compileExpressionList(){
        int nArgs = 0;

        tokenizer.advance();
        //if ')'- go BACK - no args
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ')'){
            tokenizer.pointerBack();
        }else {
            nArgs = 1;
            tokenizer.pointerBack(); // pointerback because compileExpression() starts with advance()
            //expression
            compileExpression(); 
            //check if more expressions
            do {
                tokenizer.advance();
                if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ','){//more expressions
                    //expression
                    compileExpression();
                    nArgs++;
                }else {
                    tokenizer.pointerBack();
                    break;//end
                }

            }while (true); 
        }
        return nArgs;
    }

    /**
     * throw an exception to report errors
     * @param val
     */
    private void error(String val){
        throw new IllegalStateException("Expected token missing : " + val + " Current token:" + tokenizer.getCurrentToken());
    }

    /**
     * require symbol when we know there must be such symbol
     * @param symbol
     */
    private void requireSymbol(char symbol){
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || tokenizer.symbol() != symbol){
            error("'" + symbol + "'");
        }
    }
}