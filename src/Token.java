public class Token {
    public TokenType type;
    public String val;
    public int ligne;

    public Token(TokenType type, String val, int ligne) {
        this.type = type;
        this.val = val;
        this.ligne = ligne;
    }

    @Override
    public String toString() {
        return "Token { type: " + type +", val: \"" + val + "\", ligne:" + ligne + " }";
    }
}
