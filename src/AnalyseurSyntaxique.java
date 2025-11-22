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
        S();
        if (verifier(TokenType.FIN)) {
            System.out.println("SUCCES : Code valide !");
        } else {
            if (!err) erreur("Fin de fichier attendue");
            afficherErreur();
        }
    }

    public static void S() {
        if (err) return;

        while (i < tokens.size() &&
                tokens.get(i).type != TokenType.FIN &&
                tokens.get(i).type != TokenType.ACCOLADE_FERMANTE) {

            Instruction();
            if (err) return;
        }
    }

    public static void Instruction() {
        if (err) return;

        TokenType typeCourant = tokens.get(i).type;

        if (typeCourant == TokenType.FOREACH) {
            Foreach();
        } else if (typeCourant == TokenType.VARIABLE) {
            Affectation();
        } else if (typeCourant == TokenType.NOM) {
            Signature();
        } else {
            erreur("Instruction inconnue (Attendu: foreach, variable ou Signature)");
        }
    }

    public static void Foreach() {
        if (verifier(TokenType.FOREACH)) {
            if (verifier(TokenType.PARENTHESE_OUVRANTE)) {
                if (verifier(TokenType.VARIABLE)) {
                    if (verifier(TokenType.AS)) {
                        if (verifier(TokenType.VARIABLE)) {
                            if (verifier(TokenType.PARENTHESE_FERMANTE)) {
                                if (verifier(TokenType.ACCOLADE_OUVRANTE)) {
                                    S();
                                    if (verifier(TokenType.ACCOLADE_FERMANTE)) {

                                    } else {
                                        erreur("Manque }");
                                    }
                                } else {
                                    erreur("Manque {");
                                }
                            } else {
                                erreur("Manque )");
                            }
                        } else {
                            erreur("Manque variable value");
                        }
                    } else {
                        erreur("Manque as");
                    }
                } else {
                    erreur("Manque variable array");
                }
            } else {
                erreur("Manque (");
            }
        }
    }

    public static void Affectation() {
        if (verifier(TokenType.VARIABLE)) {
            if (err) return;

            TokenType typeSuivant = tokens.get(i).type;

            if (typeSuivant == TokenType.AFFECTATION) {
                verifier(TokenType.AFFECTATION);
                Expression();
                if (!verifier(TokenType.POINT_VIRGULE)) {
                    erreur("Manque ;");
                }
            } else if (typeSuivant == TokenType.INCREMENTATION) {
                verifier(TokenType.INCREMENTATION);
                if (!verifier(TokenType.POINT_VIRGULE)) {
                    erreur("Manque ;");
                }
            } else {
                erreur("Attendu = ou ++ après la variable");
            }
        }
    }

    public static void Signature() {
        if (verifier(TokenType.NOM)) {
            if (verifier(TokenType.PRENOM)) {
                if (!verifier(TokenType.POINT_VIRGULE)) {
                    erreur("Manque ;");
                }
            } else {
                erreur("Manque Prénom");
            }
        }
    }

    public static void Expression() {
        TokenType typeCourant = tokens.get(i).type;
        if (typeCourant == TokenType.NOMBRE ||
                typeCourant == TokenType.VARIABLE ||
                typeCourant == TokenType.IDENTIFIANT) {
            i++;
        } else {
            erreur("Expression attendue (Nombre, Variable ou Identifiant)");
        }
    }
}