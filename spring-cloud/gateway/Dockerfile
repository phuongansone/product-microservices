# we use a multi-stage build, meaning that there is a first step, named builder, that handles the extraction
FROM eclipse-temurin:21.0.1_12-jre-jammy as builder
WORKDIR extracted
ADD ./build/libs/*-SNAPSHOT.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# The second stage builds the actual Docker image that will be used at runtime,
# picking the files as required from the first stage
FROM eclipse-temurin:21.0.1_12-jre-jammy
WORKDIR application
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./

EXPOSE 8761

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]