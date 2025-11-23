# Mini Compilateur PHP (Spécialité Foreach)

![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![Java Version](https://img.shields.io/badge/Java-8+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)
![Version](https://img.shields.io/badge/Version-1.0-brightgreen.svg)

Un compilateur pédagogique développé en **Java** dans le cadre du module de Compilation. Il analyse un sous-ensemble du langage **PHP**, avec un focus particulier sur la structure de contrôle `foreach`, la gestion des variables dynamiques et les expressions arithmétiques.

✨ **Nouveauté :** Le projet inclut une **Interface Graphique (Mini IDE)** pour écrire et compiler le code visuellement.

---

## 🚀 Fonctionnalités Clés

### 1. Interface Graphique (GUI)
* **Éditeur de code** : Zone de texte pour écrire vos scripts PHP.
* **Console intégrée** : Affiche le résultat de l'analyse ou les erreurs avec précision.
* **Contrôles** : Boutons pour compiler et effacer le contenu.

### 2. Analyse Lexicale (Scanner)
* **Technique** : Automate à états finis manuel (lecture caractère par caractère).
* **Support** :
  * Mots-clés : `foreach`, `as`
  * Variables : `$maVariable`, `$compteur`
  * Nombres entiers
  * Opérateurs : `+`, `-`, `*`, `/`, `=`, `++`, `--`
  * Symboles : `{`, `}`, `(`, `)`, `;`
  * **Signature Spéciale** : `Said`, `Tadjine`

### 3. Analyse Syntaxique (Parser)
* **Technique** : Analyse Descendante Récursive.
* **Priorité des opérateurs** : La multiplication `*` et la division `/` sont traitées avant l'addition `+` et la soustraction `-`.
* **Expressions complexes** : Supporte les parenthèses `($a + 1) * 5`.
* **Gestion d'erreurs** : Système robuste qui indique le token attendu en cas d'erreur syntaxique.

---

## 📜 Grammaire (BNF)

Le compilateur valide la grammaire suivante :

```text
Z (Axiome)   -> S FIN
FIN          -> (Fin du fichier)
S            -> Instruction S | ε
Instruction  -> Foreach | Affectation | Signature

Foreach      -> "foreach" "(" VARIABLE "as" VARIABLE ")" "{" S "}"

Affectation  -> VARIABLE "=" Expression ";" 
              | VARIABLE "++" ";"
              | VARIABLE "--" ";"
              
Signature    -> "Said" "Tadjine" ";"

Expression   -> Terme { ("+"|"-") Terme }
Terme        -> Facteur { ("*"|"/") Facteur }
Facteur      -> NOMBRE | VARIABLE | "(" Expression ")"
```

## 💻 Exemple de Code PHP Supporté
Voici un exemple complet que vous pouvez tester dans l'interface :

```php
$total = 0;
$prix = 50;

foreach ($panier as $article) {
    Said Tadjine;           // Signature obligatoire
    
    // Calcul avec priorité et parenthèses
    $total = ($prix + 10) * 2; 
    
    $compteur++;
}
```

---

## 🛠️ Installation et Compilation (Pour les développeurs)
Si vous souhaitez modifier le code source :

### Prérequis
* Java JDK (version 8 ou supérieure).

### Instructions
1.  **Clonez ce dépôt :**
    ```bash
    git clone https://github.com/SaidTadjine/Mini-Compilateur-PHP.git
    cd Mini-Compilateur-PHP
    ```

2.  **Compilez les sources :**
    *(Cette commande place les fichiers .class à la racine du projet)*
    ```bash
    javac -d . src/*.java
    ```

3.  **Lancez l'application :**
    ```bash
    java Main
    ```

## 📦 Créer un fichier JAR exécutable

Pour packager le compilateur dans un fichier `.jar` auto-exécutable :

1.  **Assurez-vous que le fichier `src/META-INF/MANIFEST.MF` existe avec le contenu suivant :**
    ```
    Main-Class: Main
    ```

2.  **Compilez et créez le JAR :**
    *(Cette commande doit être lancée depuis la racine du projet)*
    ```bash
    jar cfm MiniCompilateur.jar src/META-INF/MANIFEST.MF *.class
    ```

3.  **Exécutez le JAR :**
    ```bash
    java -jar MiniCompilateur.jar
    ```

---

## 📂 Structure du Projet
```
.
└── src/
    ├── Main.java                # Point d'entrée (Lance l'interface graphique)
    ├── Interface.java           # Fenêtre Swing (Mini IDE)
    ├── AnalyseurLexical.java    # Scanner (Transforme le texte en Tokens)
    ├── AnalyseurSyntaxique.java # Parser (Vérifie la grammaire récursivement)
    ├── Token.java               # Objet représentant un mot du langage
    ├── TokenType.java           # Enumération des types de tokens
    └── META-INF/
        └── MANIFEST.MF          # Fichier de manifeste pour le JAR
```

---

## 👤 Auteur
**Said Tadjine**

*   **Module** : Compilation
*   **Sujet** : Mini-Compilateur PHP (foreach)

