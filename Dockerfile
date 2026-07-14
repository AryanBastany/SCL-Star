FROM eclipse-temurin:21-jdk-jammy

# Install Python 3
RUN apt-get update && apt-get install -y python3 && rm -rf /var/lib/apt/lists/*

# Set the working directory inside the container
WORKDIR /app

# Expose the entire project directory as a volume
# VOLUME [""]

# Set Python alias
RUN ln -s /usr/bin/python3 /usr/bin/python

CMD ["java", "-cp", "./libs/learnlib-distribution-0.16.0-dependencies-bundle.jar:./libs/opencsv-5.6.jar:./libs/slf4j-jdk14-1.7.36.jar:./libs/commons-cli-1.4.jar:scl-star.jar", "main/Experiment"]
