# PROARENA — Full-Stack Gaming Tournament Portal

PROARENA combines a React/Vite client with a Java 17 + Spring Boot REST API and a relational database. Players can create accounts, sign in, browse tournaments, and (when authenticated) create tournaments. The API stores accounts and tournaments in a database, hashes passwords with BCrypt, and protects write operations with signed JWT bearer tokens.

## Stack

- **Frontend:** React 19, Vite, React Router, Axios, Bootstrap
- **Backend:** Java 17, Spring Boot 3.4, Spring Web, Spring Security, Spring Data JPA, Bean Validation
- **Data:** PostgreSQL 16 in Docker Compose; local development also supports file-backed H2
- **Authentication:** BCrypt password hashes and stateless signed JWTs
- **Operations:** Docker Compose, health endpoint, configurable environment variables

## Run locally

Requirements: Node.js 20+, Java 17+, Maven 3.9+. Docker Desktop is needed only for the PostgreSQL workflow.

### Option A — PostgreSQL with Docker

From the repository root:

```bash
docker compose up --build
```

In a second terminal:

```bash
npm install
cp .env.example .env
npm run dev
```

Open http://localhost:5173. The API listens at http://localhost:8080. For Windows PowerShell, use `Copy-Item .env.example .env` in place of `cp`.

### Option B — Local H2 database

Run the frontend as above, then start the API from `backend/`:

```bash
mvn spring-boot:run
```

The default profile creates a local file-backed H2 database under `backend/data`. The H2 console is at http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/proarena`, username `sa`, blank password).

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `VITE_API_URL` | `http://localhost:8080` | Frontend API origin |
| `DATABASE_URL` | `jdbc:h2:file:./data/proarena;MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE` | JDBC connection |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | `sa` / blank | Database credentials |
| `JWT_SECRET` | Development-only value | HMAC signing secret; set a long random secret outside local development |
| `FRONTEND_ORIGIN` | `http://localhost:5173` | Allowed browser origin |

## API

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| GET | `/api/health` | Public | API health and timestamp |
| POST | `/api/auth/signup` | Public | Create account; returns `token` and `player` |
| POST | `/api/auth/login` | Public | Verify credentials; returns `token` and `player` |
| GET | `/api/auth/me` | Bearer token | Current player profile |
| GET | `/api/tournaments` | Public | List tournaments by start date |
| GET | `/api/tournaments/{id}` | Public | Tournament details |
| GET | `/api/tournaments/mine` | Bearer token | Current player's tournaments |
| POST | `/api/tournaments` | Bearer token | Create tournament (multipart form; optional image up to 5 MB) |

For protected requests, send `Authorization: Bearer <token>`. Signup accepts the frontend's `username`, `email`, `password`, and optional `confirmPassword` fields. Passwords must be at least 8 characters.

## Data and scope

JPA persists players and tournaments. Tournament banners are stored in the API's local `uploads/` directory; use persistent storage when deploying. This is a portfolio/demo application: it does not process payments, provide production account recovery, or include tournament registration/match-result workflows yet.

## Project structure

```
src/                         React/Vite frontend
backend/src/main/java/       Spring Boot API, auth, persistence, security
backend/src/main/resources/  application configuration
docker-compose.yml           PostgreSQL + API services
```

## Verify

```bash
# Frontend
npm install
npm run build

# Backend (from backend/)
mvn verify
```

No verification commands are claimed as passing unless run in the target environment.
