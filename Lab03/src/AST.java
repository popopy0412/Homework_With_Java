// AST.java
// AST for S
import java.util.*;


class Indent{ // 들여쓰기 해주는 클래스
    public static void display(int level, String s){ // 들여쓰기 후 출력하는 display() 메소드 구현
        String tab = "";
        for(int i=0;i<level;i++) tab = tab + "    ";
        System.out.println(tab + s);
    }
}
abstract class Command {
    // Command = Decl | Function | Stmt
    Type type =Type.UNDEF;
    public void display(int level){} // display() 메소드 선언
}

class Decls extends ArrayList<Decl> {
    // Decls = Decl*

    Decls() { super(); }
    Decls(Decl d) {
	    this.add(d);
    }

    public void display(int level){ // Decl 여러 개를 위한 display() 구현
        Indent.display(level, "Decls");
        Iterator<Decl> it = this.iterator();
        while(it.hasNext()){
            Decl d = it.next();
            d.display(level+1);
        }
    }
}

class Decl extends Command {
    // Decl = Type type; Identifier id 
    Identifier id;
    Expr expr = null;
    int arraysize = 0;

    Decl (String s, Type t) {
        id = new Identifier(s); type = t;
    } // declaration 

    Decl (String s, Type t, int n) {
        id = new Identifier(s); type = t; arraysize = n;
    } // array declaration 

    Decl (String s, Type t, Expr e) {
        id = new Identifier(s); type = t; expr = e;
    } // declaration

    @Override
    public void display(int level) { // Decl을 출력하기 위한 display() 구현
        Indent.display(level, "Decl");
        type.display(level+1);
        id.display(level+1);
        if(expr != null) expr.display(level+1);
    }
}

class Functions extends ArrayList<Function> {
    // Functions = Function*
    public void display(int level){ // Function 여러 개를 출력하기 위한 display() 구현
        Indent.display(level, "Functions");
        Iterator<Function> it = this.iterator();
        while(it.hasNext()){
            Function f = it.next();
            f.display(level+1);
        }
    }
}

class Function extends Command  {
    // Function = Type type; Identifier id; Decls params; Stmt stmt
    Identifier id;
    Decls params;
    Stmt stmt;

    Function(String s, Type t) { 
        id = new Identifier(s); type = t; params = null; stmt = null;
    }

    public String toString ( ) { 
       return id.toString()+params.toString(); 
    }

    @Override
    public void display(int level) { // Function을 출력하기 위한 display() 구현
        Indent.display(level, "Function");
        type.display(level+1);
        id.display(level+1);
        params.display(level+1);
        stmt.display(level+1);
    }
}

class Type {
    // Type = int | bool | string | fun | array | except | void
    final static Type INT = new Type("int");
    final static Type BOOL = new Type("bool");
    final static Type STRING = new Type("string");
    final static Type VOID = new Type("void");
    final static Type FUN = new Type("fun");
    final static Type ARRAY = new Type("array");
    final static Type EXC = new Type("exc");
    final static Type RAISEDEXC = new Type("raisedexc");
    final static Type UNDEF = new Type("undef");
    final static Type ERROR = new Type("error");
    
    protected String id;
    protected Type(String s) { id = s; }
    public String toString ( ) { return id; }

    public void display(int level){
        Indent.display(level, "Type: "+id);
    }
}

class ProtoType extends Type {
   // defines the type of a function and its parameters
   Type result;  
   Decls params;
   ProtoType (Type t, Decls ds) {
      super(t.id);
      result = t;
      params = ds;
   }

}

abstract class Stmt extends Command {
    // Stmt = Empty | Stmts | Assignment | If  | While | Let | Read | Print
}

class Empty extends Stmt {

}

class Stmts extends Stmt {
    // Stmts = Stmt*
    public ArrayList<Stmt> stmts = new ArrayList<Stmt>();
    
    Stmts() {
	    super(); 
    }

    Stmts(Stmt s) {
	     stmts.add(s);
    }

    @Override
    public void display(int level) { // Stmt 여러 개를 출력하기 위한 display() 구현
        Indent.display(level, "Stmts");
        Iterator<Stmt> it = stmts.iterator();
        while(it.hasNext()){
            Stmt s = it.next();
            s.display(level+1);
        }
    }
}

class Assignment extends Stmt {
    // Assignment = Identifier id; Expr expr
    Identifier id;
    Array ar = null;
    Expr expr;

    Assignment (Identifier t, Expr e) {
        id = t;
        expr = e;
    }

