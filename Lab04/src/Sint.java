// Sint.java
// Interpreter for S

import java.util.Iterator;
import java.util.Scanner;

public class Sint {
    static Scanner sc = new Scanner(System.in);
    static State state = new State();

    State Eval(Command c, State state) {
        if (c instanceof Decl) {
            Decls decls = new Decls();
            decls.add((Decl) c);
            return allocate(decls, state);
        }

        if (c instanceof Function) {
            Function f = (Function) c;
            state.push(f.id, new Value(f));
            return state;
        }

        if (c instanceof Stmt) return Eval((Stmt) c, state);

        throw new IllegalArgumentException("no command");
    }

    State Eval(Stmt s, State state) {
        if (s instanceof Empty) return Eval((Empty) s, state);
        if (s instanceof Assignment) return Eval((Assignment) s, state);
        if (s instanceof If) return Eval((If) s, state);
        if (s instanceof While) return Eval((While) s, state);
        if (s instanceof Stmts) return Eval((Stmts) s, state);
        if (s instanceof Let) return Eval((Let) s, state);
        if (s instanceof Read) return Eval((Read) s, state);
        if (s instanceof Print) return Eval((Print) s, state);
        if (s instanceof Call) return Eval((Call) s, state);
        if (s instanceof Return) return Eval((Return) s, state);
        throw new IllegalArgumentException("no statement");
    }

/*
    // call without return value
    State Eval(Call c, State state) {
	//
	// evaluate call without return value
	//
	    return state;
    }

    // value-returning call 
    Value V (Call c, State state) { 
	    Value v = state.get(c.fid);  			// find function
        Function f = v.funValue();
        State s = newFrame(state, c, f);	// create new frame on the stack
        s = Eval(f.stmt, s); 						// interpret the call
	    v = s.peek().val;							// get the return value  v = s.get(new Identifier("return")); 
        s = deleteFrame(s, c, f); 				// delete the frame on the stack
    	return v;
    }

    State Eval(Return r, State state) {
        Value v = V(r.expr, state);
		return state.set(new Identifier("return"), v); 
    }

    State newFrame (State state, Call c, Function f) {
        if (c.args.size() == 0) 
            return state;
	//
	// evaluate arguments
	//

	//
	// activate a new stack frame in the stack 
	//
	
	    state.push(new Identifier("return"), null); // allocate for return value
        return state;
    }

    State deleteFrame (State state, Call c, Function f) {
	    state.pop();  // pop the return value
	//
	// free a stack frame from the stack
	//
	    return state;
    }
*/

    State Eval(Empty s, State state) {
        return state;
    }

    State Eval(Assignment a, State state) {
        Value v = V(a.expr, state);
        if(a.ar != null) { // 만약 Assignment 객체에 들어온 것이 배열이라면
            Value arr = state.get(a.ar.id); // State에서 해당 배열을 가져오고
            arr.arrValue()[V(a.ar.expr, state).intValue()] = v; // 해당 배열(a.ar)의 expr 위치에 해당하는 위치에 v를 대입하고
            return state.set(a.ar.id, arr); // state 갱신
        }
        else {
            return state.set(a.id, v);
        }
    }

    State Eval(Read r, State state) {
        r.id.type = state.get(r.id).type; // state에서 r.id에 해당하는 id를 찾아서 type을 대입
        if (r.id.type == Type.INT) {
            int i = sc.nextInt();
            state.set(r.id, new Value(i));
        }

        if (r.id.type == Type.BOOL) {
            boolean b = sc.nextBoolean();
            state.set(r.id, new Value(b));
        }

        //
        // input string
        //

        return state;
    }

    State Eval(Print p, State state) {
        System.out.println(V(p.expr, state));
        return state;
    }

    State Eval(Stmts ss, State state) {
        for (Stmt s : ss.stmts) {
            state = Eval(s, state);
            if (s instanceof Return) return state;
        }
        return state;
    }

    State Eval(If c, State state) {
        if (V(c.expr, state).boolValue()) return Eval(c.stmt1, state);
        else return Eval(c.stmt2, state);
    }

    State Eval(While l, State state) {
        if (V(l.expr, state).boolValue()) return Eval(l, Eval(l.stmt, state));
        else return state;
    }

    State Eval(Let l, State state) {
        State s = allocate(l.decls, state);
        s = Eval(l.stmts, s);
        return free(l.decls, s);
    }

    State allocate(Decls ds, State state) {
        if (ds != null) {
            Iterator it = ds.iterator(); // ds 순환자
            while (it.hasNext()) {
                Decl d = (Decl) it.next();
                if(d.arraysize != 0){ // 만약 d가 배열이라면
                    Value[] v = new Value[d.arraysize]; // d.arraysize 크기를 갖는 Value 배열 선언
                    state.push(d.id, new Value(v)); // 그 후 state에 배열 push
                }
                else if(d.expr == null) state.push(d.id, new Value(d.type)); // Decl문에서 expr이 null일 때 type을 생성자 인자로 하는 Value 객체 push
                else state.push(d.id, (Value)d.expr); // 아니라면 expr을 push
            }
        }
        // add entries for declared variables on the state

        return state;
    }

