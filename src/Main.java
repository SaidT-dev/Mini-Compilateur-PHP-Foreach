import java.util.List;

public class Main {

    public static void main(String[] args) {

        String code = """
            $compteur = 0;
            foreach ($utilisateurs as $user) {
                Said Tadjine;
                $compteur++;
            }
        """;

        System.out.println("--- CODE SOURCE ---");
        System.out.println(code);
        System.out.println("-------------------");

        AnalyseurLexical lexer = new AnalyseurLexical(code);
        List<Token> tokens = lexer.scan();

        AnalyseurSyntaxique.init(tokens);
        AnalyseurSyntaxique.Z();
    }
}