# PROARENA — Gaming Tournament Portal

PROARENA is a full-stack gaming portal project. It pairs a React interface with a Java Spring Boot REST API and persistent player and tournament data.

## What the project includes

- Player signup and login with BCrypt password hashing and signed JWT authentication.
- Public tournament list and detail API endpoints.
- Authenticated tournament creation, including optional banner image upload.
- PostgreSQL database for the Docker workflow and file-backed H2 for local backend development.
- React pages for home, login, signup, dashboard, tournament creation and registration, leaderboard, groups, play, gaming gear, business, and more.

The React pages have different levels of functionality. Signup, login, and tournament creation are wired to the backend. Several other pages are currently UI/demo pages; in particular, the leaderboard uses sample data, and tournament registration is not yet backed by a registration API. See [Current scope](#current-scope).

## Technology

| Layer | Technology |
| --- | --- |
| Frontend | React 19, Vite 6, React Router, Axios, Bootstrap |
| Backend | Java 17, Spring Boot 3.4, Spring Web, Spring Security, Spring Data JPA, Bean Validation |
| Database | PostgreSQL 16 (Docker Compose) or H2 (local development) |
| Authentication | BCrypt password hashes and stateless JWT bearer tokens |
| Local environment | Docker Compose, Maven |

## Run the project

### Requirements

- Node.js and npm
- Java 17 and Maven 3.9+
- Docker Desktop for the PostgreSQL and Docker Compose option

### Option 1: Run PostgreSQL and the API with Docker

From the repository root, start the database and backend:

```bash
docker compose up --build
```

Keep that terminal running. In a second terminal, install and start the frontend:

```bash
npm install
npm run dev
```

Open the Vite URL shown in the terminal (normally http://localhost:5173). The API is available at http://localhost:8080.

To stop the containers, press Ctrl+C and run `docker compose down`. The named PostgreSQL volume keeps database data between container restarts. To remove the database volume as well, run `docker compose down -v`.

The Compose file uses local development credentials by default. Set `POSTGRES_PASSWORD` and `JWT_SECRET` in the environment before using this setup outside local development.

### Option 2: Run the backend with H2

Start the API from the backend directory:

```bash
cd backend
mvn spring-boot:run
```

The API starts on port 8080 and creates a file-backed H2 database in `backend/data`. To start the frontend, open a separate terminal at the repository root:

```bash
npm install
npm run dev
```

The H2 console is available at http://localhost:8080/h2-console. Use JDBC URL `jdbc:h2:file:./data/proarena`, username `sa`, and a blank password. The JDBC path is relative to the backend working directory.

### Frontend API URL

The frontend defaults to `http://localhost:8080`. To point it at another API origin, create a root `.env` file (PowerShell: `Copy-Item .env.example .env`) and set:

```dotenv
VITE_API_URL=http://localhost:8080
```

Restart Vite after changing environment variables.

## API reference

Protected endpoints require this header:

```http
Authorization: Bearer <token>
```

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| GET | `/api/health` | Public | Health status and timestamp |
| POST | `/api/auth/signup` | Public | Register a player; returns a token and player profile |
| POST | `/api/auth/login` | Public | Authenticate with email and password |
| GET | `/api/auth/me` | Token | Return the authenticated player profile |
| GET | `/api/tournaments` | Public | List tournaments ordered by start date |
| GET | `/api/tournaments/{id}` | Public | Get one tournament |
| GET | `/api/tournaments/mine` | Token | List tournaments created by the authenticated player |
| POST | `/api/tournaments` | Token | Create a tournament using multipart form data |

Signup request example:

```json
{
  "username": "player-one",
  "email": "player@example.com",
  "password": "choose-a-password",
  "confirmPassword": "choose-a-password"
}
```

Tournament creation accepts multipart fields for `name`, `game`, `startDate` (ISO date, such as `2026-12-15`), `endDate`, `maxParticipants`, `prizePool`, `rules`, `platform`, `registrationFee`, `tournamentType`, `streamUrl`, and optional `bannerImage`. Images are limited to 5 MB and supported image formats.

## Configuration

The backend reads these environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | API port |
| `DATABASE_URL` | File-backed H2 URL | JDBC connection |
| `DATABASE_USERNAME` | `sa` | Database username |
| `DATABASE_PASSWORD` | Empty | Database password |
| `JWT_SECRET` | Development-only fallback | JWT signing key; replace with a private random secret outside local development |
| `FRONTEND_ORIGIN` | `http://localhost:5173` | Allowed frontend origin |

Docker Compose supplies PostgreSQL connection settings to the API. For local use with H2, the defaults work without additional backend configuration.

## Repository layout

```text
src/                              React/Vite application
  routes/                         Frontend page components
backend/
  src/main/java/com/proarena/     Spring Boot API, auth, security, persistence
  src/main/resources/             Backend configuration
  pom.xml                         Maven dependencies and build
docker-compose.yml                PostgreSQL and backend services
.env.example                      Frontend API URL example
```

## Build commands

Run these from the repository root:

```bash
npm install
npm run build
npm run lint
```

Run the backend build from `backend/`:

```bash
mvn verify
```

## Current scope

The API currently persists player accounts and tournaments. Passwords are hashed, and tournament write endpoints require a valid JWT. Tournament banner files are stored in the backend's local `uploads/` directory, so deployments need persistent file storage or an object-storage integration.

This is a portfolio project and is still under development. Payments, password recovery, tournament registration persistence, group management, match results, and a database-backed leaderboard are not implemented yet. Some frontend routes are presentation/demo pages and may use placeholder or sample content. The README describes the current implementation and does not imply those unfinished features are complete.
