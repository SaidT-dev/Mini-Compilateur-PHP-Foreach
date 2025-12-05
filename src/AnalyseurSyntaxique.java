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
        if (verifier(TokenType.PHP_TAG_OUVRANT)) {
            S();
            if (i < tokens.size() && tokens.get(i).type == TokenType.PHP_TAG_FERMANT) {
                verifier(TokenType.PHP_TAG_FERMANT);
            } else if (i >= tokens.size() || tokens.get(i).type != TokenType.FIN) {
                 if(i>0 && tokens.get(i-1).type != TokenType.POINT_VIRGULE && tokens.get(i-1).type != TokenType.ACCOLADE_FERMANTE){
                    erreur("Manque ;");
                }
            }
        } else {
            erreur("Attendu <?php");
        }
    }

    public static void S() throws Exception {
        if (err) return;

        while (i < tokens.size() &&
                tokens.get(i).type != TokenType.FIN &&
                tokens.get(i).type != TokenType.ACCOLADE_FERMANTE &&
                tokens.get(i).type != TokenType.PHP_TAG_FERMANT) {

            Instruction();
            if (err) return;
        }
    }

    public static void Instruction() throws Exception {
        if (err) return;

        TokenType typeCourant = tokens.get(i).type;

        if (typeCourant == TokenType.FOREACH) {
            Foreach();
        } else if (typeCourant == TokenType.IF || typeCourant == TokenType.WHILE || typeCourant == TokenType.FOR || typeCourant == TokenType.SWITCH) {
            ignorerBloc(true);
        } else if (typeCourant == TokenType.DO) {
            ignorerBloc(true);
        } else if (typeCourant == TokenType.ELSE) {
            i++;
            if (i < tokens.size() && tokens.get(i).type == TokenType.IF) {
                ignorerBloc(true);
            } else {
                ignorerBloc(false);
            }
        } else if (typeCourant == TokenType.VARIABLE) {
            Affectation();
        } else if (typeCourant == TokenType.ECHO) {
            Echo();
        } else if (typeCourant == TokenType.FUNCTION) {
            Function();
        } else if (typeCourant == TokenType.RETURN) {
            Return();
        } else if (typeCourant == TokenType.NOM) {
            Signature();
        } else {
            erreur("Instruction inconnue (Attendu: foreach, variable ou Signature)");
        }
    }

    private static void ignorerBloc(boolean consommerMotCle) throws Exception {
        if (consommerMotCle) {
            i++;
        }

        TokenType typeCourant = tokens.get(i-1).type;

        if (typeCourant == TokenType.IF || typeCourant == TokenType.WHILE || typeCourant == TokenType.FOR || typeCourant == TokenType.SWITCH) {
            if (!verifier(TokenType.PARENTHESE_OUVRANTE)) {
                erreur("Structure de contrôle incomplète, parenthèse ouvrante manquante.");
                return;
            }
            int parentheses = 1;
            while (i < tokens.size() && parentheses > 0) {
                TokenType type = tokens.get(i).type;
                if (type == TokenType.PARENTHESE_OUVRANTE) parentheses++;
                else if (type == TokenType.PARENTHESE_FERMANTE) parentheses--;
                i++;
            }
            if (parentheses != 0) {
                erreur("Parenthèses non équilibrées dans la condition.");
                return;
            }
        }

        if (verifier(TokenType.ACCOLADE_OUVRANTE)) {
            int accolades = 1;
            while (i < tokens.size() && accolades > 0) {
                TokenType type = tokens.get(i).type;
                if (type == TokenType.ACCOLADE_OUVRANTE) accolades++;
                else if (type == TokenType.ACCOLADE_FERMANTE) accolades--;
                i++;
            }
            if (accolades != 0) {
                erreur("Accolades non équilibrées dans le bloc.");
            }
        } else {
            while (i < tokens.size() && tokens.get(i).type != TokenType.POINT_VIRGULE && tokens.get(i).type != TokenType.ACCOLADE_OUVRANTE) {
                i++;
            }
            if (i < tokens.size() && tokens.get(i).type == TokenType.POINT_VIRGULE) {
                i++;
            }
        }

        if (typeCourant == TokenType.DO) {
            if (!verifier(TokenType.WHILE)) {
                erreur("Manque 'while' après le bloc do-while.");
                return;
            }
            if (!verifier(TokenType.PARENTHESE_OUVRANTE)) {
                erreur("Manque '(' après le 'while' du do-while.");
                return;
            }
            int parentheses = 1;
            while (i < tokens.size() && parentheses > 0) {
                TokenType type = tokens.get(i).type;
                if (type == TokenType.PARENTHESE_OUVRANTE) parentheses++;
                else if (type == TokenType.PARENTHESE_FERMANTE) parentheses--;
                i++;
            }
            if (parentheses != 0) {
                erreur("Parenthèses non équilibrées dans la condition du do-while.");
                return;
            }
            if (!verifier(TokenType.POINT_VIRGULE)) {
                erreur("Manque ';' après la condition du do-while.");
            }
        }
    }

    public static void Foreach() throws Exception {
        if (verifier(TokenType.FOREACH)) {
            if (verifier(TokenType.PARENTHESE_OUVRANTE)) {
                Expression();
                if (verifier(TokenType.AS)) {
                    if (verifier(TokenType.VARIABLE)) {
                        if (verifier(TokenType.FLECHE)) {
                            if (verifier(TokenType.VARIABLE)) {
                                if (verifier(TokenType.PARENTHESE_FERMANTE)) {
                                    if (verifier(TokenType.ACCOLADE_OUVRANTE)) {
                                        S();
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
                        } else if (verifier(TokenType.PARENTHESE_FERMANTE)) {
                            if (verifier(TokenType.ACCOLADE_OUVRANTE)) {
                                S();
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

    public static void Echo() throws Exception {
        if (verifier(TokenType.ECHO)) {
            Expression();
            if (!verifier(TokenType.POINT_VIRGULE)) {
                erreur("Manque ; après l'expression dans echo");
            }
        }
    }

    public static void Function() throws Exception {
        if (verifier(TokenType.FUNCTION)) {
            if (verifier(TokenType.IDENTIFIANT)) {
                if (verifier(TokenType.PARENTHESE_OUVRANTE)) {
                    if (verifier(TokenType.VARIABLE)) {
                        while (verifier(TokenType.VIRGULE)) {
                            if (!verifier(TokenType.VARIABLE)) {
                                erreur("Variable attendue après la virgule dans les paramètres de la fonction");
                                return;
                            }
                        }
                    }
                    if (verifier(TokenType.PARENTHESE_FERMANTE)) {
                        if (verifier(TokenType.ACCOLADE_OUVRANTE)) {
                            S();
                            if (!verifier(TokenType.ACCOLADE_FERMANTE)) {
                                erreur("Manque } pour fermer la fonction");
                            }
                        } else {
                            erreur("Manque { pour ouvrir le corps de la fonction");
                        }
                    } else {
                        erreur("Manque ) pour fermer les paramètres de la fonction");
                    }
                } else {
                    erreur("Manque ( après le nom de la fonction");
                }
            } else {
                erreur("Nom de fonction attendu");
            }
        }
    }

    public static void Return() throws Exception {
        if (verifier(TokenType.RETURN)) {
            Expression();
            if (!verifier(TokenType.POINT_VIRGULE)) {
                erreur("Manque ; après l'expression de retour");
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
                (tokens.get(i).type == TokenType.PLUS || tokens.get(i).type == TokenType.MOINS || tokens.get(i).type == TokenType.DOT)) {
            if (err) return;
            i++;
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

        if (typeCourant == TokenType.VARIABLE && i + 1 < tokens.size() && tokens.get(i + 1).type == TokenType.CROCHET_OUVRANT) {
            i++;
            verifier(TokenType.CROCHET_OUVRANT);
            Expression();
            if (!verifier(TokenType.CROCHET_FERMANT)) {
                erreur("Manque ] pour fermer l'accès au tableau.");
            }
        }
        else if (typeCourant == TokenType.NOMBRE ||
                typeCourant == TokenType.VARIABLE ||
                typeCourant == TokenType.STRING ||
                typeCourant == TokenType.BOOLEAN ||
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
        else if (typeCourant == TokenType.CROCHET_OUVRANT) {
            i++;
            if (tokens.get(i).type != TokenType.CROCHET_FERMANT) {
                do {
                    if ((tokens.get(i).type == TokenType.STRING || tokens.get(i).type == TokenType.NOMBRE) &&
                            i + 1 < tokens.size() && tokens.get(i + 1).type == TokenType.FLECHE) {
                        i++;
                        verifier(TokenType.FLECHE);
                        Expression();
                    } else {
                        Expression();
                    }
                } while (verifier(TokenType.VIRGULE));
            }
            if (!verifier(TokenType.CROCHET_FERMANT)) {
                erreur("Manque ] pour fermer le tableau");
            }
        }
        else {
            erreur("Expression attendue (Nombre, Variable, '(', '['...)");
        }
    }
}