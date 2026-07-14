FROM eclipse-temurin:21-jdk-jammy

# Install Python 3
RUN apt-get update && apt-get install -y python3 && rm -rf /var/lib/apt/lists/*

# Set the working directory inside the container
WORKDIR /app

# Set Python alias
RUN ln -s /usr/bin/python3 /usr/bin/python

# Bake the current build into the image so a fresh `docker build` (and the
# `part_a*` tarball produced from it) always reflects the latest code, not
# whatever happened to be bind-mounted at `docker run` time. Rebuild
# scl-star.jar (see RUN_ON_REMOTE_SERVER.md 0.4) before building this image.
COPY scl-star.jar ./scl-star.jar
COPY libs ./libs
COPY src/test ./src/test
COPY Results ./Results
COPY Configs ./Configs

# Experiment.java writes into Log/** on first run but never creates the
# directories itself — they must already exist.
RUN mkdir -p "Log/Counter Example"

# At `docker run` time, `-v "$(pwd)":/app` (see RUN_ON_REMOTE_SERVER.md A.3)
# overlays these baked-in files with whatever is on the host, and persists
# Results/ back to the host. The COPY above matters for cases where the image
# is used without that bind mount (e.g. distributing part_a* on its own).
CMD ["java", "-cp", "./libs/learnlib-distribution-0.16.0-dependencies-bundle.jar:./libs/opencsv-5.6.jar:./libs/slf4j-jdk14-1.7.36.jar:./libs/commons-cli-1.4.jar:scl-star.jar", "main/Experiment"]
