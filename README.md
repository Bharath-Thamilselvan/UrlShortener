Url-Shortener
A simple URL Shortener web application built using HTML, CSS, JavaScript, and Thymeleaf for the frontend. The backend is developed with Java, Spring Boot, and Spring Data JPA, with PostgreSQL used as the database. The application also integrates Spring Security for authentication and secure access control.

🚀 Features

🔐 Secure authentication and authorization using Spring Security

🔗 Generate short URLs from long links

📊 Manage and track created URLs

⏳ Support for URL expiration

🌐 Public and private URL access

📄 Pagination and sorting for performance optimization

⚡ Fast and reliable redirection

🛠️ Tech Stack

Frontend:

HTML, CSS, JavaScript

Thymeleaf

Backend:

Java

Spring Boot

Spring Data JPA

Database:

PostgreSQL

Security:

Spring Security

🧠 Key Highlights

Clean layered architecture (Controller → Service → Repository)

Efficient handling of expired and invalid URLs

Optimized database queries with pagination

Production-level coding practices and structure

📦 Setup Instructions

Clone the repository
git clone https://github.com/Bharath-Thamilselvan/Url-Shortener.git

Navigate to project
cd url-shortener

Run the application
./mvnw spring-boot:run

Update your application.properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/urlshortener spring.datasource.username=your_username spring.datasource.password=your_password spring.jpa.hibernate.ddl-auto=update

📌 Use Case

This application provides a simple and secure way to shorten long URLs, manage them, and access them efficiently—similar to platforms like Bitly.
