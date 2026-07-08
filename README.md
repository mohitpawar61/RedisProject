# 🔴 Redis Caching Project

A Spring Boot REST API that demonstrates **Redis as a caching layer** for a Student management system. Built with **Spring Boot 4.1**, **Java 21**, **Spring Data Redis**, and **Docker Compose** for Redis. Showcases all three Spring Cache annotations — `@CachePut`, `@Cacheable`, and `@CacheEvict` — with measurable caching speedup (100ms → ~1ms on repeated reads).

---

## 📌 Table of Contents

- [Overview](#-overview)
- [What is Redis (simple explanation)](#-what-is-redis-simple-explanation)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Cache Annotations Explained](#-cache-annotations-explained)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [API Endpoints](#-api-endpoints)
- [Request & Response Examples](#-request--response-examples)
- [Redis CLI — Useful Commands](#-redis-cli--useful-commands)
- [Configuration](#-configuration)
- [How the Fake Database Works](#-how-the-fake-database-works)
- [Author](#-author)

---

## 📖 Overview

This project teaches Redis caching using a simple Student CRUD API. Instead of a real database, it uses a **ConcurrentHashMap with a 100ms artificial delay** to simulate DB latency — making the cache speedup obvious and measurable.

The focus is entirely on demonstrating three Spring Cache concepts:
- How to cache a result when you write data (`@CachePut`)
- How to serve cached data on repeated reads (`@Cacheable`)
- How to remove stale data from the cache on delete (`@CacheEvict`)

Redis is configured with a **10-minute TTL**, **JSON serialization** for human-readable values, and **String keys** so you can inspect the cache with `redis-cli KEYS *`.

---

## 🧠 What is Redis (Simple Explanation)

Imagine your app is a waiter at a restaurant. A customer asks "what's today's special?" The waiter walks to the kitchen (database), asks the chef, gets the answer, and walks back. That takes 100ms.

Now imagine the waiter writes the answer on a sticky note and keeps it in their pocket. Next time someone asks the same question, they read from the pocket in ~1ms. **That pocket is Redis.**

Redis is an **in-memory key-value store** — it lives in RAM, not on disk — so it's ~100x faster than a database query.

```
First request  → DB query       → ~100ms
Repeat request → Redis cache hit → ~1ms
Speed gain: 100×
```

---

## ✨ Features

- ⚡ Redis caching with 10-minute TTL — responses go from 100ms to ~1ms
- 📝 `@CachePut` — write to DB and update cache simultaneously
- 🔍 `@Cacheable` — serve from cache, skip DB on cache hits
- 🗑️ `@CacheEvict` — remove stale entries when data is deleted
- 🔌 Startup Redis connection check — logs "Redis Connection Successfully" or "Redis Connection Failed"
- 💾 JSON-serialized cache values — human-readable with `redis-cli GET key`
- 🐳 Redis 7 via Docker Compose — one command to start (with appendonly persistence)
- 📚 Swagger UI at `/swagger-ui/index.html` — all endpoints documented and testable
- 📊 DEBUG logging — see every CACHE MISS, DB query, and CACHE HIT in logs
- 🔒 Thread-safe in-memory DB — `ConcurrentHashMap` for concurrent request safety

---

## 🛠 Tech Stack

| Layer              | Technology                                  |
|--------------------|---------------------------------------------|
| Language           | Java 21                                     |
| Framework          | Spring Boot 4.1                             |
| Caching            | Spring Cache abstraction + `@EnableCaching` |
| Cache Store        | Redis 7 (via Docker)                        |
| Redis Client       | Lettuce (default in spring-data-redis)      |
| Serialization      | `GenericJackson2JsonRedisSerializer` (JSON) |
| API Docs           | Springdoc OpenAPI 2.3 (Swagger UI)          |
| Build              | Maven                                       |
| Container          | Docker + Docker Compose                     |

---

## 🏗 Architecture

```
Client (Browser / Postman / curl)
            │
            │  HTTP REST
            ▼
┌──────────────────────────────┐
│     StudentController         │   @RestController, Swagger @Operation annotations
│     /api/students/**          │
└──────────────┬───────────────┘
               │
               │  Spring AOP Proxy intercepts call
               ▼
┌──────────────────────────────┐
│   Spring Cache (AOP Proxy)    │   Reads/writes Redis before calling real service
│   @CachePut / @Cacheable /    │
│   @CacheEvict                 │
└──────────────┬───────────────┘
               │  Cache MISS → calls real method
               ▼
┌──────────────────────────────┐
│     StudentService            │   Business logic + SLF4J logging of cache events
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐     ┌────────────────────────────┐
│     StudentDatabase           │     │   Redis (Docker)            │
│     (ConcurrentHashMap)       │     │   localhost:6379            │
│     Thread.sleep(100ms)       │     │   TTL: 10 minutes           │
│     simulates DB latency      │     │   Keys: String              │
└──────────────────────────────┘     │   Values: JSON              │
                                      └────────────────────────────┘
```

---

## 📁 Project Structure

```
RedisProject/
├── docker-compose.yml                          # Redis 7 alpine, port 6379, appendonly + healthcheck
├── pom.xml                                     # spring-data-redis + springdoc-openapi
└── src/
    ├── main/
    │   ├── java/com/cfs/redisproject/
    │   │   ├── RedisProjectApplication.java    # @SpringBootApplication entry point
    │   │   ├── config/
    │   │   │   ├── RedisConfig.java            # CacheManager bean — TTL, key/value serializers
    │   │   │   └── RedisConnectionCheck.java   # Pings Redis on startup, logs result
    │   │   ├── controller/
    │   │   │   └── StudentController.java      # 5 REST endpoints with @Operation Swagger docs
    │   │   ├── entity/
    │   │   │   └── Student.java                # POJO: id (Long), name, email (implements Serializable)
    │   │   ├── repo/
    │   │   │   └── StudentDatabase.java        # ConcurrentHashMap + Thread.sleep(100) per method
    │   │   └── service/
    │   │       └── StudentService.java         # @CachePut, @Cacheable, @CacheEvict annotations
    │   └── resources/
    │       └── application.properties          # Redis host/port + DEBUG logging
    └── test/
        └── java/com/cfs/redisproject/
            └── RedisProjectApplicationTests.java
```

---

## 🔑 Cache Annotations Explained

### `@CachePut` — on `createStudent()`

```java
@CachePut(value = "students", key = "#result.id")
public Student createStudent(Student student) throws InterruptedException {
    Student std = studentDatabase.save(student);  // always runs, saves to DB
    return std;                                    // result stored in Redis
}
```

**Always executes the method AND stores the result.** Never skips the DB call. Use for write operations where you want to simultaneously update the cache.

- Cache key: `students::1` (where 1 is the returned student's ID)
- SpEL expression `#result.id` extracts the ID from the returned object

---

### `@Cacheable` — on `getStudentByEmail()`

```java
@Cacheable(value = "studentByEmail", key = "#email")
public List<Student> getStudentByEmail(String email) throws InterruptedException {
    log.info("CACHE MISS: fetching data from DB {}", email);  // only logs on miss
    List<Student> byEmail = studentDatabase.findByEmail(email);
    return byEmail;
}
```

**Checks cache first. Skips the method body entirely on a cache hit.**

- First call: cache miss → DB queried (100ms) → result stored in Redis → returned
- Subsequent calls: cache hit → returned from Redis (~1ms) → method body never runs
- Proof: the log line "CACHE MISS" only appears on first call for each email

---

### `@CacheEvict` — on delete operations

```java
// Clears ALL entries from both cache namespaces
@CacheEvict(value = {"student", "studentByEmail"}, allEntries = true)
public void deleteAllStudents() throws InterruptedException { ... }

// Clears only the specific student's cache entry
@CacheEvict(value = "student", key = "#id")
public void deleteStudentById(Long id) throws InterruptedException { ... }
```

**Removes cache entries after the method runs.** Prevents stale data after deletion.

- `allEntries = true` — clears the entire cache namespace
- `key = "#id"` — removes only that specific entry

---

### No annotation — `getAllStudents()`

```java
// No @Cacheable — always queries the DB
public List<Student> getAllStudents() throws InterruptedException {
    return studentDatabase.findAll();
}
```

Deliberately not cached. A "get all" list changes every time a student is created or deleted — caching it would require evicting it on every write, which eliminates the benefit.

---

### Cache key patterns in Redis

| Cache name        | Key pattern              | Example key                          |
|-------------------|--------------------------|--------------------------------------|
| `students`        | `students::<id>`         | `students::5`                        |
| `studentByEmail`  | `studentByEmail::<email>`| `studentByEmail::mohit@test.com`     |

---

## 📋 Prerequisites

- **Java 21+** — [adoptium.net](https://adoptium.net/)
- **Maven 3.8+**
- **Docker Desktop** — [docker.com](https://docker.com/)

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/mohitpawar61/RedisProject.git
cd RedisProject
```

### 2. Start Redis

```bash
docker-compose up -d

# Verify Redis is running and healthy
docker ps
# Should show: redis-demo   Up (healthy)

# Test the connection manually
docker exec redis-demo redis-cli ping
# Expected output: PONG
```

### 3. Run the Spring Boot app

```bash
mvn clean install
mvn spring-boot:run
```

Watch for this line in the startup logs:
```
INFO  Redis Connection Successfully
```

### 4. Open Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

### 5. Test caching (see the speedup in action)

```bash
# Step 1: Create a student
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"Mohit Pawar","email":"mohit@test.com"}'

# Step 2: Search by email — FIRST CALL (cache miss, 100ms)
curl "http://localhost:8080/api/students/search/email?email=mohit@test.com"

# Step 3: Same search — REPEAT CALL (cache hit, ~1ms)
curl "http://localhost:8080/api/students/search/email?email=mohit@test.com"

# Step 4: Check what's in Redis
docker exec redis-demo redis-cli KEYS "*"
```

In the app logs you'll see:
- First call → `CACHE MISS: fetching data from DB`
- Second call → No "CACHE MISS" message (served from Redis)

---

## 📡 API Endpoints

| Method   | Endpoint                               | Cache Annotation                      | Description              |
|----------|----------------------------------------|---------------------------------------|--------------------------|
| `POST`   | `/api/students`                        | `@CachePut(students, key=result.id)`  | Create student           |
| `GET`    | `/api/students`                        | None                                  | Get all (no caching)     |
| `GET`    | `/api/students/search/email?email=`    | `@Cacheable(studentByEmail, key=email)`| Search by email         |
| `DELETE` | `/api/students`                        | `@CacheEvict(allEntries=true)`        | Delete all students      |
| `DELETE` | `/api/students/{id}`                   | `@CacheEvict(key=id)`                 | Delete by ID             |

---

## 📝 Request & Response Examples

### Create a student

```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"Mohit Pawar","email":"mohit@test.com"}'

# Response 201 Created
{"id":1,"name":"Mohit Pawar","email":"mohit@test.com"}
```

### Search by email

```bash
# First call (cache miss — logs "CACHE MISS", takes 100ms)
curl "http://localhost:8080/api/students/search/email?email=mohit@test.com"

# Response 200 OK
[{"id":1,"name":"Mohit Pawar","email":"mohit@test.com"}]

# Second call (cache hit — no "CACHE MISS" log, takes ~1ms)
curl "http://localhost:8080/api/students/search/email?email=mohit@test.com"
```

### Get all students

```bash
curl http://localhost:8080/api/students
# Always queries DB, no caching
```

### Delete by ID

```bash
curl -X DELETE http://localhost:8080/api/students/1
# 204 No Content — also evicts students::1 from Redis
```

---

## 🔧 Redis CLI — Useful Commands

```bash
# See all cached keys
docker exec redis-demo redis-cli KEYS "*"

# Get a cached value (stored as JSON)
docker exec redis-demo redis-cli GET "studentByEmail::mohit@test.com"

# Check TTL remaining on a key (in seconds, -1 = no expiry, -2 = doesn't exist)
docker exec redis-demo redis-cli TTL "studentByEmail::mohit@test.com"

# Delete a specific key manually
docker exec redis-demo redis-cli DEL "studentByEmail::mohit@test.com"

# Clear all Redis data
docker exec redis-demo redis-cli FLUSHALL

# Monitor ALL Redis commands in real time (great for debugging)
docker exec redis-demo redis-cli MONITOR

# Enter interactive Redis CLI
docker exec -it redis-demo redis-cli
```

---

## 🔧 Configuration

### `application.properties`

```properties
spring.application.name=RedisProject

# Redis connection
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=60000

# Enable DEBUG logging to see all cache events
logging.level.root=DEBUG
```

### `RedisConfig.java` — what each setting does

| Setting                              | Value           | Effect                                              |
|--------------------------------------|-----------------|-----------------------------------------------------|
| `entryTtl`                           | 10 minutes      | Cache entries auto-expire after 10 min              |
| Key serializer: `StringRedisSerializer` | String        | Keys stored as readable strings (`students::1`)     |
| Value serializer: `GenericJackson2JsonRedisSerializer` | JSON | Values stored as JSON (human-readable)   |
| `disableCachingNullValues()`         | Enabled         | Null results are never stored in Redis              |

### `docker-compose.yml` explained

```yaml
services:
  redis:
    image: redis:7-alpine          # Redis 7 on minimal Alpine (~10MB)
    container_name: redis-demo
    ports:
      - "6379:6379"                # Expose Redis to localhost
    volumes:
      - redis-data:/data           # Persist data across container restarts
    command: redis-server --appendonly yes  # AOF persistence — survives crashes
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]   # Monitor health every 10s
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped        # Auto-restart if Redis crashes
```

---

## 🗄 How the Fake Database Works

`StudentDatabase` is a `@Repository` backed by `ConcurrentHashMap<Long, Student>`:

```java
private final Map<Long, Student> database = new ConcurrentHashMap<>();

public Student save(Student student) throws InterruptedException {
    Thread.sleep(100);              // Simulates DB network + disk latency
    database.put(student.getId(), student);
    return student;
}
```

Every method has `Thread.sleep(100)` to simulate the 100ms latency of a real database query. Without this delay, the HashMap returns so fast that caching would appear to make no difference. With it, you can clearly observe:

- Without cache → every call takes 100ms
- With cache → first call 100ms, all subsequent calls ~1ms

`ConcurrentHashMap` is used instead of `HashMap` because multiple HTTP threads can call `save()` and `findAll()` simultaneously — a regular HashMap would corrupt under concurrent access.

---

## 🛑 Stopping Redis

```bash
# Stop Redis container (data preserved in Docker volume)
docker-compose down

# Stop AND delete all Redis data (volume deleted)
docker-compose down -v
```

---

## 👨‍💻 Author

**Mohit Pawar**
- GitHub: [@mohitpawar61](https://github.com/mohitpawar61)

---

## 📄 License

This project is for educational and development purposes.
