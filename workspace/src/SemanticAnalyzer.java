package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.mj.runtime.*;
import rs.etf.pp1.symboltable.*;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;


public class SemanticAnalyzer extends VisitorAdaptor {

    int printCallCount = 0;
    int varDeclCount = 0;
    int stepenIf = 0;
    private  int local_variables_count = 0;
    private  int global_variables_count = 0;
    int numberOfVariables = 0;
    int formalParamCount = 0;
    boolean imaPovratnuVrednost = false;
    private static Struct currType = SymbolTablePomClass.noType;
    Obj currentMethod = SymbolTablePomClass.noObj;
    boolean metodaValidna = false;
    Label currlabel;
    public ArrayList<Obj> labelsNormal = new ArrayList<Obj>();
	public ArrayList<Obj> labelsGoto = new ArrayList<Obj>();
	
    int breakCounter = 0;
    int continueCounter = 0;

    public ArrayList < Struct > actualParams = new ArrayList < > ();
    public ArrayList < Struct > formalParams = new ArrayList < > ();
    boolean errorDetected = false;

    Logger log = Logger.getLogger(getClass());

    public SemanticAnalyzer() {
        SymbolTablePomClass.init();
    }
    
    public int getGlobal_variables_count() {
        return global_variables_count;
    }

    //pomocne metode za tabelu simbola

    private boolean symDef(String name) {
        Obj find = SymbolTablePomClass.find(name);
        if (find == SymbolTablePomClass.noObj) {
            return false;
        } else return true;
    }

    private boolean defAllowed(String name, SyntaxNode info) {
        if (SymbolTablePomClass.currentScope().findSymbol(name) != null) {
            return true;
        } else {
            report_info("Vec postoji simbol sa ovim imenom", info);
            return false;
        }
    }

    //pomocna metoda za dodeljivanje vrednosti

    private boolean typesAssignable(Struct first, Struct second) {
        if (first == SymbolTablePomClass.intType && second == SymbolTablePomClass.intType) return true;
        if (first == SymbolTablePomClass.charType && second == SymbolTablePomClass.charType) return true;
        //u Tab klasi ne postoji boolean type, pa moramo dodati u symboltablepomclass boolType
        if (first == SymbolTablePomClass.boolType && second == SymbolTablePomClass.boolType) return true;

        return typesAssignablePom(first, second);
    }

    private boolean typesAssignablePom(Struct first, Struct second) {
        if (first == Tab.noType && second.getKind() == Struct.Array) return true;

        if (first.getKind() == Struct.Array && second.getKind() == Struct.Array) {
            first = first.getElemType();
            second = second.getElemType();
        }

        if (first != second) return false;

        return true;
    }

    //ispisi -> error i info

    public void report_error(String message, SyntaxNode info) {
        errorDetected = true;
        StringBuilder msg = new StringBuilder(message);
        int line = (info == null) ? 0 : info.getLine();
        if (line != 0)
            msg.append(" na liniji ").append(line);
        log.error(msg.toString());
    }

    public void report_info(String message, SyntaxNode info) {
        StringBuilder msg = new StringBuilder(message);
        int line = (info == null) ? 0 : info.getLine();
        if (line != 0)
            msg.append(" na liniji ").append(line);
        log.info(msg.toString());
    }

    //Program

    public void visit(ProgName progName) {
        progName.obj = SymbolTablePomClass.insert(Obj.Prog, progName.getProgName(), Tab.noType);
        SymbolTablePomClass.openScope();
    }

    //deklaracija za program, treba ulancati sve lokalne simbole
private boolean isMainDefined = false;
    public void visit(Program program) {

        //mozemo proveriti da li postoji funkcija main

    	if(global_variables_count > 65536) {
    		report_error("Program ima vise od 65536 variabli", program);
    	}
    	
    	SymbolTablePomClass.chainLocalSymbols(program.getProgName().obj);
    	SymbolTablePomClass.closeScope();
    }

    //deklaracija globalnih promenljivih