    State free(Decls ds, State state) {
        if (ds != null) {
            Iterator it = ds.iterator(); // ds 순환자
            while (it.hasNext()) {
                Decl d = (Decl) it.next();
                int idx = state.lookup(d.id); // state에서 ds에 있는 id값의 인덱스를 찾음
                if (idx != -1) state.remove(idx); // 인덱값이 -1이 아니면(id가 존재하면) 해당 위치 id를 state에서 삭제
            }
        }
        // free the entries for declared variables from the state

        return state;
    }

    Value binaryOperation(Operator op, Value v1, Value v2) {
        check(!v1.undef && !v2.undef, "reference to undef value");
        switch (op.val) {
            case "+":
                return new Value(v1.intValue() + v2.intValue());
            case "-":
                return new Value(v1.intValue() - v2.intValue());
            case "*":
                return new Value(v1.intValue() * v2.intValue());
            case "/":
                return new Value(v1.intValue() / v2.intValue());
            //
            // relational operations 생성
            //
            case "<": // Int와 String에 대한 비교 연산 추가(compareTo, equauls 사용)
                return (v1.type == Type.STRING ? new Value(v1.stringValue().compareTo(v2.stringValue()) < 0) : new Value(v1.intValue() < v2.intValue()));
            case "<=":
                return (v1.type == Type.STRING ? new Value(v1.stringValue().compareTo(v2.stringValue()) <= 0) : new Value(v1.intValue() <= v2.intValue()));
            case ">":
                return (v1.type == Type.STRING ? new Value(v1.stringValue().compareTo(v2.stringValue()) > 0) : new Value(v1.intValue() > v2.intValue()));
            case ">=":
                return (v1.type == Type.STRING ? new Value(v1.stringValue().compareTo(v2.stringValue()) >= 0) : new Value(v1.intValue() >= v2.intValue()));
            case "==":
                return (v1.type == Type.STRING ? new Value(v1.stringValue().equals(v2.stringValue())) : new Value(v1.intValue() == v2.intValue()));
            case "!=":
                return (v1.type == Type.STRING ? new Value(!v1.stringValue().equals(v2.stringValue())) : new Value(v1.intValue() != v2.intValue()));
            //
            // logical operations 생성
            //
            case "&": // 논리 연산 추가
                return new Value(v1.boolValue() & v2.boolValue());
            case "|":
                return new Value(v1.boolValue() | v2.boolValue());

            default:
                throw new IllegalArgumentException("no operation");
        }
    }

    Value unaryOperation(Operator op, Value v) {
        check(!v.undef, "reference to undef value");
        switch (op.val) {
            case "!":
                return new Value(!v.boolValue());
            case "-":
                return new Value(-v.intValue());
            default:
                throw new IllegalArgumentException("no operation: " + op.val);
        }
    }

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    Value V(Expr e, State state) {
        if (e instanceof Value) return (Value) e;

        if (e instanceof Identifier) {
            Identifier v = (Identifier) e;
            return (Value) (state.get(v));
        }

        if(e instanceof Array){ // e가 Array라면
            Array a = (Array) e; // 타입 캐스팅 한 후
            Value v = state.get(a.id); // 해당 id의 배열을 state에서 가져온 다음
            return v.arrValue()[V(a.expr, state).intValue()]; // 배열에서 a.expr에 해당하는 값의 위치에 있는 값을 반환
        }

        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Value v1 = V(b.expr1, state);
            Value v2 = V(b.expr2, state);
            return binaryOperation(b.op, v1, v2);
        }

        if (e instanceof Unary) {
            Unary u = (Unary) e;
            Value v = V(u.expr, state);
            return unaryOperation(u.op, v);
        }

        if (e instanceof Call) return V((Call) e, state);
        throw new IllegalArgumentException("no operation");
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            Sint sint = new Sint();
            Lexer.interactive = true;
            System.out.println("Language S Interpreter 2.0");
            System.out.print(">> ");
            Parser parser = new Parser(new Lexer());

            do { // Program = Command*
                if (parser.token == Token.EOF) parser.token = parser.lexer.getToken();

                Command command = null;
                try {
                    command = parser.command();
                    if (command != null) command.display(0);    // display AST
                    else if (command == null) throw new Exception();
                    else {
                        command.type = TypeChecker.Check(command);
                        System.out.println("\nType: " + command.type);
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    System.out.print(">> ");
                    continue;
                }

                if (command.type != Type.ERROR) {
                    System.out.println("\nInterpreting...");
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
                System.out.print(">> ");
            } while (true);
        } else {
            System.out.println("Begin parsing... " + args[0]);
            Command command = null;
            Parser parser = new Parser(new Lexer(args[0]));
            Sint sint = new Sint();

            do {    // Program = Command*
                if (parser.token == Token.EOF) break;

                try {
                    command = parser.command();
                    if (command != null) command.display(0);    // display AST
                    else if (command == null) throw new Exception();
                    else {
                        command.type = TypeChecker.Check(command);
                        System.out.println("\nType: " + command.type);
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    continue;
                }

                if (command.type != Type.ERROR) {
                    System.out.println("\nInterpreting..." + args[0]);
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
            } while (command != null);
        }
    }
}