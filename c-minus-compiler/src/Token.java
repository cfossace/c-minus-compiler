/*
 *  This class is the class that holds the tokens and the number represent them.
 */

public class Token
{
    String token;
    int token_num;

    public Token(int num,String tok)
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
