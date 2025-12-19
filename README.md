# Surest Member Management Project

## 🔍 Overview
This is a Spring Boot–based Member Management System that provides secured REST APIs for creating and managing members with authentication and role-based authorization.

## ✨ Features
- 👤 Member CRUD operations
- 🔐 JWT-based Authentication & Authorization
- 🎯 Role-based access control (USER / ADMIN)
- 🧪 Integration & Unit Test Coverage
- 🛡️ Global Exception Handling
- 🗄️ Database versioning with Flyway
- 🐳 Docker support

## 🚀 Tech Stack
| Layer | Technology |
|-------|------------|
| Backend | Spring Boot, Spring Security |
| Database | PostgreSQL |
| ORM | Spring Data JPA, Hibernate |
| Auth | JWT Token Security |
| Migration | Flyway |
| Build Tool | Gradle |
| Testing | JUnit, Mockito |

## 📁 Project Structure
src/
├─ main/java/org/surest/
│ ├─ controller/
│ ├─ dto/
│ ├─ entity/
│ ├─ exception/
│ ├─ repository/
│ ├─ security/
│ ├─ service/
│ └─ SurestApplication.java
└─ test/java/org/surest/
## 🔑 User Roles
| Role | Description |
|------|-------------|
| ADMIN | Can Create / Update / Delete / View Members |
| USER | Can only View members |
Run the project
./gradlew bootRun

Execute Tests
./gradlew test

🧪 Default Credentials for Testing
Username	Password	Role
admin	admin123	ADMIN
user	user123	USER
