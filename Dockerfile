# Use a base image with Java 21
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Install required packages for VPN protocols
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openvpn \
    wireguard \
    iptables \
    iproute2 \
    net-tools \
    && rm -rf /var/lib/apt/lists/*

# Copy the JAR file
COPY target/*.jar app.jar

# Create config directories
RUN mkdir -p /app/config/openvpn /app/config/wireguard

# Expose ports
# Spring Boot web server
EXPOSE 8080
# OpenVPN
EXPOSE 1194/udp
# WireGuard
EXPOSE 51820/udp

# Set environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Run with privileged mode for network operations
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
