package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;

import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.ac.bg.etf.pp1.LabelaPom;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import rs.ac.bg.etf.pp1.ast.*;

public class CodeGenerator extends VisitorAdaptor {

	private int mainPc;
	private Logger log = Logger.getLogger(getClass());
	private boolean array = false;
	public int getMainPc() {
		return mainPc;
	}
	
	public void report_error(String message, SyntaxNode info) {
		//errorDetected = true;
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
	
	public CodeGenerator() {
		SymbolTablePomClass.init();
	}
	
	public void visit(ProgName progName) {
		//setOrd();
		//setChr();
		//setLen();
	}
	
	public void visit(Program program) {
		super.visit(program);
	}
	
	
	public void visit(PrintSmth printStmt) {
		
		if(printStmt.getExpr().struct == SymbolTablePomClass.intType || printStmt.getExpr().struct == SymbolTablePomClass.boolType) {
			Code.loadConst(4);
			Code.put(Code.print);
		} else {
			Code.loadConst(1);
			Code.put(Code.bprint);
		}
	}
	
	public void visit(PrintTwo printTwoTimes) {
		Code.loadConst(printTwoTimes.getNumb());
	
		if(printTwoTimes.getExpr().struct == SymbolTablePomClass.charType) {
			Code.put(Code.bprint);
		} else Code.put(Code.print);
	}
	
	public void visit(ReturnStatement returnStmt) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	
	public void visit(MethodTypeName methodName) {
		if(methodName.obj.getName().equals("main")) {
			mainPc = Code.pc;
		}
		
		methodName.obj.setAdr(Code.pc);
		

		VarCounter varCnt = new VarCounter();
		methodName.getParent().traverseTopDown(varCnt);
		FormParamCounter fpCnt = new FormParamCounter();
		methodName.getParent().traverseTopDown(fpCnt);
		

		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put(fpCnt.getCount() + varCnt.getCount());
	}
	
	public void visit(MethodDeclarations methodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	private int constVal(SyntaxNode tipConst) {
		if(tipConst instanceof ConstN) return ((ConstN)tipConst).getNumVal();
		if(tipConst instanceof ConstCh) return ((ConstCh)tipConst).getCharVal();
		if(tipConst instanceof ConstB) {
		
			if(((ConstB)tipConst).getBoolVal()){
				return 1;
			} else return 0;
		}
		return -1;
	}
	
	public void visit(ReadStatement readStmt) {
		if(readStmt.getDesignator().obj.getType() == SymbolTablePomClass.charType ) {
		Code.put(Code.bread);
		} else {
			Code.put(Code.read);
		}
		Code.store(readStmt.getDesignator().obj);
	}
	
	public void visit(DesignatorAssignOp desigAssign) {
		Designator desig = desigAssign.getDesignator();
		
		report_info("designator " + desig.obj.getName(), desig);
		if(desig instanceof DesignatorVar) {
			Obj desigObj = desig.obj;
			Code.store(desigObj);
		} else if(desig instanceof DesignatorArray) {
			Obj arrayDesig = desig.obj;
			if(arrayDesig.getType() == SymbolTablePomClass.charType) {
				Code.put(Code.bastore);
			} else {
				Code.put(Code.astore);
			}
		} 
	}
	
	public void visit(DesignatorActPars desiAP) {
			
		int off = desiAP.obj.getAdr() - Code.pc;
		
		Code.put(Code.call);
		Code.put2(off);
		if(desiAP.obj.getType() != SymbolTablePomClass.noType) {
			Code.put(Code.pop);
		}
	}

	
	public void visit(DesignatorIncrement incDesig) {
		Designator desi = incDesig.getDesignator();
		array = true;
		
		if(desi instanceof DesignatorVar) {
			Code.load(desi.obj);
		} else if(desi instanceof DesignatorArray) {
			if(array) {
				Code.put(Code.dup2);
			}
			if(desi.obj.getType() == SymbolTablePomClass.intType) {
				Code.put(Code.aload);
			} else {
				Code.put(Code.baload);
			}
		}
		array = false;
		Code.loadConst(1);
		Code.put(Code.add);
		
		if(desi instanceof DesignatorArray) {
			if(desi.obj.getType() == SymbolTablePomClass.charType) {
				Code.put(Code.bastore);
			}else {
				Code.put(Code.astore);
			}
		} else {
			if(desi instanceof DesignatorVar) {
				Code.store(desi.obj);
			}
		}
	}

	
	public void visit(DesignatorDecrement decDesig) {
		Designator desi = decDesig.getDesignator();
		array = true;
		
		if(desi instanceof DesignatorVar) {
			Code.load(desi.obj);
		} else if(desi instanceof DesignatorArray) {
			if(array) {
				Code.put(Code.dup2);
			}
			if(desi.obj.getType() == SymbolTablePomClass.intType) {
				Code.put(Code.aload);
			} else {
				Code.put(Code.baload);
			}
		}
		array = false;
		Code.loadConst(-1);
		Code.put(Code.add);
		
		if(desi instanceof DesignatorArray) {
			if(desi.obj.getType() == SymbolTablePomClass.charType) {
				Code.put(Code.bastore);
			}else {
				Code.put(Code.astore);
			}
		} else {
			if(desi instanceof DesignatorVar) {
				Code.store(desi.obj);
			}
		}
	}
	
	
	public void visit(FactDesignator factDes) {
		if(factDes.getOptActPartsOpt() instanceof OActPO) {
			int off = factDes.getDesignator().obj.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(off);
		} else {
			Code.load(factDes.getDesignator().obj);
		}
		
	}
	
	public void visit(ConstN numConst) {
		Obj cnst = SymbolTablePomClass.insert(Obj.Con, "$", numConst.struct);
		cnst.setLevel(0);
		cnst.setAdr(constVal(numConst));
		
		Code.load(cnst);
	}
	
	public void visit(ConstCh charConst) {
		Obj cnst = SymbolTablePomClass.insert(Obj.Con, "$", charConst.struct);
		cnst.setLevel(0);
		cnst.setAdr(constVal(charConst));
		Code.load(cnst);
	}
	
	public void visit(ConstB boolConst) {
		Obj cnst = SymbolTablePomClass.insert(Obj.Con, "$", boolConst.struct);
		cnst.setLevel(0);
		cnst.setAdr(constVal(boolConst));
		Code.load(cnst);
	}
	
	public void visit(FactNumber factNum) {
		int val = factNum.getValueN();
		 Obj tmp = new Obj(Obj.Con, "", SymbolTablePomClass.intType, val, 0);
		Code.load(tmp);
	}
	
	/*public void visit(FactBoolean factBool) {
		int val = factBool.getValueB();
		 Obj tmp = new Obj(Obj.Con, "", SymbolTablePomClass.boolType, val, 0);
		Code.load(tmp);
	}
	*/
	public void visit(FactCharacter factChar) {
        int value = factChar.getValueC();
        Obj tmp = new Obj(Obj.Con, "", SymbolTablePomClass.charType, value, 0);
        Code.load(tmp);
	}
	
	public void visit(FactNewExpr newExpr) {
		Code.put(Code.newarray);
		
		if(newExpr.getType().struct == SymbolTablePomClass.intType) {
			Code.loadConst(1);
		} else {
			Code.loadConst(0);
		}
	}
	//Expression and terms
	public void visit(Expression expr) {
		if(expr.getMinusMayExist() instanceof MinusExists) {
			Code.loadConst(-1);
			Code.put(Code.mul);
		}
	}
	
	public void visit(Terminal termMul) {
		SyntaxNode tipTerm = termMul.getMulOp();
		
		if(tipTerm instanceof MultiplyOperationM) Code.put(Code.mul);
		if(tipTerm instanceof MultiplyOperationD) Code.put(Code.div);
		if(tipTerm instanceof MultiplyOperationMod) Code.put(Code.rem);
	}
	
	public void visit(TerminalList termAdd) {
		SyntaxNode tipTerm = termAdd.getAddOp();
		
		if(tipTerm instanceof AddOperationP) Code.put(Code.add);
		if(tipTerm instanceof AddOperationM) Code.put(Code.sub);
	}
	
	public void visit(DesignArrayName desigArray) {
		Code.load(desigArray.obj);
	}
    
    private List<LabelaPom> labels = new LinkedList<LabelaPom>();
    
	public void visit(GoToStatement gotoStmt) {
		boolean postojiLabela = false;
		
		for(LabelaPom l:labels) {
			if(l.getNazivLabele().equals(gotoStmt.getLabelName())) {
				postojiLabela = true;
				break;
			}
		}
		
		if(postojiLabela) {
			
			for(LabelaPom l:labels) {
				if(l.getNazivLabele().equals(gotoStmt.getLabelName())) {
					Code.putJump(l.getAdresaLabele());
					break;
				}
			}
		} else {
			Code.putJump(0);
			LabelaPom l = new LabelaPom(gotoStmt.getLabelName(), -1);
			labels.add(l);
			for(LabelaPom lab: labels) {
				if(lab.getNazivLabele().equals(l.getNazivLabele())) {
					lab.gotoListaLabela.add(Code.pc-2);
					break;
				}
			}
		}
	} 
	
	public void visit(Label label) {
		LabelaPom l = new LabelaPom(label.getLabel(),Code.pc);
		labels.add(l);
		for(LabelaPom lab : labels ) {
			if(lab.getNazivLabele().equals(label.getLabel())) {
				while(!lab.gotoListaLabela.isEmpty()) {
				Code.fixup(lab.gotoListaLabela.remove(lab.gotoListaLabela.size() - 1));
				}
			}
		}
		
	}

}
