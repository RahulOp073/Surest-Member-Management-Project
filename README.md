Surest Member Management is a backend project built to handle member information in a clean, secure, and organized way.
It uses Spring Boot and PostgreSQL to store and manage data, and it protects everything with JWT-based login so only the right
people can access the system.
There are two types of users in the system:
Admins, who can add, edit, delete, and view members
Regular users, who can only view members
The project includes a members API that supports filtering, sorting, pagination, and caching so repeated requests are faster. 
The database is set up with Flyway (or basic SQL scripts) and starts with a default admin and a normal user already created.
To make sure the application works correctly, the project includes unit and integration tests using JUnit and Mockito, and tracks 
test coverage with JaCoCo. The aim is to keep code quality high with over 80% coverage.
Overall, this project shows how to build a secure, well-structured, and tested Spring Boot application with real-world 
features like authentication, role-based access, caching, and database migrations.