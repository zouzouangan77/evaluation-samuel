# Instructions aux candidat-e-s

- Finaliser/sécuriser l'authentification
- Faire/finir et sécuriser les routes pour voir son client, ses comptes, ses users 
- Permettre de se connecter avec un client pour voir ses données
- Pour la route Account, permettre de modifier "uniquement" le nom du compte (pas le montant)

---

# Description du projet

## Prérequis et Démarrage
- **Java 21**
- **Maven 3.8+**
- Commande de démarrage :
  ```bash
  mvn spring-boot:run
  ```
- L'application démarre par défaut sur le port `8080` : `http://localhost:8080`.

## Base de données

Le projet utilise une base SQLite locale située dans `sqlite/sqlite.test.db`, initialisée automatiquement à chaque démarrage (voir `DataInitializationConfig.java`).

### Données de test pré-chargées :
| Utilisateur | Mot de passe | Client ID | Personne Morale (legalEntity) | Compte associé | Montant |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `Alice` | `aliceSecret` | `1` | `true` | AMAZON | 1000.0 |
| `Bernard` | `bernardSecret` | `2` | `true` | BORNEO | 2000.0 |
| `Chloé` | `chloeSecret` | `3` | `true` | COMORA | 3000.0 |
| `David` | `davidSecret` | `4` | `false` | *(aucun)* | - |
| `Emma` | `emmaSecret` | `5` | `false` | *(aucun)* | - |

---

## Sécurité et Authentification (JWT)

Toutes les requêtes vers l'API (à l'exception de `/api/auth/**`) requièrent la présence du header HTTP suivant :
```http
Authorization: Bearer <votre_token_jwt>
```

---

## Documentation des Endpoints de l'API REST

### 1. Authentification (`/api/auth`)

#### Connexion avec un utilisateur
- **URL** : `POST /api/auth/login`
- **Authentification requise** : Non
- **Corps de requête (JSON)** :
  ```json
  {
    "username": "Alice",
    "password": "aliceSecret"
  }
  ```
- **Réponse (200 OK)** :
  ```json
  {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "message": "Authentification réussie"
  }
  ```

#### Connexion avec un client (`client-login`)
Permet de se connecter directement avec un `clientId` (et optionnellement un couple utilisateur/mot de passe associé) :
- **URL** : `POST /api/auth/client-login`
- **Authentification requise** : Non
- **Corps de requête (JSON)** :
  ```json
  {
    "clientId": 2
  }
  ```
  *(Ou avec identifiants utilisateur optionnels : `{"clientId": 1, "username": "Alice", "password": "aliceSecret"}`)*
- **Réponse (200 OK)** :
  ```json
  {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "message": "Authentification client réussie"
  }
  ```

#### Validation d'un jeton JWT
- **URL** : `POST /api/auth/validate`
- **Authentification requise** : Oui (`Authorization: Bearer <token>`)
- **Réponse (200 OK)** :
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "message": "Jeton valide pour l'utilisateur : 1"
  }
  ```

---

### 2. Gestion des Comptes (`/api/accounts`)

*Attributs d'un compte : `id` (Integer), `name` (String), `amount` (Double), `clientId` (Integer)*

| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/accounts` | Liste l'ensemble des comptes |
| `GET` | `/api/accounts/{id}` | Détail d'un compte par son identifiant |
| `GET` | `/api/accounts/client/{clientId}` | Liste des comptes d'un client spécifique |
| `GET` | `/api/accounts/search?name={name}` | Recherche de comptes par nom |
| `POST` | `/api/accounts` | Création d'un nouveau compte |
| `PUT` | `/api/accounts/{id}` | Modification d'un compte (*règle métier : seul le nom doit être modifiable*) |
| `DELETE` | `/api/accounts/{id}` | Suppression d'un compte |

---

### 3. Gestion des Clients (`/api/clients`)

*Attributs d'un client : `id` (Integer), `legalEntity` (Boolean)*

| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/clients` | Liste l'ensemble des clients |
| `GET` | `/api/clients/{id}` | Détail d'un client par son identifiant |
| `POST` | `/api/clients` | Création d'un client |
| `PUT` | `/api/clients/{id}` | Mise à jour des informations d'un client |
| `DELETE` | `/api/clients/{id}` | Suppression d'un client |

---

### 4. Gestion des Utilisateurs (`/api/users`)

*Attributs d'un utilisateur : `id` (Integer), `username` (String), `password` (String), `clientId` (Integer)*

| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/users` | Liste l'ensemble des utilisateurs |
| `GET` | `/api/users/{id}` | Détail d'un utilisateur par son identifiant |
| `GET` | `/api/users/client/{clientId}` | Liste des utilisateurs rattachés à un client |
| `GET` | `/api/users/search?username={username}`| Recherche d'utilisateurs par nom |
| `POST` | `/api/users` | Création d'un utilisateur |
| `PUT` | `/api/users/{id}` | Mise à jour d'un utilisateur |
| `DELETE` | `/api/users/{id}` | Suppression d'un utilisateur |

