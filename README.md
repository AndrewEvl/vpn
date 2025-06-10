# Multi-Protocol VPN Server

A flexible **server-side** VPN application supporting multiple protocols (OpenVPN, WireGuard) to bypass traffic blocking, similar to AmneziaVPN.

## Application Type

This is a **server-side application** that:
- Hosts VPN services for multiple protocols
- Provides a web interface for administration
- Generates client configurations that can be used with standard VPN clients
- Runs on a server with the necessary network privileges

It is not a VPN client application - it does not connect to other VPN servers. Instead, it creates a VPN server that clients can connect to.

## Features

- Support for multiple VPN protocols:
  - OpenVPN
  - WireGuard
- RESTful API for managing VPN connections
- Web interface for administration
- Containerized deployment with Docker
- Configuration persistence
- Auto-start capability
- Client configuration generation

## Requirements

- Java 21 or later
- Docker and Docker Compose (for containerized deployment)
- OpenVPN and WireGuard (if running without Docker)

## Quick Start

### Using Docker Compose (Recommended)

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/vpn.git
   cd vpn
   ```

2. Build the application:
   ```bash
   ./mvnw clean package -DskipTests
   ```

3. Set your server's public IP address:
   ```bash
   export VPN_SERVER_ADDRESS=your_server_ip_address
   ```

4. Start the VPN server:
   ```bash
   docker-compose up -d
   ```

5. Check the logs:
   ```bash
   docker-compose logs -f
   ```

### Manual Deployment

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/vpn.git
   cd vpn
   ```

2. Build the application:
   ```bash
   ./mvnw clean package
   ```

3. Run the application:
   ```bash
   java -jar target/vpn-0.0.1-SNAPSHOT.jar
   ```

## Configuration

### Application Properties

The application can be configured using `application.properties` or environment variables:

```properties
# Server configuration
server.port=8080

# VPN configuration
vpn.server-address=your_server_ip_address
vpn.auto-start-protocols=true

# OpenVPN configuration
vpn.protocols.openvpn.enabled=true
vpn.protocols.openvpn.port=1194
vpn.protocols.openvpn.options.cipher=AES-256-GCM
vpn.protocols.openvpn.options.auth=SHA256

# WireGuard configuration
vpn.protocols.wireguard.enabled=true
vpn.protocols.wireguard.port=51820
vpn.protocols.wireguard.options.persistent-keepalive=25
```

### Environment Variables

When using Docker, you can configure the application using environment variables:

```bash
VPN_SERVER_ADDRESS=your_server_ip_address
VPN_AUTO_START_PROTOCOLS=true
VPN_PROTOCOLS_OPENVPN_ENABLED=true
VPN_PROTOCOLS_OPENVPN_PORT=1194
VPN_PROTOCOLS_WIREGUARD_ENABLED=true
VPN_PROTOCOLS_WIREGUARD_PORT=51820
```

## API Usage

### List All Protocols

```bash
curl -X GET http://localhost:8080/api/vpn/protocols
```

### Start a Protocol

```bash
curl -X POST http://localhost:8080/api/vpn/protocols/openvpn/start
```

### Stop a Protocol

```bash
curl -X POST http://localhost:8080/api/vpn/protocols/openvpn/stop
```

### Check Protocol Status

```bash
curl -X GET http://localhost:8080/api/vpn/protocols/openvpn/status
```

### Generate Client Configuration

```bash
curl -X POST "http://localhost:8080/api/vpn/protocols/openvpn/client-config?username=user1"
```

### Start All Protocols

```bash
curl -X POST http://localhost:8080/api/vpn/protocols/start-all
```

### Stop All Protocols

```bash
curl -X POST http://localhost:8080/api/vpn/protocols/stop-all
```

## Client Setup

### OpenVPN

1. Generate a client configuration:
   ```bash
   curl -X POST "http://localhost:8080/api/vpn/protocols/openvpn/client-config?username=user1" > user1.ovpn
   ```

2. Import the `.ovpn` file into your OpenVPN client.

### WireGuard

1. Generate a client configuration:
   ```bash
   curl -X POST "http://localhost:8080/api/vpn/protocols/wireguard/client-config?username=user1" > user1.conf
   ```

2. Import the `.conf` file into your WireGuard client.

## Security Considerations

- The application requires privileged access to configure network interfaces
- In production, secure the API with authentication and HTTPS
- Regularly update the application and dependencies
- Consider implementing additional security measures like firewall rules

## Troubleshooting

### Common Issues

1. **Ports already in use**: Ensure that ports 1194 and 51820 are not already in use by other services.
2. **Permission denied**: When running without Docker, ensure the application has sufficient privileges.
3. **Connection issues**: Check firewall settings to ensure the VPN ports are open.

### Logs

Check the application logs for detailed error messages:

```bash
docker-compose logs -f
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
