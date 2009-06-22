

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
        public String toString()
        {
        	return this.token;
        }
    }

