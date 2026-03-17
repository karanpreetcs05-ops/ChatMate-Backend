# Step 1: Build the application using Maven (with a valid image)
FROM maven:3.8.4-openjdk-17 AS build
COPY . .
RUN mvn clean install -DskipTests

# Step 2: Run the application (using Eclipse Temurin - the new standard)
FROM eclipse-temurin:17-jdk-focal
COPY --from=build /target/classes /app/classes
COPY --from=build /target/dependency /app/dependency
WORKDIR /app
CMD ["java", "-cp", "classes:dependency/*", "ChatMate"]