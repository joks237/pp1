
package rs.ac.bg.etf.pp1;
import java_cup.runtime.Symbol;

%%

%{

	//ukljucivanje informacije o poziciji tokena na osnovu tipa
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	
	//ukljucivanje informacije o poziciji tokena na osnovu tipa i vrednosti
	private Symbol new_symbol(int type, Object value){
		return new Symbol(type, yyline+1, yycolumn, value);
	}

%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" "    { }
"\b"   { }
"\t"   { }
"\r\n" { }
"\f"   { }

"program" { return new_symbol(sym.PROG, yytext());}
"print"   { return new_symbol(sym.PRINT, yytext());}
"return"  { return new_symbol(sym.RETURN, yytext());}
"void"    { return new_symbol(sym.VOID, yytext());}
"break"   { return new_symbol(sym.BREAK, yytext());}
"else"    { return new_symbol(sym.ELSE, yytext());}
"const"   { return new_symbol(sym.CONST, yytext());}
"if"      { return new_symbol(sym.IF, yytext());}
"do"   { return new_symbol(sym.DO, yytext());}
"while"   { return new_symbol(sym.WHILE, yytext());}
"new"   { return new_symbol(sym.NEW, yytext());}
"read"   { return new_symbol(sym.READ, yytext());}
"continue"   { return new_symbol(sym.CONTINUE, yytext());}
"goto" { return new_symbol(sym.GOTO, yytext());}
"+"       { return new_symbol(sym.PLUS, yytext());}
"-"   { return new_symbol(sym.MINUS, yytext());}
"*"   { return new_symbol(sym.MUL, yytext());}
"/"   { return new_symbol(sym.DIV, yytext());}
"%"   { return new_symbol(sym.MOD, yytext());}

"=="   { return new_symbol(sym.EQUALSTO, yytext());}
"!="   { return new_symbol(sym.DIFFERENT, yytext());}
">"   { return new_symbol(sym.GREATER, yytext());}
">="   { return new_symbol(sym.EGREATER, yytext());}
"<"   { return new_symbol(sym.LESS, yytext());}
"<="   { return new_symbol(sym.ELESS, yytext());}

"&&"   { return new_symbol(sym.AND, yytext());}
"||"   { return new_symbol(sym.OR, yytext());}

"="       { return new_symbol(sym.EQUAL, yytext());}
"++"   { return new_symbol(sym.PLUSPLUS, yytext());}
"--"   { return new_symbol(sym.MINUSMINUS, yytext());}

";"       { return new_symbol(sym.SEMI, yytext());}
","       { return new_symbol(sym.COMMA, yytext());}
"."   { return new_symbol(sym.DOT, yytext());}
"(" 	  { return new_symbol(sym.LPAREN, yytext());}
")" 	  { return new_symbol(sym.RPAREN, yytext());}
"{" 	  { return new_symbol(sym.LBRACE, yytext());}
"}" 	  { return new_symbol(sym.RBRACE, yytext());}
"["   { return new_symbol(sym.LSQBRACE, yytext());}
"]"   { return new_symbol(sym.RSQBRACE, yytext());}
":"   { return new_symbol(sym.DOUBLEDOT, yytext());}

"//" 	  { yybegin(COMMENT); }
<COMMENT> . { yybegin(COMMENT); }
<COMMENT> "\r\n" { yybegin(YYINITIAL); }

"true"			{ return new_symbol(sym.BOOL, new Boolean(yytext())); }
"false"			{ return new_symbol(sym.BOOL, new Boolean(yytext())); }


[0-9]+ { return new_symbol(sym.NUMBER, new Integer(yytext()));}
"'"[\040-\176]"'" {return new_symbol (sym.CHARCONST, new Character (yytext().charAt(1)));}
([a-z]|[A-Z])[a-zA-Z0-9_]* { return new_symbol (sym.IDENT, yytext());}

. { System.err.println("Leksicka greska ("+yytext()+") u liniji" + (yyline+1) + "kolona " + yycolumn); }