    public void visit(Type type) {

        if (!symDef(type.getTypeName())) {
            report_error("Ne postoji tip", type);
            currType = SymbolTablePomClass.noType;
            return;
        }
        Obj typeNode = SymbolTablePomClass.find(type.getTypeName());

        if (Obj.Type == typeNode.getKind()) {
            currType = typeNode.getType();
        } else {
            report_error("Greska: Ime nije predefinisani tip" + type.getTypeName() + "ne predstavlja tip", type);
            currType = SymbolTablePomClass.noType;
        }

        type.struct = typeNode.getType();

        report_info("Obidjen type " + type.getTypeName(), type);
    }

    //KONSTANTE

    public void visit(ConstAlone konstanta) {

        String name = konstanta.getSingleConstName();

        if(currType == SymbolTablePomClass.noType) {
        	report_error("Const tip nije tacan", konstanta);
        	return;
        }
        
        Obj constObj = SymbolTablePomClass.find(name);
        if(constObj != SymbolTablePomClass.noObj) {
        	report_error("Simbol je vec definisan", konstanta);
        	return;
        }
        
        int constVal = 0;
        Struct rightSide = SymbolTablePomClass.noType;
        
        if(konstanta.getConstDeclExp() instanceof ConstN) {
        	rightSide = SymbolTablePomClass.intType;
        	constVal = ((ConstN)konstanta.getConstDeclExp()).getNumVal();
        } else if(konstanta.getConstDeclExp() instanceof ConstCh) {
        	rightSide = SymbolTablePomClass.charType;
        	constVal = ((ConstCh)konstanta.getConstDeclExp()).getCharVal();
        } else if(konstanta.getConstDeclExp() instanceof ConstB) {
        	rightSide = SymbolTablePomClass.boolType;
        	if(((ConstB)konstanta.getConstDeclExp()).getBoolVal()) {
        		constVal = 1;
        	} else constVal = 0;
        	}
        	
         else {
        	report_error("Nijedan od tipova nije dobar", currlabel);
        
         }
        
        if(typesAssignable(currType, rightSide)) {
        Obj newConst = SymbolTablePomClass.insert(Obj.Con, name, rightSide);
        newConst.setAdr(constVal);
        newConst.setLevel(0);
        
        } else {
        	report_error("Const init value je pogresnog tipa", konstanta);
        }
    }
    


    public void visit(ConstN numConst) {
        numConst.struct = SymbolTablePomClass.intType;
    }

    public void visit(ConstCh charConst) {
        charConst.struct = SymbolTablePomClass.charType;
    }

    public void visit(ConstB boolConst) {
        boolConst.struct = SymbolTablePomClass.boolType;
    }

    //VARIJABLE
    
    public void visit(VarDeclaration varDecl) {
    	
    	if (currType == SymbolTablePomClass.noType) {
            report_error("Tip nije dobar", varDecl); //bag from type
            return;
        }
    	
    	String varName = varDecl.getVarName();
        if (defAllowed(varName, varDecl)) {
            report_error("Vec postoji simbol sa ovim imenom", varDecl);
            return;
        }
        

        ArrOpt arrOpt = varDecl.getArrOpt();
        Obj newVar;
        if (arrOpt instanceof ArrayOption) {
            newVar = SymbolTablePomClass.insert(Obj.Var, varName, new Struct(Struct.Array, currType));
        }
        else {
            newVar = SymbolTablePomClass.insert(Obj.Var, varName, currType);
        }

        if (currentMethod == SymbolTablePomClass.noObj) global_variables_count++;
       
        else local_variables_count++;
        
        System.out.print(global_variables_count);
        System.out.print(local_variables_count);


        report_info("variable named " + varDecl.getVarName() + " is defined. Object string: " + newVar.toString() + " ", varDecl);

       report_info("VarDeclaration visit", varDecl);
    	
    }



    //METODE

    public void visit(ReturnVoid noType) {
        noType.struct = Tab.noType;
    }


