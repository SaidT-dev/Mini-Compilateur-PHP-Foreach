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

    public static void erreur(String message) throws Exception {
        if (!err) {
            err = true;
            errMsg = message;
            throw new Exception(construireMessageErreur());
        }
    }

    public static String construireMessageErreur() {
        int index = (i < tokens.size()) ? i : tokens.size() - 1;
        if (index < 0) {
            return "Erreur sur un token inexistant. Le code est peut-être vide.";
        }
        Token erreurToken = tokens.get(index);

        return "ERREUR SYNTAXIQUE Ligne " + erreurToken.ligne + "\n" +
                "    Token trouve : \"" + erreurToken.val + "\"\n" +
                "    Attendu      : " + errMsg;
    }

    public static void Z() throws Exception {
        S();
        if (verifier(TokenType.FIN)) {
            // SUCCESS, do nothing, UI will show success
        } else {
            if (!err) {
                erreur("Fin de fichier attendue");
            }
        }
    }

    public static void S() throws Exception {
        if (err) return;

        while (i < tokens.size() &&
                tokens.get(i).type != TokenType.FIN &&
                tokens.get(i).type != TokenType.ACCOLADE_FERMANTE) {

            Instruction();
            if (err) return;
        }
    }

    public static void Instruction() throws Exception {
        if (err) return;

        TokenType typeCourant = tokens.get(i).type;

        if (typeCourant == TokenType.FOREACH) {
            Foreach();
        } else if (typeCourant == TokenType.IF || typeCourant == TokenType.WHILE || typeCourant == TokenType.FOR) {
            ignorerBloc();
        } else if (typeCourant == TokenType.ELSE) {
            // On peut avoir "else if" ou juste "else"
            i++; // Consomme le "else"
            if (i < tokens.size() && tokens.get(i).type == TokenType.IF) {
                ignorerBloc(); // C'est un "else if", on ignore tout le bloc
            } else {
                // C'est un "else" simple, on cherche l'accolade ouvrante
                if (i < tokens.size() && tokens.get(i).type == TokenType.ACCOLADE_OUVRANTE) {
                    ignorerBloc();
                }
                // Si pas d'accolade, on suppose une seule instruction à ignorer, ce qui est plus complexe.
                // Pour l'instant, on gère le cas simple avec bloc.
            }
        } else if (typeCourant == TokenType.VARIABLE) {
            Affectation();
        } else if (typeCourant == TokenType.NOM) {
            Signature();
        } else {
            erreur("Instruction inconnue (Attendu: foreach, variable ou Signature)");
        }
    }

    private static void ignorerBloc() throws Exception {
        int accolades = 0;
        boolean blocCommence = false;

        // Cherche la parenthèse ouvrante de la condition
        while (i < tokens.size() && tokens.get(i).type != TokenType.PARENTHESE_OUVRANTE) {
            i++;
        }
        if (i >= tokens.size()) {
            erreur("Structure de contrôle incomplete, parenthèse ouvrante manquante.");
            return;
        }

        // Cherche l'accolade ouvrante du bloc
        while (i < tokens.size() && tokens.get(i).type != TokenType.ACCOLADE_OUVRANTE) {
            i++;
        }
        if (i >= tokens.size()) {
            erreur("Structure de contrôle incomplete, accolade ouvrante manquante.");
            return;
        }

        // Maintenant, on compte les accolades pour trouver la fin du bloc
        while (i < tokens.size()) {
            TokenType type = tokens.get(i).type;
            if (type == TokenType.ACCOLADE_OUVRANTE) {
                accolades++;
                blocCommence = true;
            } else if (type == TokenType.ACCOLADE_FERMANTE) {
                accolades--;
            }
            i++;
            if (blocCommence && accolades == 0) {
                break; // Fin du bloc
            }
        }

        if (accolades != 0) {
            erreur("Structure de contrôle non fermée (accolade fermante manquante).");
        }
    }

    public static void Foreach() throws Exception {
        if (verifier(TokenType.FOREACH)) {
            if (verifier(TokenType.PARENTHESE_OUVRANTE)) {
                if (verifier(TokenType.VARIABLE)) {
                    if (verifier(TokenType.AS)) {
                        if (verifier(TokenType.VARIABLE)) {
                            if (verifier(TokenType.PARENTHESE_FERMANTE)) {
                                if (verifier(TokenType.ACCOLADE_OUVRANTE)) {
                                    S(); // Contenu du foreach
                                    if (!verifier(TokenType.ACCOLADE_FERMANTE)) {
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

    public static void Affectation() throws Exception {
        if (verifier(TokenType.VARIABLE)) {
            if (err) return;

            if (i >= tokens.size()) {
                erreur("Unexpected end of file after variable");
                return;
            }

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
            } else if (typeSuivant == TokenType.DECREMENTATION) {
                verifier(TokenType.DECREMENTATION);
                if (!verifier(TokenType.POINT_VIRGULE)) {
                    erreur("Manque ;");
                }
            } else if (typeSuivant == TokenType.POINT_VIRGULE) {
                verifier(TokenType.POINT_VIRGULE);
            } else {
                erreur("Attendu =, ++, -- ou ; après la variable");
            }
        }
    }

    public static void Signature() throws Exception {
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

    public static void Expression() throws Exception {
        Terme();
        while (i < tokens.size() &&
                (tokens.get(i).type == TokenType.PLUS || tokens.get(i).type == TokenType.MOINS)) {
            if (err) return;
            i++; // Consomme l'opérateur
            Terme();
        }
    }

    public static void Terme() throws Exception {
        Facteur();
        while (i < tokens.size() &&
                (tokens.get(i).type == TokenType.FOIS || tokens.get(i).type == TokenType.DIVISE)) {
            if (err) return;
            i++;
            Facteur();
        }
    }

    public static void Facteur() throws Exception {
        if (err) return;

        TokenType typeCourant = tokens.get(i).type;

        if (typeCourant == TokenType.NOMBRE ||
                typeCourant == TokenType.VARIABLE ||
                typeCourant == TokenType.IDENTIFIANT) {
            i++;
        }
        else if (typeCourant == TokenType.PARENTHESE_OUVRANTE) {
            i++;
            Expression();
            if (!verifier(TokenType.PARENTHESE_FERMANTE)) {
                erreur("Manque ) après l'expression");
            }
        }
        else {
            erreur("Expression attendue (Nombre, Variable ou '('...)");
        }
    }
}