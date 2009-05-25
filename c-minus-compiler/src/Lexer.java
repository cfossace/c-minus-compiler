
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * the main Class - Lexer
 */

/**
 *trytry
 * @author nitsan
 */
public class Lexer
{
    private ArrayList<ArrayList<Token>> lines;
    private HashMap<String,Integer> tokens_val;
    private ArrayList<String> line;

    public Lexer()
    {
        this.lines = new ArrayList<ArrayList<Token>>();
        this.tokens_val = new HashMap<String, Integer>();
        this.line = new ArrayList<String>();
    }

    public void init_tokens()
    {
        tokens_val.put("main", 1000);
        tokens_val.put("{", 1001);
        tokens_val.put("int", 1002);
        tokens_val.put("struct", 1003);
        tokens_val.put("}", 1004);
        tokens_val.put("cin", 1005);
        tokens_val.put(">>", 1006);
        tokens_val.put("cout", 1007);
        tokens_val.put("<<", 1008);
        tokens_val.put("*", 1);
        tokens_val.put("/", 3);
        //tokens_val.put("&", 5);
        tokens_val.put("+", 6);
        tokens_val.put("-", 7);
        //tokens_val.put("|", 8);
        tokens_val.put("=", 9);
        tokens_val.put(".", 18);
        tokens_val.put(",", 19);
        //tokens_val.put(":", 20);
        tokens_val.put(")",22);
        tokens_val.put("]", 23);
        tokens_val.put("(", 29);
        tokens_val.put("[", 30);
        tokens_val.put(";", 38);
        tokens_val.put("const", 57);
        tokens_val.put("ident", 37);
        tokens_val.put("number", 34);
    }

    public static void main(String [] args)
    {
        Lexer lex = new Lexer();
        lex.init_tokens(); //create hash map with all the knows tokens
        lex.get(); //get the tokens from the file
        lex.print(); //print the tokens to the screen
    }

    public void get()
    {
        File file = new File("program.txt");
        BufferedReader reader = null;
        FileReader fr = null;
        try
        {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String inline;
            while((inline = reader.readLine()) != null)
            {
                inline = inline.trim(); //remove spaces
                this.line.add(inline);  //add the complete line
                this.tokenize(inline.toCharArray()); //get the tokens from the line
               // reader.readLine(); //read the empty line******* this is not supposed to be here
            }
        }
        catch(Exception e) {System.out.println(e.getMessage());}
    }

    public void tokenize(char [] char_array)
    {
        char c;
        int i=0;
        ArrayList<Token> list = new ArrayList<Token>();
        while(i<char_array.length)
        {
            c = char_array[i];
            StringBuilder str = new StringBuilder();
            if (c==' ')
                ++i;
            else if (Character.isDigit(c)||Character.isLetter(c) )//|| (c=='-' &&i+1<char_array.length&& Character.isDigit(char_array[i+1])))
            {
//                if (c=='-')
//                {
//                    str.append(char_array[i]);
//                    ++i;
//                }
                while (i<char_array.length && (Character.isDigit(char_array[i])||Character.isLetter(char_array[i])))
                {
                    str.append(char_array[i]);
                    ++i;
                }
            }
            else
            {
                while (i<char_array.length &&(!Character.isDigit(char_array[i])&& !Character.isLetter(char_array[i])))
                {
                    str.append(char_array[i]);
                    ++i;
                    if (tokens_val.containsKey(str.toString()))
                        break;

                }
            }

            //check and add the tokens with the appropriate values
            if (str.length()>0)
            {
                if (tokens_val.containsKey(str.toString())) //for the known tokens
                        list.add(new Token(tokens_val.get(str.toString()), str.toString()));
                else if (Character.isDigit(str.toString().charAt(0))) //for the numbers
                    list.add(new Token(tokens_val.get("number"), str.toString()));
                else if (Character.isLetter(str.toString().charAt(0))) //for the variables
                    list.add(new Token(tokens_val.get("ident"), str.toString()));
                //else if (str.length()>1&&Character.isDigit(str.toString().charAt(1))&&str.toString().charAt(0)=='-') //for numbers with minus
                   // list.add(new Token(tokens_val.get("number"), str.toString()));
                else //for all other unknown vars
                {
                    System.out.println("error ! unknown string");
                    System.exit(1);
                }
            }
        }
        lines.add(list);
    }

    public void print()
    {
        for (int i=0;i<line.size();++i)
        {
            System.out.println(line.get(i));
            for (int j=0;j<lines.get(i).size();++j)
            {
                System.out.print(lines.get(i).get(j).getToken()+'\t');
            }
            System.out.print('\n');
            for (int j=0;j<lines.get(i).size();++j)
            {
                System.out.print(Integer.toString(lines.get(i).get(j).getToken_num())+'\t');
            }
            System.out.print('\n');

        }
    }

    //the private class that holds the token and its symbol number
    public class Token
    {

        String token;
        int token_num;

        public Token(int num, String tok)
        {
            this.token = tok;
            this.token_num = num;
        }

        public String getToken()
        {
            return token;
        }

        public void setToken(String token)
        {
            this.token = token;
        }

        public int getToken_num()
        {
            return token_num;
        }

        public void setToken_num(int token_num)
        {
            this.token_num = token_num;
        }
    }

    public ArrayList<ArrayList<Token>> getLines()
    {
    	return this.lines;
    }
}
