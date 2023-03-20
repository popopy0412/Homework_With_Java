import java.io.*;
import java.util.Collections;

public class Calc {
    int token; int value; int ch;
    private PushbackInputStream input;
    final int NUMBER=256;

    Calc(PushbackInputStream is) {
        input = is;
    }

    int getToken( )  { /* tokens are characters */
        while(true) {
            try  {
                ch = input.read();
                if (ch == ' ' || ch == '\t' || ch == '\r') ;
                else if (Character.isDigit(ch)) {
                    value = number();
                    input.unread(ch);
                    return NUMBER;
                }
                else return ch;
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private int number( )  {
        /* number -> digit { digit } */
        int result = ch - '0';
        try  {
            ch = input.read();
            while (Character.isDigit(ch)) {
                result = 10 * result + ch -'0';
                ch = input.read();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return result;
    }

    void error( ) {
        System.out.printf("parse error : %d\n", ch);
        //System.exit(1);
    }

    void match(int c) {
        if (token == c)
            token = getToken();
        else error();
    }

    void command( ) {
        /* command -> expr '\n' */
        Object result = expr();
        if (token == '\n') /* end the parse and print the result */
            System.out.println(result);
        else error();
    }

    Object expr() {
        Object result;
        if(token == '!'){
            match('!');
            result = !(boolean) expr();
        }
        else if(token == 't'){
            match('t');
            result = true;
        }
        else if(token == 'f'){
            match('f');
            result = false;
        }
        else {
            result = bexp();
            while (token == '&' || token == '|') {
                if (token == '&') {
                    match('&');
                    result = (boolean) result & (boolean) bexp();
                } else if (token == '|') {
                    match('|');
                    result = (boolean) result | (boolean) bexp();
                }
            }
        }
        return result;
    }

    Object bexp( ) {
        Object result = aexp();
        if(token == '='){
            match('=');
            if(token == '='){
                match('=');
                result = (int)result == aexp();
            }
        }
        else if(token == '!'){
            match('!');
            if(token == '='){
                match('=');
                result = (int)result != aexp();
            }
        }
        else if(token == '<'){
            match('<');
            if(token == '='){
                match('=');
                result = (int)result <= aexp();
            }
            else{
                result = (int)result < aexp();
            }
        }
        else if(token == '>'){
            match('>');
            if(token == '='){
                match('=');
                result = (int)result >= aexp();
            }
            else{
                result = (int)result > aexp();
            }
        }
        return result;
    }

    int aexp( ) {
        /* expr -> term { '+' term } */
        int result = term();
        while (token == '+' || token == '-') {
            if(token == '+') {
                match('+');
                result += term();
            }
            else if(token == '-'){
                match('-');
                result -= term();
            }
        }
        return result;
    }

    int term( ) {
        /* term -> factor { '*' factor } */
        int result = factor();
        while (token == '*' || token == '/') {
            if(token == '*') {
                match('*');
                result *= factor();
            }
            else if(token == '/'){
                match('/');
                result /= factor();
            }
        }
        return result;
    }

    int factor() {
        /* factor -> '(' expr ')' | number */
        int result = 0;
        boolean isNegative = false;
        if(token == '-'){
            match('-');
            isNegative = true;
        }
        if (token == '(') {
            match('(');
            result = aexp();
            match(')');
        }
        else if (token == NUMBER) {
            result = value;
            match(NUMBER); //token = getToken();
        }
        return (isNegative ? -result : result);
    }

    void parse( ) {
        token = getToken(); // get the first token
        command();          // call the parsing command
    }

    public static void main(String args[]) {
        Calc calc = new Calc(new PushbackInputStream(System.in));
        while(true) {
            System.out.print(">> ");
            calc.parse();
        }
    }
}