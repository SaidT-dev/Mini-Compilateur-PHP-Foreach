import java.util.ArrayList;
import java.util.List;

public class AnalyseurLexical {

    private String code;
    private int pos = 0;
    private int ligne = 1;
    private List<Token> tokens;
    private static final char EOF = '\0';

    public AnalyseurLexical(String code) {
        this.code = code;
        tokens = new ArrayList<>();
    }

    private char lireCaractere() {
        try {
            return code.charAt(pos);
        } catch (IndexOutOfBoundsException e) {
            return EOF;
        }
    }

    private char regarderSuivant() {
        try {
            return code.charAt(pos + 1);
        } catch (IndexOutOfBoundsException e) {
            return EOF;
        }
    }

    public List<Token> scan() {
        char c = lireCaractere();

        while (c != EOF) {
            // 1. Blancs
            if (c == ' ' || c == '\t' || c == '\r') {
                pos++;
            }
            else if (c == '\n') {
                ligne++;
                pos++;
            }
            // 2. Variables
            else if (c == '$') {
                lireVariable();
            }
            // 3. Mots
            else if (estLettre(c)) {
                lireMot();
            }
            // 4. Nombres
            else if (estChiffre(c)) {
                lireNombre();
            }
            // 5. Symboles et Opérateurs
            else {
                if (c == '{') {
                    ajouterToken(TokenType.ACCOLADE_OUVRANTE, "{");
                    pos++;
                } else if (c == '}') {
                    ajouterToken(TokenType.ACCOLADE_FERMANTE, "}");
                    pos++;
                } else if (c == '(') {
                    ajouterToken(TokenType.PARENTHESE_OUVRANTE, "(");
                    pos++;
                } else if (c == ')') {
                    ajouterToken(TokenType.PARENTHESE_FERMANTE, ")");
                    pos++;
                } else if (c == '[') {
                    ajouterToken(TokenType.CROCHET_OUVRANT, "[");
                    pos++;
                } else if (c == ']') {
                    ajouterToken(TokenType.CROCHET_FERMANT, "]");
                    pos++;
                } else if (c == ',') {
                    ajouterToken(TokenType.VIRGULE, ",");
                    pos++;
                } else if (c == ';') {
                    ajouterToken(TokenType.POINT_VIRGULE, ";");
                    pos++;
                }
                // GESTION DU PLUS (+) et INCREMENTATION (++)
                else if (c == '+') {
                    if (regarderSuivant() == '+') {
                        ajouterToken(TokenType.INCREMENTATION, "++");
                        pos += 2;
                    } else {
                        ajouterToken(TokenType.PLUS, "+"); // Ajouté !
                        pos++;
                    }
                }
                // GESTION DU MOINS (-) et DECREMENTATION (--)
                else if (c == '-') {
                    if (regarderSuivant() == '-') {
                        ajouterToken(TokenType.DECREMENTATION, "--");
                        pos += 2;
                    } else {
                        ajouterToken(TokenType.MOINS, "-"); // Ajouté !
                        pos++;
                    }
                }
                // GESTION DU FOIS (*)
                else if (c == '*') {
                    ajouterToken(TokenType.FOIS, "*"); // Ajouté !
                    pos++;
                }
                // GESTION DU DIVISE (/)
                else if (c == '/') {
                    if (regarderSuivant() == '/') {
                        lireCommentaireSimple();
                    } else if (regarderSuivant() == '*') {
                        lireCommentaireMultiLignes();
                    } else {
                        ajouterToken(TokenType.DIVISE, "/");
                        pos++;
                    }
                }
                // GESTION DES STRINGS
                else if (c == '"' || c == '\'') {
                    lireString(c);
                }
                // GESTION DU POINT
                else if (c == '.') {
                    ajouterToken(TokenType.DOT, ".");
                    pos++;
                }
                // GESTION DU TERNAIRE
                else if (c == '?') {
                    if (regarderSuivant() == '>') {
                        ajouterToken(TokenType.PHP_TAG_FERMANT, "?>");
                        pos += 2;
                    } else {
                        ajouterToken(TokenType.QUESTION_MARK, "?");
                        pos++;
                    }
                } else if (c == '<') {
                    if (code.length() > pos + 4 && code.substring(pos, pos + 5).equals("<?php")) {
                        ajouterToken(TokenType.PHP_TAG_OUVRANT, "<?php");
                        pos += 5;
                    }
                    // Potentiellement d'autres opérateurs avec < ici
                }
                else if (c == ':') {
                    ajouterToken(TokenType.COLON, ":");
                    pos++;
                }
                // GESTION DES OPERATEURS LOGIQUES
                else if (c == '&' && regarderSuivant() == '&') {
                    ajouterToken(TokenType.AND, "&&");
                    pos += 2;
                } else if (c == '|' && regarderSuivant() == '|') {
                    ajouterToken(TokenType.OR, "||");
                    pos += 2;
                }
                // GESTION DU EGAL (=) et EGALITE (==)
                else if (c == '=') {
                    if (regarderSuivant() == '=') {
                        ajouterToken(TokenType.EGALITE, "==");
                        pos += 2;
                    } else if (regarderSuivant() == '>') {
                        ajouterToken(TokenType.FLECHE, "=>");
                        pos += 2;
                    } else {
                        ajouterToken(TokenType.AFFECTATION, "=");
                        pos++;
                    }
                } else {
                    System.out.println("Caractère inconnu: " + c + " à la ligne " + ligne);
                    pos++;
                }
            }

            c = lireCaractere();
        }

        tokens.add(new Token(TokenType.FIN, "", ligne));
        return tokens;
    }

