import java.util.List;

public class AnalyseurSyntaxique {
    private static List<Token> tokens;
    private static int i = 0;
    private static boolean err = false;
    private static String errMsg;

    public static void init(List<Token> tokensALire) {
        tokens = tokensALire;
        i = 0;
        err = false;
        errMsg = "";
    }

    public static boolean verifier(TokenType typeAttendu) {
        if (err) return false;

        if (i >= tokens.size()) return false;

        Token courant = tokens.get(i);

        if (courant.type == typeAttendu) {
            i++;
            return true;
        } else {
            return false;
        }
    }

    public static void erreur(String message) {
        if (!err) {
            err = true;
            errMsg = message;
        }
    }

    public static void afficherErreur() {
        int index = (i < tokens.size()) ? i : tokens.size() - 1;
        Token erreurToken = tokens.get(index);

        System.out.println("\n>>> ERREUR SYNTAXIQUE Ligne " + erreurToken.ligne);
        System.out.println("    Token trouve : \"" + erreurToken.val + "\"");
        System.out.println("    Attendu      : " + errMsg);
    }

    public static void Z() {

    }
}