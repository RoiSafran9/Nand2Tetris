import java.util.HashMap;
import java.util.Map;

/**
 * This module provides services for creating and using a symbol table.
 * Recall that each symbol has a scope from which it is visible in the source code.
 * The symbol table implements this abstraction by giving each symbol a running number (index) within the scope.
 * The index starts at 0, increments by 1 each time an identifier is added to the table, and resets to 0 when starting a new scope.
 * The following kinds of identifiers may appear in the symbol table:
 *  Static: Scope: class.
 *  Field: Scope: class.
 *  Argument: Scope: subroutine (method/function/constructor).
 *  Var: Scope: subroutine (method/function/constructor).
 *
 *  When compiling error-free Jack code, any identifier not found in the symbol table may be assumed to be a subroutine name or a class name.
 *  Since the Jack language syntax rules suffice for distinguishing between these two possibilities, and since no linking needs to be done by the compiler,
 *  there is no need to keep these identifiers in the symbol table.
 *
 *  Provides a symbol table abstraction.
 *  The symbol table associates the identifier names found in the program with identifier properties needed for compilation:
 *  type, kind, and running index. The symbol table for Jack programs has two nested scopes (class/subroutine).
 */
public class SymbolTable {

    private HashMap<String,Symbol> classSymbols;//for STATIC, FIELD
    private HashMap<String,Symbol> subroutineSymbols;//for ARG, VAR
    private HashMap<Symbol.KIND,Integer> indices; // kinds counter 


     //creates a new empty symbol table
     //init all indices
    public SymbolTable() {
        classSymbols = new HashMap<String, Symbol>();
        subroutineSymbols = new HashMap<String, Symbol>();

        indices = new HashMap<Symbol.KIND, Integer>();
        indices.put(Symbol.KIND.ARG,0);
        indices.put(Symbol.KIND.FIELD,0);
        indices.put(Symbol.KIND.STATIC,0);
        indices.put(Symbol.KIND.VAR,0);
    }

    //new subroutine scope
    //resets the subroutine's symbol table
    public void startSubroutine(){
        subroutineSymbols.clear(); //reset
        indices.put(Symbol.KIND.VAR,0); //Var
        indices.put(Symbol.KIND.ARG,0); //Arg
    }

    /**
     * Defines a new identifier of a given name,type and kind
     * and assigns it a running index, STATIC and FIELD identifiers
     * jave a class scope, while ARG and VAR identifiers have a subroutine scope
     * @param name
     * @param type
     * @param kind
     */
    public void define(String name, String type, Symbol.KIND kind){

        if (kind == Symbol.KIND.ARG || kind == Symbol.KIND.VAR){
            //SubRoutine Table
            int index = indices.get(kind);
            Symbol symbol = new Symbol(type,kind,index); //create
            indices.put(kind,index+1);
            subroutineSymbols.put(name,symbol);

        }else if(kind == Symbol.KIND.STATIC || kind == Symbol.KIND.FIELD){ 
            //class Table
            int index = indices.get(kind);
            Symbol symbol = new Symbol(type,kind,index);
            indices.put(kind,index+1);
            classSymbols.put(name,symbol);
        }
    }

    
    //returns the number of variables of the kind already defined in the current scope
    public int varCount(Symbol.KIND kind){
        return indices.get(kind);
    }

    /**
     * returns the kind of the named identifier in the current scope
     * if the identifier is unknown in the current scope returns NONE */
    public Symbol.KIND kindOf(String name){
        Symbol symbol = isExist(name);

        if (symbol != null) return symbol.getKind(); //symbol not exist in the table - get his kind

        return Symbol.KIND.NONE; // unknown symbol
    }

    /**
     * returns the type of the named identifier in the current scope
     */
    public String typeOf(String name){

        Symbol symbol = isExist(name); 

        if (symbol != null) return symbol.getType(); //symbol not exist in the table - get his type

        return "";
    }

    /**
     * returns the index assigned to the named identifier
     */
    public int indexOf(String name){

        Symbol symbol = isExist(name);

        if (symbol != null) return symbol.getIndex(); //symbol not exist in the table - get his type

        return -1; // symbol already exist
    }

    
    //check if target symbol exists
    // returns it if exist NULL if not exist
    private Symbol isExist(String name){ //isExist

        if (classSymbols.get(name) != null){
            return classSymbols.get(name);
        }else if (subroutineSymbols.get(name) != null){
            return subroutineSymbols.get(name);
        }else {
            return null;
        }
    }

}