    public void visit(MethodTypeName methodTypeName) {

    	String methodName = methodTypeName.getMethodName();
        metodaValidna = true;

        if (symDef(methodName)) {
            report_error("Symbol used for method name is already defined in this scope", methodTypeName);
            metodaValidna = false;
        }

        RetType retType = methodTypeName.getRetType();
        Struct type;

        if (retType instanceof ReturnVoid) {
            type = SymbolTablePomClass.noType;
        } else {
            type = currType;
        }
        
        if(methodName.equals("main")) {
        	if (isMainDefined) {
        		report_error("Imamo vise metoda koje se zovu main", retType);
        		metodaValidna = false;
        	} 
        	isMainDefined = true;
        }


        if (metodaValidna) {
            methodTypeName.obj = currentMethod = SymbolTablePomClass.insert(Obj.Meth, methodName, type);
        } else {
            methodTypeName.obj = currentMethod = new Obj(Obj.Meth, methodName, type);
        }
        SymbolTablePomClass.openScope();
        imaPovratnuVrednost = false;
        currentMethod.setLevel(0); //set number of parameters to 0 before parameters list
       
        report_info("Obradjuje se funkcija " + methodTypeName.getMethodName(), methodTypeName);

 
    }
    
    private boolean isMainCorrect() {
        return currentMethod.getLevel() == 0 && currentMethod.getType() == SymbolTablePomClass.noType;
    }

    //zatvaranje metode
    public void visit(MethodDecl methodDecl) {
    	if (currentMethod == SymbolTablePomClass.noObj) {
            report_error("dsada", methodDecl);
            return;
        }

        if (metodaValidna) SymbolTablePomClass.chainLocalSymbols(currentMethod);

        String methodName = currentMethod.getName();
        if(methodName.equals("main") && !isMainCorrect()) {
        	
        	report_error("nesto nije ok", methodDecl);
        }
        
        currentMethod = SymbolTablePomClass.noObj;
        SymbolTablePomClass.closeScope();
        
        if(local_variables_count > 256) {
        	report_error("ima vise od 256 lok simbola", methodDecl);
        }
        
        local_variables_count = 0;
    }

    //FORM PARAMS

    public void visit(FormParams fp) {
        currentMethod.setLocals(SymbolTablePomClass.currentScope().getLocals());
    }

    public void visit(FormalParam formalParam) {

        String imeFormalnogParametra = formalParam.getFormalParamName();

        if (currType == SymbolTablePomClass.noType) {
            report_error("Tip parametra moze biti int/char/bool", formalParam);
        }

        if (defAllowed(imeFormalnogParametra, formalParam)) {
            report_error("Simbol sa ovim imenom vec postoji u tabeli simbola" + imeFormalnogParametra, formalParam);
        }

        if (formalParam.getArrOpt() instanceof ArrOpt) {
            Obj formalParameters = SymbolTablePomClass.insert(Obj.Var, imeFormalnogParametra, new Struct(Struct.Array, currType));
            formalParameters.setFpPos(1);
            formalParams.add(formalParam.getType().struct);
        } else {
            Obj formalParameter = SymbolTablePomClass.insert(Obj.Var, imeFormalnogParametra, currType);
            formalParameter.setFpPos(1);
            formalParams.add(formalParam.getType().struct);
        }
    }

    //STATEMENTS

    public void visit(If ifstmt) {
    	stepenIf++;
    }

    public void visit(IfElseStatement ifelseStmt) {
        if (stepenIf > 0) {
            stepenIf--;
        } else {
            report_error("Ne moze se izvrsiti if-else statement", ifelseStmt);
        }

    }
    //napomena, break i continue mogu da se nalaze samo unutar do-while petlje, jer nemam druge petlje
    private static int doWhile = 0;

    public void visit(DoStmt dostmt) {
        doWhile++;
        breakCounter++;
        continueCounter++;
    }

    public void visit(DoWhileStatement dwStmt) {
        if (doWhile <= 0) {
            report_error("Greska pri ugnjezdavanju do while petlje", dwStmt);
        } else {
            doWhile--;
        }
        breakCounter--;
        continueCounter--;
    }

    public void visit(BreakStatement breakStmt) {
        if (breakCounter <= 0) {
            report_error("Break neispravan", breakStmt);
        } else {
            report_info("Ispravan brake", breakStmt);
        }
    }

    public void visit(ContinueStatement continueStmt) {
        if (continueCounter <= 0) {
            report_error("Break neispravan", continueStmt);
        } else {
            report_info("Ispravan brake", continueStmt);
        }
    }