    private void lireCommentaireSimple() {
        pos += 2; // Saute le //
        while (lireCaractere() != '\n' && lireCaractere() != EOF) {
            pos++;
        }
    }

    private void lireCommentaireMultiLignes() {
        pos += 2; // Saute le /*
        while (lireCaractere() != '*' || regarderSuivant() != '/') {
            if (lireCaractere() == '\n') {
                ligne++;
            }
            if (lireCaractere() == EOF) {
                // Erreur, commentaire non fermé
                return;
            }
            pos++;
        }
        pos += 2; // Saute le */
    }

    private void lireString(char delimiteur) {
        int debut = ++pos;
        while (lireCaractere() != delimiteur && lireCaractere() != EOF) {
            if (lireCaractere() == '\\') { // Gérer les caractères d'échappement
                pos++;
            }
            pos++;
        }
        String str = code.substring(debut, pos);
        ajouterToken(TokenType.STRING, str);
        if (lireCaractere() == delimiteur) {
            pos++;
        }
    }

    private void lireVariable() {
        int debut = pos;
        pos++;
        while (estLettre(lireCaractere()) || estChiffre(lireCaractere())) {
            pos++;
        }
        String variable = code.substring(debut, pos);
        ajouterToken(TokenType.VARIABLE, variable);
    }

    private void lireMot() {
        int debut = pos;
        while (estLettre(lireCaractere())) {
            pos++;
        }
        String str = code.substring(debut, pos);

        if (egale(str, "foreach")) {
            ajouterToken(TokenType.FOREACH, str);
        } else if (egale(str, "as")) {
            ajouterToken(TokenType.AS, str);
        } else if (egale(str, "if")) {
            ajouterToken(TokenType.IF, str);
        } else if (egale(str, "else")) {
            ajouterToken(TokenType.ELSE, str);
        } else if (egale(str, "while")) {
            ajouterToken(TokenType.WHILE, str);
        } else if (egale(str, "for")) {
            ajouterToken(TokenType.FOR, str);
        } else if (egale(str, "true") || egale(str, "false")) {
            ajouterToken(TokenType.BOOLEAN, str);
        } else if (egale(str, "elseif")) {
            ajouterToken(TokenType.ELSEIF, str);
        } else if (egale(str, "echo")) {
            ajouterToken(TokenType.ECHO, str);
        } else if (egale(str, "function")) {
            ajouterToken(TokenType.FUNCTION, str);
        } else if (egale(str, "return")) {
            ajouterToken(TokenType.RETURN, str);
        } else if (egale(str, "switch")) {
            ajouterToken(TokenType.SWITCH, str);
        } else if (egale(str, "case")) {
            ajouterToken(TokenType.CASE, str);
        } else if (egale(str, "default")) {
            ajouterToken(TokenType.DEFAULT, str);
        } else if (egale(str, "break")) {
            ajouterToken(TokenType.BREAK, str);
        } else if (egale(str, "do")) {
            ajouterToken(TokenType.DO, str);
        } else if (egale(str, "Said")) {
            ajouterToken(TokenType.NOM, str);
        } else if (egale(str, "Tadjine")) {
            ajouterToken(TokenType.PRENOM, str);
        } else {
            ajouterToken(TokenType.IDENTIFIANT, str);
        }
    }

    private void lireNombre() {
        int debut = pos;
        while (estChiffre(lireCaractere())) {
            pos++;
        }
        String nombre = code.substring(debut, pos);
        ajouterToken(TokenType.NOMBRE, nombre);
    }

    private void ajouterToken(TokenType type, String valeur) {
        tokens.add(new Token(type, valeur, this.ligne));
    }

    private boolean estLettre(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean estChiffre(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean egale(String str1, String str2) {
        if (str1.length() != str2.length()) {
            return false;
        }

        for (int i = 0; i < str1.length(); i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                return false;
            }
        }

        return true;
    }
}