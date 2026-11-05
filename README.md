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