    public void visit(ReturnStatement returnStmt) {
        if (returnStmt.getOptExpr() instanceof ExprO && !typesAssignable(((ExprO) returnStmt.getOptExpr()).getExpr().struct, currentMethod.getType())) {
            report_error("Povratna vrednost funkcije i return vrednost nisu iste", returnStmt);
        }
        if (returnStmt.getOptExpr() instanceof NoExpression && currentMethod.getType() != SymbolTablePomClass.noType) {
            report_error("Funkcija ima povratnu vrednost, fali return", returnStmt);
        }
        if (returnStmt.getOptExpr() instanceof ExprO) imaPovratnuVrednost = true;

    }

    public void visit(ReadStatement readStmt) {
        Obj designator = readStmt.getDesignator().obj;
        Struct tipDesignatora = designator.getType();

        if (tipDesignatora == SymbolTablePomClass.intType || tipDesignatora == SymbolTablePomClass.charType || tipDesignatora == SymbolTablePomClass.boolType) {
            report_info("Moze da se izvrsi read()", readStmt);
        } else {
            report_error("Read nema dobre parametre", readStmt);
        }
        
        if(designator.getKind() != Obj.Var) {
        	report_error("Read parameter nije varijabla", readStmt);
        }

    }
    
    public void visit(PrintSmth print) {
    	printCallCount++; 
    	if(print.getExpr().struct != SymbolTablePomClass.intType && print.getExpr().struct != SymbolTablePomClass.charType && print.getExpr().struct != SymbolTablePomClass.boolType) {
    		report_error("Operand mora biti tipa int/char ili bool", print);
    	}
    }
    
    public void visit(PrintTwo printNumber) {
    	printCallCount++;
    	if(printNumber.getExpr().struct != SymbolTablePomClass.intType && printNumber.getExpr().struct != SymbolTablePomClass.charType && printNumber.getExpr().struct != SymbolTablePomClass.boolType) {
    		report_error("Operand mora biti tipa int/char ili bool", printNumber);
    	}
    }
   /* public void visit(PrintStatement printStmt) {
        Struct izraz = printStmt.getExpr().struct;
        if (izraz != SymbolTablePomClass.intType && izraz != SymbolTablePomClass.charType && izraz != SymbolTablePomClass.boolType) {
            report_error("Tipovi koji mogu da ispisuju moraju biti tipa int/char/bool " + izraz.getKind(), printStmt);
        }
        printCallCount++;
       
    }*/

    //DESIGNATOR

    public void visit(DesignatorAssignOp desAssign) {
        Obj design = desAssign.getDesignator().obj;

        if (design == SymbolTablePomClass.noObj) {
            report_error("Ne postoji objekat designatora", desAssign);
            return;
        }


        Struct first = design.getType();
        Struct second = desAssign.getExpr().struct;

        if (!typesAssignable(first, second)) {
            report_error("Nisu dobri tipovi", desAssign);
        }
     report_info("Designator Assign Operation obidjen", desAssign);
    }


    public void visit(DesignatorActPars methodDesignator) {
        Designator designator = methodDesignator.getDesignator();
        Obj designatorObj = designator.obj;

        if (!(designator instanceof DesignatorVar)) {
            report_error("PMetoda je pozvana za niz", designator);
        }

        if (designatorObj.getKind() != Obj.Meth) {
            report_error("Nije metoda", designator);
        }

        methodDesignator.obj = designatorObj;
    }

    public void visit(DesignatorIncrement desiInc) {

        Obj desiObj = desiInc.getDesignator().obj;

        if (desiObj == SymbolTablePomClass.noObj) {
            report_error("Greska, designator ne postoji", desiInc);
        }

        if (desiObj.getKind() != Obj.Var && desiObj.getKind() != Obj.Elem) {
            report_error("Vrednost nije promenljiva", desiInc);
        }

        if (!(desiObj.getType().assignableTo(SymbolTablePomClass.intType))) {
            report_error("Nije integer promenljiva", desiInc);
        }
    }

    public void visit(DesignatorDecrement desiDec) {
        Obj desiObj = desiDec.getDesignator().obj;
        if (desiObj == SymbolTablePomClass.noObj) {
            report_error("Greska, designator ne postoji", desiDec);
        }

        if (desiObj.getKind() != Obj.Var && desiObj.getKind() != Obj.Elem) {
            report_error("Vrednost nije promenljiva", desiDec);
        }

        if (!(desiObj.getType().assignableTo(SymbolTablePomClass.intType))) {
            report_error("Nije integer promenljiva", desiDec);
        }

    }

