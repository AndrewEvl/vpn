package com.andrewevl.vpn.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the WireGuard protocol.
 */
@Slf4j
@Component
public class WireGuardProtocol extends AbstractProtocol {
    
    private static final String PROTOCOL_NAME = "WireGuard";
    private static final int DEFAULT_PORT = 51820;
    
    private Process serverProcess;
    private final String configDir = "config/wireguard";
    private final String serverConfigPath = configDir + "/wg0.conf";
    private final String privateKeyPath = configDir + "/privatekey";
    private final String publicKeyPath = configDir + "/publickey";
    
    public WireGuardProtocol() {
        super(DEFAULT_PORT);
        initializeConfigDirectory();
    }
    
    public WireGuardProtocol(int port) {
        super(port);
        initializeConfigDirectory();
    }
    
    private void initializeConfigDirectory() {
        try {
            Files.createDirectories(Paths.get(configDir));
            if (!Files.exists(Paths.get(privateKeyPath)) || !Files.exists(Paths.get(publicKeyPath))) {
                generateServerKeys();
            }
            createDefaultServerConfig();
        } catch (IOException e) {
            logger.error("Failed to initialize WireGuard config directory: {}", e.getMessage(), e);
        }
    }
    
    private void generateServerKeys() {
        // In a real implementation, this would execute the WireGuard commands to generate keys
        // For demonstration purposes, we'll simulate it
        
        try {
            // Simulate private key
            String privateKey = UUID.randomUUID().toString().replace("-", "");
            Files.write(Paths.get(privateKeyPath), privateKey.getBytes());
            
            // Simulate public key
            String publicKey = UUID.randomUUID().toString().replace("-", "");
            Files.write(Paths.get(publicKeyPath), publicKey.getBytes());
            
            logger.info("Generated WireGuard server keys");
        } catch (IOException e) {
            logger.error("Failed to generate WireGuard keys: {}", e.getMessage(), e);
        }
    }
    
    private void createDefaultServerConfig() throws IOException {
        Path configPath = Paths.get(serverConfigPath);
        if (!Files.exists(configPath)) {
            String privateKey = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
            
            List<String> lines = new ArrayList<>();
            lines.add("[Interface]");
            lines.add("PrivateKey = " + privateKey);
            lines.add("Address = 10.0.0.1/24");
            lines.add("ListenPort = " + port);
            lines.add("PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE");
            lines.add("PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE");
            
            Files.write(configPath, lines);
            logger.info("Created default WireGuard server configuration");
        }
    }
    
    @Override
    protected void doStart() throws Exception {
        // In a real implementation, this would execute the WireGuard commands to start the interface
        // For demonstration purposes, we'll simulate it
        
        if (!Files.exists(Paths.get(serverConfigPath))) {
            throw new IOException("Server configuration file not found: " + serverConfigPath);
        }
        
        List<String> command = new ArrayList<>();
        command.add("wg-quick");
        command.add("up");
        command.add("wg0");
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        
        // In a real implementation, we would start the process
        // serverProcess = processBuilder.start();
        
        // For demonstration, we'll just log that we would start it
        logger.info("Would execute command: {}", String.join(" ", command));
    }
    
    @Override
    protected void doStop() throws Exception {
        // In a real implementation, this would execute the WireGuard commands to stop the interface
        // For demonstration purposes, we'll simulate it
        
        List<String> command = new ArrayList<>();
        command.add("wg-quick");
        command.add("down");
        command.add("wg0");
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        
        // In a real implementation, we would start the process and wait for it to complete
        // Process process = processBuilder.start();
        // process.waitFor();
        
        // For demonstration, we'll just log that we would execute it
        logger.info("Would execute command: {}", String.join(" ", command));
        
        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroy();
            if (serverProcess.isAlive()) {
                serverProcess.destroyForcibly();
            }
            serverProcess = null;
        }
    }
    
    @Override
    public String getName() {
        return PROTOCOL_NAME;
    }
    
    @Override
    public int getDefaultPort() {
        return DEFAULT_PORT;
    }
    
    @Override
    public String generateClientConfig(String username) {
        try {
            // In a real implementation, we would generate client keys
            // For demonstration, we'll simulate it
            String clientPrivateKey = UUID.randomUUID().toString().replace("-", "");
            String clientPublicKey = UUID.randomUUID().toString().replace("-", "");
            String serverPublicKey = new String(Files.readAllBytes(Paths.get(publicKeyPath)));
            
            // Create client configuration
            List<String> lines = new ArrayList<>();
            lines.add("[Interface]");
            lines.add("PrivateKey = " + clientPrivateKey);
            lines.add("Address = 10.0.0." + (10 + Math.abs(username.hashCode() % 240)) + "/32");
            lines.add("DNS = 8.8.8.8, 8.8.4.4");
            lines.add("");
            lines.add("[Peer]");
            lines.add("PublicKey = " + serverPublicKey);
            lines.add("AllowedIPs = 0.0.0.0/0");
            lines.add("Endpoint = " + (serverAddress != null ? serverAddress : "server_ip_address") + ":" + port);
            lines.add("PersistentKeepalive = 25");
            
            // Save the client config to a file
            String clientConfigPath = configDir + "/" + username + ".conf";
            Files.write(Paths.get(clientConfigPath), lines);
            
            // Add client to server config
            addClientToServerConfig(username, clientPublicKey);
            
            logger.info("Generated WireGuard client configuration for user: {}", username);
            return String.join("\n", lines);
        } catch (IOException e) {
            logger.error("Failed to generate client configuration: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private void addClientToServerConfig(String username, String clientPublicKey) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(serverConfigPath));
        List<String> newLines = new ArrayList<>(lines);
        
        // Check if client already exists
        boolean clientExists = false;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("# " + username)) {
                clientExists = true;
                break;
            }
        }
        
        if (!clientExists) {
            newLines.add("");
            newLines.add("# " + username);
            newLines.add("[Peer]");
            newLines.add("PublicKey = " + clientPublicKey);
            newLines.add("AllowedIPs = 10.0.0." + (10 + Math.abs(username.hashCode() % 240)) + "/32");
            
            Files.write(Paths.get(serverConfigPath), newLines);
            logger.info("Added client {} to WireGuard server configuration", username);
        }
    }
}
