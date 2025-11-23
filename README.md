# Mini Compilateur PHP (Spécialité Foreach)

Ce projet est un mini-compilateur développé en **Java**. Il analyse un sous-ensemble du langage **PHP**, en se concentrant spécifiquement sur la structure de contrôle `foreach`, la gestion des variables et une signature personnalisée.

Il a été réalisé dans le cadre du TP de Compilation.

## 🚀 Fonctionnalités

Le compilateur effectue deux phases principales :

### 1. Analyse Lexicale (Scanner)
* **Approche manuelle :** Lecture du code source caractère par caractère (simulation d'un automate à états finis).
* **Tokens reconnus :**
    * Mots-clés : `foreach`, `as`
    * Variables PHP : `$maVariable`, `$compteur`...
    * Identifiants & Signature : `Said`, `Tadjine`
    * Nombres entiers
    * Opérateurs : `=`, `++`, `==`
    * Symboles : `{`, `}`, `(`, `)`, `;`

### 2. Analyse Syntaxique (Parser)
* **Méthode :** Analyse Descendante Récursive (Recursive Descent Parsing).
* **Validation :** Vérifie que la suite de tokens respecte la grammaire définie.
* **Gestion d'erreurs :** Détecte les erreurs syntaxiques (ex: point-virgule manquant, parenthèse oubliée) et affiche la ligne correspondante sans planter le programme.

---

## 📜 Grammaire (BNF)

Voici les règles de production supportées par le compilateur :

```text
Z (Axiome)   -> S FIN
S            -> Instruction S | ε
Instruction  -> Foreach | Affectation | Signature

Foreach      -> "foreach" "(" VARIABLE "as" VARIABLE ")" "{" S "}"
Affectation  -> VARIABLE "=" Expression ";" 
              | VARIABLE "++" ";"
              | VARIABLE "==" Expression
Signature    -> "Said" "Tadjine" ";"

Expression   -> NOMBRE | VARIABLE | IDENTIFIANT