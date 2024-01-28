import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CompilationEngine {

    private PrintWriter writer;
    private JackTokenizer jk;
    

     public CompilationEngine(File inputfile,File outputFile) throws FileNotFoundException{

        writer = new PrintWriter(outputFile);
        jk = new JackTokenizer(inputfile);
        
     }

     public void compileClass(){
        
        writer.println("<class>");
        writer.println("<keyword> class </keyword>");
        jk.advance();
        writer.println("<identifier> " +  jk.identifier() + " </identifier>");
        jk.advance(); 
        writer.println("<symbol> { </symbol>");
        compileClassVarDec();
        compileSubRoutine();
        writer.println("<symbol> } </symbol>");
        writer.println("</class>");
        writer.close();     
}

// writes all the static or field into the XML file
    private void compileClassVarDec() {

        jk.advance();

        while (jk.KeyWord().equals("static")||jk.KeyWord().equals("field")) {
            writer.println("<classVarDec>");
            // writes field or static
            writer.println("<keyword> " + jk.KeyWord() + " </keyword>");
            jk.advance();
            // can be var or identifier
            if (jk.tokenType().equals("identifier")) {
                writer.println("<identifier> " +  jk.identifier() + " </identifier>");   
            }else{
                writer.println("<keyword> " + jk.KeyWord() + " </keyword>");
            }
            jk.advance();
            writer.println("<identifier> " +  jk.identifier() + " </identifier>"); 
            jk.advance();
            // option to writes more the one variable 
            while(jk.symbol().equals(",")) {
                writer.println("<symbol> , </symbol>");
                jk.advance();
                writer.println("<identifier> " +  jk.identifier() + " </identifier>"); 
                jk.advance();
            }
            
            writer.println("<symbol> ; </symbol>");
            jk.advance();
            writer.println("</classVarDec>");
    
        }
        //going back to the previous token the start with advance in the next function
            jk.pointerBack();
    }

    // writes the functions methods constructor into output file - Recursive 
    public void compileSubRoutine() {
        boolean sub = false;
        jk.advance();
        
        // base case for the recursive call
        if(jk.symbol().equals("}") && jk.tokenType().equals("symbol")) {
            return;
        }

        if ((jk.KeyWord().equals("function") || jk.KeyWord().equals("method") || jk.KeyWord().equals("constructor")))  {
                writer.println("<subroutineDec>");
                sub = true;
                writer.println("<keyword> " + jk.KeyWord() + " </keyword>");
                jk.advance();
            }

            // identifier
            if (jk.tokenType().equals("identifier")) {
                writer.println("<identifier> "  + jk.identifier() + " </identifier>"); 
                jk.advance();
            }

            // keyword
            else if (jk.tokenType().equals("keyword")) {
                    writer.println("<keyword> " + jk.KeyWord() + " </keyword>");
                    jk.advance();
            }
                
            // name of subRoutine
            if (jk.tokenType().equals("identifier")) {
                writer.println("<identifier> "  + jk.identifier() + " </identifier>"); 
                jk.advance();
            }
            // get the sequence of "( parameters if threre are ) "
            if (jk.symbol().equals("(")) {
                writer.println("<symbol> ( </symbol>");
                writer.println("<parameterList>");
                compileParameterList();
                writer.println("</parameterList>");
                writer.println("<symbol> ) </symbol>");
            }
            jk.advance();

            // start Body
            if (jk.symbol().equals("{")) {
                writer.println("<subroutineBody>");
                writer.println("<symbol> { </symbol>");
                jk.advance();
            }
            // var declarations in the subroutine
            while(jk.KeyWord().equals("var") && (jk.tokenType().equals("keyword"))) {
                writer.print("<varDec>\n ");
                jk.pointerBack();
                compileVarDec();
                writer.println("</varDec>");
            }
            writer.println("<statements>");
            compileStatements();
            writer.println("</statements>");
            writer.println("<symbol> " + jk.symbol() + " </symbol>");
            if(sub) {
                writer.println("</subroutineBody>");
                writer.println("</subroutineDec>");
                
            }
            compileSubRoutine();    
    }

    private void compileParameterList() {
        jk.advance();
        //writes all whats inside (..)
        while (!(jk.tokenType().equals("symbol")&&(jk.symbol().equals(")")))) {
            // identifiers
            if(jk.tokenType().equals("identifier")) {
                writer.println("<identifier> " +  jk.identifier() + " </identifier>");
                jk.advance();
            }
             // keywodrs
            else if(jk.tokenType().equals("keyword")){
                writer.println("<keyword> " + jk.KeyWord() + " </keyword>");
                jk.advance();
            }
             //writes symbols
            else if(jk.tokenType().equals("symbol")&&jk.symbol().equals(",")){
                writer.println("<symbol> , </symbol>");
                jk.advance();
            }        
        }
    }

    private void compileVarDec() {
        jk.advance();
        // check
        if (jk.KeyWord().equals("var")&&jk.tokenType().equals("keyword")) {

            writer.println("<keyword> " + jk.KeyWord() + " </keyword>");
            jk.advance();   
        }
            // identifiers
            if (jk.tokenType().equals("identifier")) {
                writer.println("<identifier> " +  jk.identifier() + " </identifier>");
                jk.advance();
            }
             // keywodrs
            else if(jk.tokenType().equals("keyword")){
                writer.println("<keyword> " + jk.KeyWord() + " </keyword>");
                jk.advance();
            }
            // identifiers name
            if (jk.tokenType().equals("identifier")) {
                writer.println("<identifier> " +  jk.identifier() + " </identifier>");
                jk.advance();
            }
             //writes symbols
             while(jk.symbol().equals(",")) {
                writer.println("<symbol> , </symbol>");
                jk.advance();
                writer.println("<identifier> " +  jk.identifier() + " </identifier>"); 
                jk.advance();
            }
        writer.println("<symbol> ; </symbol>");
        jk.advance();
    }

    // do/ let/ while/ if/ return 
    private void compileStatements() {
        if(jk.symbol().equals("}") && (jk.tokenType().equals("symbol"))) {
            return;
        }
        // do  
        else if (jk.KeyWord().equals("do") && (jk.tokenType().equals("keyword"))) {
            writer.print("<doStatement>\n ");
            compileDo();
            writer.println("</doStatement>");

        }
        // let   
        else if (jk.KeyWord().equals("let") && (jk.tokenType().equals("keyword"))) {
            writer.print("<letStatement>\n ");
            compileLet();
            writer.println("</letStatement>");
        } 
        // if  
        else if (jk.KeyWord().equals("if") && (jk.tokenType().equals("keyword"))) {
            writer.print("<ifStatement>\n ");
            compileIf();
            writer.println("</ifStatement>");
        } 
        // while  
        else if (jk.KeyWord().equals("while") && (jk.tokenType().equals("keyword"))) {
            writer.print("<whileStatement>\n ");
            compileWhile();
            writer.println("</whileStatement>");
        } 
        // return 
        else if (jk.KeyWord().equals("return") && (jk.tokenType().equals("keyword"))) {
            writer.print("<returnStatement>\n ");
            compileReturn();
            writer.println("</returnStatement>");
        }

        jk.advance();

        // recursive call
        compileStatements();

    }

    private void compileDo() {
        writer.println("<keyword> do </keyword>");
        jk.advance();

        // first identifier
        writer.println("<identifier> " +  jk.identifier() + " </identifier>");
        jk.advance();

            // if its a type of identifier.identinfier();
            if ((jk.tokenType().equals("symbol")) && (jk.symbol().equals( "."))) {
                writer.println("<symbol> . </symbol>");
                jk.advance();
                writer.println("<identifier> " + jk.identifier() + " </identifier>");
                jk.advance();
                writer.println("<symbol> " + jk.symbol() + " </symbol>");

                // parameters in the parentheses
                writer.println("<expressionList>");
                compileExpressionList();
                writer.println("</expressionList>");
                jk.advance();
                writer.println("<symbol> " + jk.symbol() + " </symbol>");


            }
            // if if its a type of identifier();
            else if ((jk.tokenType().equals("symbol")) && (jk.symbol().equals("("))) {
                writer.println("<symbol> " + jk.symbol() + " </symbol>");
                writer.println("<expressionList>");
                compileExpressionList();
                writer.println("</expressionList>");

                // parentheses )
                jk.advance();
                writer.println("<symbol> " + jk.symbol() + " </symbol>");
            }
    
        jk.advance();
        writer.println("<symbol> " + jk.symbol() + " </symbol>");
    }

    public void compileExpressionList() {
        jk.advance();

        // if end of list
        if ((jk.symbol().equals(")")) && jk.tokenType().equals("symbol")) {
            jk.pointerBack();
        } else {
            jk.pointerBack();
            compileExpression();
        }
        while (true) {
            jk.advance();
            if (jk.tokenType().equals("symbol") && (jk.symbol().equals(","))) {
                writer.println("<symbol> , </symbol>");
                compileExpression();
            } else {
                jk.pointerBack();
                break;
            }
        }
    }

    private void compileLet() {
        // let
        writer.println("<keyword> let </keyword>");
        jk.advance();

        // identifier
        writer.println("<identifier> " +  jk.identifier() + " </identifier>");
        jk.advance();

        // [] arr
        if ((jk.tokenType().equals("symbol")) && (jk.symbol().equals("["))) {
            writer.println("<symbol> [ </symbol>");
            compileExpression();
            jk.advance(); 
                writer.println("<symbol> ] </symbol>"); 
                jk.advance();
        }
        // =
        writer.println("<symbol> = </symbol>");
        compileExpression();
        // ;
        writer.println("<symbol> ; </symbol>");
        jk.advance();
    }

    private void compileIf() {
        writer.println("<keyword> if </keyword>"); //if
        jk.advance();
        writer.println("<symbol> ( </symbol>");
        compileExpression(); //whats inside the If
        writer.println("<symbol> ) </symbol>"); 
        jk.advance();
        writer.println("<symbol> { </symbol>"); //scope
        jk.advance();
        writer.println("<statements>");
        compileStatements();
        writer.println("</statements>");
        writer.println("<symbol> } </symbol>"); // end of scope
        jk.advance();
        //else
            if (jk.tokenType().equals("keyword") && jk.KeyWord().equals("else")) {
                writer.println("<keyword> else </keyword>");
                jk.advance();
                writer.println("<symbol> { </symbol>");
                jk.advance();
                writer.println("<statements>");
                compileStatements();
                writer.println("</statements>");
                writer.println("<symbol> } </symbol>");
            }else{
                jk.pointerBack();
            }
    }

    private void compileWhile() {
            // while
            writer.println("<keyword> while </keyword>");
            jk.advance();
            // (
            writer.println("<symbol> ( </symbol>");
            // expression
            compileExpression(); // whats inside the WHILE
            // )
            jk.advance(); 
            writer.println("<symbol>  ) </symbol>");
            jk.advance();
            writer.println("<symbol> { </symbol>");      // {
            // scope begins
            writer.println("<statements>");
            compileStatements();
            writer.println("</statements>");
            // } scope ends
            writer.println("<symbol> } </symbol>");
    }

    private void compileReturn() {
        writer.println("<keyword> return </keyword>");
        jk.advance();
            if (!((jk.tokenType().equals("symbol") && (jk.symbol().equals(";"))))) { //return something = not VOID
                jk.pointerBack();
                compileExpression(); // what is return
            }
            writer.println("<symbol> ; </symbol>");
        }

    private void compileExpression() {
        writer.println("<expression>");
            compileTerm();
            while(true) {
                jk.advance();
                if (jk.tokenType().equals("symbol") && jk.isOp()) {
                    if (jk.symbol().equals("<")) { // < = &lt
                        writer.println("<symbol> &lt; </symbol>");
                    } else if (jk.symbol().equals(">")) {// > = &gt
                        writer.println("<symbol> &gt; </symbol>");
                    } else if (jk.symbol().equals("&")) { // & = &amp
                        writer.println("<symbol> &amp; </symbol>");
                    } else {
                        //arithmetic
                        writer.println("<symbol> " + jk.symbol() + " </symbol>");
                    }
                    compileTerm();
                }else{
                    jk.pointerBack();
                    break;
                }
            }
            writer.println("</expression>");
    }

    private void compileTerm() {
        writer.println("<term>");
            jk.advance();
            if (jk.tokenType().equals("identifier")) {
                String tempIdentifier = jk.identifier();
                jk.advance();

                // [] 
                if (jk.tokenType().equals("symbol") && jk.symbol().equals("[")) {
                    writer.println("<identifier> " + tempIdentifier + " </identifier>");
                    writer.println("<symbol> [ </symbol>");
                    compileExpression();
                    jk.advance();
                    writer.println("<symbol> ] </symbol>");
                }
                // ( or . 
                else if (jk.tokenType().equals("symbol") && jk.symbol().equals("(") || jk.symbol().equals(".")) {
                    jk.pointerBack();
                    //same as compileDo
                    // first identifier
                    //writer.println("<identifier> " +  jk.identifier() + " </identifier>");
                    writer.println("<identifier> " +  tempIdentifier + " </identifier>");
                    jk.advance();
                    if ((jk.tokenType().equals("symbol")) && (jk.symbol().equals( "."))) {
                        writer.println("<symbol> . </symbol>");
                        jk.advance();
                        writer.println("<identifier> " + jk.identifier() + " </identifier>");
                        jk.advance();
                        writer.println("<symbol> " + jk.symbol() + " </symbol>");
        
                        // whats inside (  )
                        writer.println("<expressionList>");
                        compileExpressionList();
                        writer.println("</expressionList>");
                        jk.advance();
                        writer.println("<symbol> " + jk.symbol() + " </symbol>");
                    }
                    // identifier();
                    else if ((jk.tokenType().equals("symbol")) && (jk.symbol().equals("("))) {
                        writer.println("<symbol> " + jk.symbol() + " </symbol>");
                        writer.println("<expressionList>");
                        compileExpressionList();
                        writer.println("</expressionList>");
        
                        // )
                        jk.advance();
                        writer.println("<symbol> " + jk.symbol() + " </symbol>");
                    }
                } else {
                    writer.println("<identifier> " + tempIdentifier + " </identifier>");
                    jk.pointerBack();
                }
            } else {
                // numbers
                if (jk.tokenType().equals("integerConstant")) {
                    writer.println("<integerConstant> " + jk.intVal() + " </integerConstant>");

                }
                // strings
                else if (jk.tokenType().equals("stringConstant")) {
                    writer.println("<stringConstant> " + jk.stringVal() + " </stringConstant>");
                }
                // this boolean or null
                else if (jk.tokenType().equals("keyword") && ((jk.KeyWord().equals("this") || jk.KeyWord().equals("NULL")|| jk.KeyWord().equals("TRUE")||jk.KeyWord().equals("FALSE")))) {
                    writer.println("<keyword> " + jk.KeyWord() + " </keyword>");
                }
                // parenthetical separation
                else if (jk.tokenType().equals("symbol") && jk.symbol().equals("(")) {
                    writer.println("<symbol> " + jk.symbol() + " </symbol>");
                    compileExpression();
                    jk.advance();
                    writer.println("<symbol> " + jk.symbol() + " </symbol>");
                }
                // unary operators
                else if (jk.tokenType().equals("symbol") && (jk.symbol().equals("-") || jk.symbol().equals("~"))) {
                    writer.println("<symbol> " + jk.symbol() + " </symbol>");

                    compileTerm();
                }
            }
            writer.println("</term>");
    }
} 