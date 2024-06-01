package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.visitors.*;

public class SymbolTablePomClass extends Tab {
	
	public static final Struct boolType = new Struct(200);
	
	public static void init() {
		Tab.init();
		currentScope.addToLocals(new Obj(2, "bool", boolType));
	}
	
	public static void openScope() {
		Tab.openScope();
	}
	
	public static void closeScope() {
		Tab.closeScope();
	}
	
	public static void chainLocalSymbols(Obj OuterScopeObj) {
		Tab.chainLocalSymbols(OuterScopeObj);
	}
	
	public static void chainLocalSymbols(Struct innerClass) {
		Tab.chainLocalSymbols(innerClass);
	}
	
	public static Obj insert(int kind, String name, Struct type) {
		Obj retObj = Tab.insert(kind, name, type);
		return retObj;
	}
	
	public static Obj find(String name) {
		Obj retObj = Tab.find(name);
		return retObj;
	}
	
	public static void dump(SymbolTableVisitor visitor) {
		Tab.dump();
	}
	
	public static void dump() {
		dump((SymbolTableVisitor)null);
	}
}