    //CONDITION
    public void visit(ConditionFactRelopExpr condRelop) {
        Struct first = condRelop.getExpr().struct;
        Struct second = condRelop.getExpr1().struct;
        RelOp relationOp = condRelop.getRelOp();

        if (typesAssignable(first, second)) {
            condRelop.struct = SymbolTablePomClass.boolType;
        } else {
            report_error("Tipovi se ne poklapaju", relationOp);
            condRelop.struct = SymbolTablePomClass.noType;
        }

    }

   public void visit(ConditionFactExpr condSingle) {
        condSingle.struct = condSingle.getExpr().struct;
    }

    public void visit(FirstConditionFactList condFirst) {
        Struct tipCond = condFirst.getCondFact().struct;
        if (tipCond == SymbolTablePomClass.boolType) {
            condFirst.struct = SymbolTablePomClass.boolType;
        } else {
            report_error("Uslovni terminal nije bool", condFirst);
            condFirst.struct = SymbolTablePomClass.boolType;
        }
    }

    public void visit(ConditionFactList condList) {
        Struct first = condList.getCondTerm().struct;
        Struct second = condList.getCondFact().struct;

        if (first != SymbolTablePomClass.boolType || second != SymbolTablePomClass.boolType) {
            report_error("Jedan od uslova nije bool tip", condList);
            condList.struct = SymbolTablePomClass.boolType;
        } else {
            condList.struct = SymbolTablePomClass.boolType;
        }
    }

    public void visit(ConditionTermList condTerm) {
        if (condTerm.getCondition().struct == SymbolTablePomClass.boolType && condTerm.getCondTerm().struct == SymbolTablePomClass.boolType) {
            condTerm.struct = SymbolTablePomClass.boolType;
        } else {
            report_error("Neki od tipova nije bool", condTerm);
            condTerm.struct = SymbolTablePomClass.noType;
        }
    }

    public void visit(FirstConditionTermList firstCond) {
        firstCond.struct = firstCond.getCondTerm().struct;
    }

    
    //EXPRESSION



    public void visit(Expression expr) {
        expr.struct = expr.getTerm().struct;
        Struct tipExpr = expr.struct;
        //expr.struct = SymbolTablePomClass.noType;
        if (expr.getMinusMayExist() instanceof MinusExists) {
            if (tipExpr != SymbolTablePomClass.intType) {
                report_error("Ne mozemo dodeliti minus necemo sto nije integer", expr);
                expr.struct = SymbolTablePomClass.noType;
            }
        }

        if (!(expr.getTermList() instanceof NoTerm) && expr.getTerm().struct != SymbolTablePomClass.intType) {
            report_error("Ne mozemo sabirati dve vrednosti koje nisu integer", expr);
        }
    }


    public void visit(TerminalList termList) {
        Struct first = termList.getTerm().struct;
        Struct second = termList.getTermList().struct;

        if (first != SymbolTablePomClass.intType && second != SymbolTablePomClass.intType) {
            report_error("Jedan od uslova nije tipa int", termList);
        } else {
            termList.struct = SymbolTablePomClass.intType;
        }
    }

    public void visit(Terminal termM) {
        Struct tipTerminala = termM.getTerm().struct;
        Struct tipFaktora = termM.getFactor().struct;
        if (tipTerminala != SymbolTablePomClass.intType || tipFaktora != SymbolTablePomClass.intType) {
            report_error("Greska, mnozenje nije tipa int", termM);
        }
        termM.struct = termM.getTerm().struct;
    }

    public void visit(TermF termF) {
        termF.struct = termF.getFactor().struct;
    }

    public void visit(FactNumber broj) {
        broj.struct = SymbolTablePomClass.intType;
    }

    @Override
    public void visit(FactCharacter karakter) {
        karakter.struct = SymbolTablePomClass.charType;
    }
    @Override
    public void visit(FactBoolean bool) {
        bool.struct = SymbolTablePomClass.boolType;
    }

    public void visit(FactParenExpr parenExpr) {
        parenExpr.struct = parenExpr.getExpr().struct;
    }

