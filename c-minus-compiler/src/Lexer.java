
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
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

    private void init_tokens()
    {
        tokens_val.put("main(){", 1000);
        tokens_val.put("{", 1001);
        tokens_val.put("int", 1002);
        tokens_val.put("struct", 1003);
        tokens_val.put("}", 1004);
        tokens_val.put("cin", 1005);
        tokens_val.put(">>", 1006);
        tokens_val.put("cout", 1007);
        tokens_val.put("<<", 1008);
        tokens_val.put("&", 5);
        tokens_val.put("+", 6);
        tokens_val.put("-", 7);
        tokens_val.put("|", 8);
        tokens_val.put("=", 10);
        tokens_val.put(".", 18);
        tokens_val.put(",", 19);
        tokens_val.put(":", 20);
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
        Lexer l = new Lexer();
        l.init_tokens();
        l.get();
        l.print();
    }

    public void get()
    {
        File file = new File("input.txt");
        BufferedReader reader = null;
        FileReader fr = null;
        try
        {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String inline;
            while((inline = reader.readLine()) != null)
            {
                this.line.add(inline);
                this.tokenize(inline.toCharArray());
                //read the enter line
                reader.readLine();
            }
        }
        catch(Exception e) {System.out.println(e.getMessage());}
    }

    private void tokenize(char [] char_array)
    {
        char c;
        int i=0;
        ArrayList<Token> list = new ArrayList<Token>();
        while(i<char_array.length)
        {
            c = char_array[i];
            StringBuilder str = new StringBuilder();
            if (Character.isDigit(c)||Character.isLetter(c)||(c=='-' && Character.isDigit(char_array[i+1])))
            {
                if (c=='-')
                {
                    str.append(char_array[i]);
                    ++i;
                }
                while (i<char_array.length && (Character.isDigit(char_array[i])||Character.isLetter(char_array[i])))
                {
                    str.append(char_array[i]);
                    ++i;
                }
            }
            else if (c==' ')
                ++i;
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
            if (str.length()>0)
            {
                if (tokens_val.containsKey(str.toString()))
                        list.add(new Token(tokens_val.get(str.toString()), str.toString()));
                else if (Character.isDigit(str.toString().charAt(0)))
                    list.add(new Token(tokens_val.get("number"), str.toString()));
                else if (Character.isLetter(str.toString().charAt(0)))
                    list.add(new Token(tokens_val.get("ident"), str.toString()));
                else if (Character.isDigit(str.toString().charAt(1))&&str.toString().charAt(0)=='-')
                    list.add(new Token(tokens_val.get("number"), str.toString()));
                else
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
}