    Assignment (Array a, Expr e) {
        ar = a;
        expr = e;
    }

    @Override
    public void display(int level) { // Assignment를 출력하기 위한 display() 구현
        Indent.display(level, "Assignment");
        id.display(level+1);
        expr.display(level+1);
    }
}

class If extends Stmt {
    // If = Expr expr; Stmt stmt1, stmt2;
    Expr expr;
    Stmt stmt1, stmt2;
    
    If (Expr t, Stmt tp) {
        expr = t; stmt1 = tp; stmt2 = new Empty( );
    }
    
    If (Expr t, Stmt tp, Stmt ep) {
        expr = t; stmt1 = tp; stmt2 = ep; 
    }

    @Override
    public void display(int level) { // If를 출력하기 위한 display() 구현
        Indent.display(level, "If");
        expr.display(level+1);
        stmt1.display(level+1);
        if(!(stmt2 instanceof Empty)) stmt2.display(level+1);
    }
}

class While extends Stmt {
    // While = Expr expr; Stmt stmt;
    Expr expr;
    Stmt stmt;

    While (Expr t, Stmt b) {
        expr = t; stmt = b;
    }

    @Override
    public void display(int level) { // While을 출력하기 위한 display() 구현
        Indent.display(level, "While");
        expr.display(level+1);
        stmt.display(level+1);
    }
}

class Let extends Stmt {
    // Let = Decls decls; Functions funs; Stmts stmts;
    Decls decls;
    Functions funs;
    Stmts stmts;

    Let(Decls ds, Stmts ss) {
        decls = ds;
		funs = null;
        stmts = ss;
    }

    Let(Decls ds, Functions fs, Stmts ss) {
        decls = ds;
	    funs = fs;
        stmts = ss;
    }

    @Override
    public void display(int level) { // Let을 출력하기 위한 display() 구현
        Indent.display(level, "Let");
        decls.display(level+1);
        if(funs != null) funs.display(level+1);
        stmts.display(level+1);
    }
}

class Read extends Stmt {
    // Read = Identifier id
    Identifier id;

    Read (Identifier v) {
        id = v;
    }

    @Override
    public void display(int level) { // Let을 출력하기 위한 display() 구현
        Indent.display(level, "Read");
        id.display(level+1);
    }
}

class Print extends Stmt {
    // Print =  Expr expr
    Expr expr;

    Print (Expr e) {
        expr = e;
    }

    @Override
    public void display(int level) { // Print를 출력하기 위한 display() 구현
        Indent.display(level, "Print");
        expr.display(level+1);
    }
}

class Return extends Stmt {
    Identifier fid;
    Expr expr;

    Return (String s, Expr e) {
        fid = new Identifier(s);
        expr = e;
    }

    @Override
    public void display(int level) { // Return을 출력하기 위한 display() 구현
        Indent.display(level, "Return");
        fid.display(level+1);
        expr.display(level+1);
    }
}

class Try extends Stmt {
    // Try = Identifier id; Stmt stmt1; Stmt stmt2; 
    Identifier eid;
    Stmt stmt1; 
    Stmt stmt2; 

    Try(Identifier id, Stmt s1, Stmt s2) {
        eid = id; 
        stmt1 = s1;
        stmt2 = s2;
    }

    @Override
    public void display(int level) { // Try를 출력하기 위한 display() 구현
        Indent.display(level, "Try");
        eid.display(level+1);
        stmt1.display(level+1);
        stmt2.display(level+1);
    }
}

class Raise extends Stmt {
    Identifier eid;

    Raise(Identifier id) {
        eid = id;
    }

    @Override
    public void display(int level) { // Raise를 출력하기 위한 display() 구현
        Indent.display(level, "Raise");
        eid.display(level+1);
    }
}

class Exprs extends ArrayList<Expr> {
    // Exprs = Expr*
    public void display(int level) { // Expr 여러 개를 출력하기 위한 display() 구현
        Iterator<Expr> it = this.iterator();
        while (it.hasNext()) {
            Expr e = it.next();
            e.display(level + 1);
        }
    }
}

abstract class Expr extends Stmt {
    // Expr = Identifier | Value | Binary | Unary | Call
}

class Call extends Expr { 
    Identifier fid;  
    Exprs args;

    Call(Identifier id, Exprs a) {
       fid = id;
       args = a;
    }

    @Override
    public void display(int level) { // Call을 출력하기 위한 display() 구현
        Indent.display(level, "Call");
        fid.display(level+1);
        args.display(level+1);
    }
}