    public void visit(FactNewExpr newExpr) {
        newExpr.struct = new Struct(Struct.Array, newExpr.getType().struct);

        if (SymbolTablePomClass.intType.compatibleWith(newExpr.struct)) {
            report_error("Izraz u NEW nije int", newExpr);
            newExpr.struct = SymbolTablePomClass.noType;
        } else {
            newExpr.struct = new Struct(Struct.Array, newExpr.getType().struct);
            report_info("Kreiran niz", newExpr);
        }
    }

    public void visit(FactDesignator factDes) {
        factDes.struct = factDes.getDesignator().obj.getType();
        if (factDes.getOptActPartsOpt() instanceof NoOptActParts) return;

        if (factDes.getDesignator().obj.getKind() != Obj.Meth) {
            report_error("Greska " + factDes.getDesignator(), factDes);
            return;
        }



    }

    public void visit(DesignArrayName desigArrayName) {
        String name = desigArrayName.getDesignatorName();
        desigArrayName.obj = SymbolTablePomClass.find(name);
    }

    public void visit(DesignatorArray desigArray) {
        desigArray.obj = SymbolTablePomClass.noObj;

        Obj designator = desigArray.getDesignatorArrayName().obj;

        if (designator == SymbolTablePomClass.noObj) {
            report_error("Simbol nije definisan", desigArray);
            desigArray.obj = SymbolTablePomClass.noObj;
            return;
        }

        if (designator.getKind() != Obj.Var) {
            report_error("Greska, element nije varijabla", desigArray);
            desigArray.obj = SymbolTablePomClass.noObj;
            return;
        }

        if (!desigArray.getExpr().struct.assignableTo(SymbolTablePomClass.intType)) {
            report_error("Izraz nije integer", desigArray);
            desigArray.obj = SymbolTablePomClass.noObj;
            return;
        }

        if (designator.getType().getKind() != Struct.Array) {
            report_error("Nije niz", desigArray);
            desigArray.obj = SymbolTablePomClass.noObj;
            return;
        }

        if (currentMethod != SymbolTablePomClass.noObj) {
            if (desigArray.obj.getLevel() > 0 && desigArray.obj.getAdr() < currentMethod.getLevel()) {
                report_info("Parametar upotrebljen!", desigArray);
            }
        }


        desigArray.obj = new Obj(Obj.Elem, designator.getName(), designator.getType().getElemType());
        report_info("DesignatorArray obidjen" , desigArray);
    }

    public void visit(DesignatorVar desigVar) {
        String name = desigVar.getDesignatorName();

        desigVar.obj = SymbolTablePomClass.find(name);

        if (desigVar.obj == SymbolTablePomClass.noObj) {
            report_error("Simbol nije definisan", desigVar);
            desigVar.obj = SymbolTablePomClass.noObj;
        }
        if (currentMethod != SymbolTablePomClass.noObj) {
            if (desigVar.obj.getLevel() > 0 && desigVar.obj.getAdr() < currentMethod.getLevel()) {
                report_info("Parametar upotrebljen", desigVar);
            }
        }
    }

    public void visit(ActParams actuals) {
        actualParams.add(actuals.struct);
        report_info("Broj parametara je: " + actualParams.size(), actuals);
    }

    public void visit(FirstActParams firstParams) {
        firstParams.struct = firstParams.getExpr().struct;

        actualParams.add(firstParams.struct);
        report_info("Broj parametara je: " + actualParams.size(), firstParams);
    }
	
	public void visit(RelativeOperationE equals) {
		equals.struct = new Struct(Code.eq);
	}
	public void visit(RelativeOperationD nequals) {
		nequals.struct = new Struct(Code.ne);
	}
	public void visit(RelativeOperationG gre) {
		gre.struct = new Struct(Code.gt);
	}
	public void visit(RelativeOperationEG gequals) {
		gequals.struct = new Struct(Code.ge);
	}
	public void visit(RelativeOperationL less) {
		less.struct = new Struct(Code.lt);
	}
	public void visit(RelativeOperationEL lessequal) {
		lessequal.struct = new Struct(Code.le);
	}
	

    public boolean passed() {
        return !errorDetected;
    }
}