---

## Axes d'amélioration & Perspectives (Production-Ready)

Dans le cadre d'une revue d'architecture et d'une projection vers un environnement de production d'entreprise, voici une analyse détaillée des améliorations à apporter :

### 1. Sécurité applicative & Gestion des identités
- **Masquage des informations sensibles (Information Disclosure)** :
  - *Constat* : Actuellement, l'entité `User` expose directement son hash de mot de passe (`password`) lors des appels `GET /api/users`.
  - *Recommandation* : Séparer strictement le modèle de persistance du modèle de présentation en introduisant des DTOs dédiés (`UserResponseDto` avec `id`, `username`, `clientId`), ou a minima annoter le champ avec `@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)`.
- **Hachage systématique des mots de passe à la création** :
  - *Constat* : La méthode `createUser` persiste le mot de passe sans passer par `PasswordUtil.hashPassword()`.
  - *Recommandation* : Intégrer l'encodage BCrypt systématiquement dans la couche de service avant persistance.
- **Gestion fine des habilitations (RBAC / Roles)** :
  - Introduire une hiérarchie de rôles (`ROLE_ADMIN`, `ROLE_CLIENT_ADMIN`, `ROLE_USER`) et sécuriser les méthodes avec `@PreAuthorize("hasRole('ADMIN')")` pour distinguer les opérations d'administration système des opérations métier client.
- **Gestion des jetons JWT (Cycle de vie & Révocation)** :
  - Externaliser la clé secrète JWT (`jwt.secret`) dans les variables d'environnement (ex: via Spring Cloud Vault ou AWS Secrets Manager).
  - Implémenter un mécanisme de jetons de rafraîchissement (**Refresh Tokens**) et une liste de révocation (Blacklist Redis) pour invalider un token avant son expiration en cas de compromission.

### 2. Robustesse, Validation des entrées & Résilience
- **Validation déclarative des DTOs (`Jakarta Bean Validation`)** :
  - Ajouter la dépendance `spring-boot-starter-validation`.
  - Définir des contraintes strictes sur les requêtes entrantes (`@NotBlank`, `@Size(min = 3, max = 50)`, `@Positive`, `@NotNull`) afin de rejeter les charges utiles invalides dès la couche HTTP (retour `400 Bad Request`) avant tout traitement métier.
- **Gestion centralisée des exceptions (`@RestControllerAdvice`)** :
  - Éliminer le code verbeux des blocs `try/catch` dispersés dans les contrôleurs.
  - Mettre en place un `GlobalExceptionHandler` interceptant les exceptions métier (`EntityNotFoundException`, `AccessDeniedException`, `MethodArgumentNotValidException`) et structurant les réponses selon le standard **RFC 7807 (Problem Details for HTTP APIs)** avec les champs `type`, `title`, `status`, `detail`, `instance` et `timestamp`.

### 3. Architecture des données & Persistance
- **Migration & Versioning de base de données (Flyway / Liquibase)** :
  - Remplacer l'exécution de requêtes SQL brutes au démarrage (`DataInitializationConfig.java`) par des scripts de migration versionnés (`V1__init_schema.sql`, `V2__seed_data.sql`).
  - Permet une traçabilité rigoureuse des évolutions de schéma en équipe et garantit la reproductibilité en CI/CD.
- **Transition vers un SGBDR d'entreprise** :
  - SQLite est pratique pour un test ou de l'embarqué, mais ne gère pas les accès concurrents intensifs (verrouillage de table).
  - Basculer sur **PostgreSQL** avec un pool de connexions optimisé via **HikariCP** pour la scalabilité horizontale.
- **Pagination et tris des listes** :
  - Remplacer les méthodes retournant des listes complètes (`List<Account>`) par du `Pageable` (`Page<Account>`) avec tri et pagination pour prévenir les risques d'épuisement mémoire (OOM) sur de gros volumes.

### 4. Tests, Qualité & Observabilité
- **Complétude de la couverture de tests** :
  - Ajouter des tests de mutation (**Pitest**) et des tests de charge (Gatling / k6) pour évaluer le comportement sous forte concurrence.
- **Supervision & Métriques de production** :
  - Intégrer `spring-boot-starter-actuator` couplé à **Micrometer / Prometheus** pour exposer des sondes de santé (`/actuator/health`), de readiness/liveness (Kubernetes) et surveiller les temps de réponse de l'API.
- **Documentation d'API vivante (OpenAPI / Swagger UI)** :
  - Intégrer `springdoc-openapi-starter-webmvc-ui` avec configuration OpenAPI 3.0 intégrant la saisie du Bearer JWT directement dans l'interface de test Swagger.