class Identifier extends Expr {
    // Identifier = String id
    private String id;

    Identifier(String s) { id = s; }

    public String toString( ) { return id; }
    
    public boolean equals (Object obj) {
        String s = ((Identifier) obj).id;
        return id.equals(s);
    }

    @Override
    public void display(int level) { // Identifier를 출력하기 위한 display() 구현
        Indent.display(level, "Identifier: "+id);
    } 
}

class Array extends Expr {
    // Array = Identifier id; Expr expr
    Identifier id;
    Expr expr = null;

    Array(Identifier s, Expr e) {id = s; expr = e;}

    public String toString( ) { return id.toString(); }
    
    public boolean equals (Object obj) {
        String s = ((Array) obj).id.toString();
        return id.equals(s);
    }

    @Override
    public void display(int level) {  // Array를 출력하기 위한 display() 구현
        Indent.display(level, "Array");
        id.display(level+1);
        expr.display(level+1);
    }
}

class Value extends Expr {
    // Value = int | bool | string | array | function 
    protected boolean undef = true;
    Object value = null; // Type type;
    
    Value(Type t) {
        type = t;
        if (type == Type.INT) value = new Integer(0);
        if (type == Type.BOOL) value = new Boolean(false);
        if (type == Type.STRING) value = "";
        undef = false;
    }

    Value(Object v) {
        if (v instanceof Integer) type = Type.INT;
        if (v instanceof Boolean) type = Type.BOOL;
        if (v instanceof String) type = Type.STRING;
        if (v instanceof Function) type = Type.FUN;
        if (v instanceof Value[]) type = Type.ARRAY;
        value = v; undef = false; 
    }

    Object value() { return value; }

    int intValue( ) { 
        if (value instanceof Integer) 
            return ((Integer) value).intValue(); 
        else return 0;
    }
    
    boolean boolValue( ) { 
        if (value instanceof Boolean) 
            return ((Boolean) value).booleanValue(); 
        else return false;
    } 

    String stringValue ( ) {
        if (value instanceof String) 
            return (String) value; 
        else return "";
    }

    Function funValue ( ) {
        if (value instanceof Function) 
            return (Function) value; 
        else return null;
    }

    Value[] arrValue ( ) {
        if (value instanceof Value[]) 
            return (Value[]) value; 
        else return null;
    }

    Type type ( ) { return type; }

    public String toString( ) {
        //if (undef) return "undef";
        if (type == Type.INT) return "" + intValue(); 
        if (type == Type.BOOL) return "" + boolValue();
	    if (type == Type.STRING) return "" + stringValue();
        if (type == Type.FUN) return "" + funValue();
        if (type == Type.ARRAY) return "" + arrValue();
        return "undef";
    }

    @Override
    public void display(int level) { // Value를 출력하기 위한 display() 구현
        Indent.display(level, "Value: "+value);
    }
}

class Binary extends Expr {
// Binary = Operator op; Expr expr1; Expr expr2;
    Operator op;
    Expr expr1, expr2;

    Binary (Operator o, Expr e1, Expr e2) {
        op = o; expr1 = e1; expr2 = e2;
    } // binary

    @Override
    public void display(int level) { // Binary를 출력하기 위한 display() 구현
        Indent.display(level, "Binary");
        op.display(level+1);
        expr1.display(level+1);
        expr2.display(level+1);
    }
}

class Unary extends Expr {
    // Unary = Operator op; Expr expr
    Operator op;
    Expr expr;

    Unary (Operator o, Expr e) {
        op = o; //(o.val == "-") ? new Operator("neg"): o; 
        expr = e;
    } // unary

    @Override
    public void display(int level) { // Unary를 출력하기 위한 display() 구현
        Indent.display(level, "Unary");
        op.display(level+1);
        expr.display(level+1);
    }
}

class Dowhile extends Stmt{
    Stmt stmt;
    While wh;

    Dowhile(Stmt s, While w){
        stmt = s;
        wh = w;
    }

    @Override
    public void display(int level) {
        stmt.display(level);
        wh.display(level);
    }
}

class Operator{
    String val;
    
    Operator (String s) { 
	val = s; 
    }

    public String toString( ) { 
	return val; 
    }

    public boolean equals(Object obj) { 
	return val.equals(obj); 
    }

    public void display(int level){ // Operator를 출력하기 위한 display() 구현
        Indent.display(level, "Operator: "+val);
    }
}