import java.io.*;

public class Calc {
    int token; int value; int ch;
    private PushbackInputStream input;
    final int NUMBER = 256;

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
            result = !(boolean) expr(); // !<expr> 부분
        }
        else if(token == 't'){
            match('t');
            result = true; // t이면 True를 반환
        }
        else if(token == 'f'){
            match('f');
            result = false; // f면 False를 반환
        }
        else {
            result = bexp(); // 우선 result에 bexp 결과를 대입
            while (token == '&' || token == '|') { // 이후 { & <bexp> | '|' <bexp> } 연산 실행
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
        Object result = aexp(); // 우선 result에 aexp 결과를 대입
        if(token == '='){ // 이후 <relop> 이
            match('=');
            if(token == '='){ // "==" 인 경우
                match('=');
                result = (int)result == aexp();
            }
        }
        else if(token == '!'){
            match('!');
            if(token == '='){ // "!=" 인 경우
                match('=');
                result = (int)result != aexp();
            }
        }
        else if(token == '<'){
            match('<');
            if(token == '='){ // "<=" 인 경우
                match('=');
                result = (int)result <= aexp();
            }
            else{ // "<" 인 경우
                result = (int)result < aexp();
            }
        }
        else if(token == '>'){
            match('>');
            if(token == '='){ // ">=" 인 경우
                match('=');
                result = (int)result >= aexp();
            }
            else{ // ">" 인 경우
                result = (int)result > aexp();
            }
        }
        return result;
    }

    int aexp( ) {
        /* expr -> term { '+' term } */
        int result = term();
        while (token == '+' || token == '-') { // 조건으로 '-'인 경우도 추가해서
            if(token == '+') {
                match('+');
                result += term();
            }
            else if(token == '-'){ //  "- <term>" 부분 구현
                match('-');
                result -= term();
            }
        }
        return result;
    }

    int term( ) {
        /* term -> factor { '*' factor } */
        int result = factor();
        while (token == '*' || token == '/') { // 조건으로 '/'인 경우도 추가해서
            if(token == '*') {
                match('*');
                result *= factor();
            }
            else if(token == '/'){  // "/ <factor>" 부분 구현
                match('/');
                result /= factor();
            }
        }
        return result;
    }

    int factor() {
        /* factor -> '(' expr ')' | number */
        int result = 0;
        boolean isNegative = false; // 앞에 '-'가 붙었는지 여부
        if(token == '-'){
            match('-');
            isNegative = true; // 앞에 '-'가 붙었다면 음수로 확인
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
        return (isNegative ? -result : result); // 음수라면 result에 음수를 붙여서, 아니면 그냥 result를 반환
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