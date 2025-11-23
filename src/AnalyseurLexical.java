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
                    // Note: C'est ici qu'on pourrait gérer les commentaires // si on voulait
                    ajouterToken(TokenType.DIVISE, "/"); // Ajouté !
                    pos++;
                }
                // GESTION DU EGAL (=) et EGALITE (==)
                else if (c == '=') {
                    if (regarderSuivant() == '=') {
                        ajouterToken(TokenType.EGALITE, "==");
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