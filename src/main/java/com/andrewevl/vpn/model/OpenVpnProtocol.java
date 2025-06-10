package com.andrewevl.vpn.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the OpenVPN protocol.
 */
@Slf4j
@Component
public class OpenVpnProtocol extends AbstractProtocol {
    
    private static final String PROTOCOL_NAME = "OpenVPN";
    private static final int DEFAULT_PORT = 1194;
    
    private Process serverProcess;
    private final String configDir = "config/openvpn";
    private final String serverConfigPath = configDir + "/server.conf";
    private final String clientConfigTemplate = configDir + "/client-template.conf";
    
    public OpenVpnProtocol() {
        super(DEFAULT_PORT);
        initializeConfigDirectory();
    }
    
    public OpenVpnProtocol(int port) {
        super(port);
        initializeConfigDirectory();
    }
    
    private void initializeConfigDirectory() {
        try {
            Files.createDirectories(Paths.get(configDir));
            createDefaultServerConfig();
            createClientConfigTemplate();
        } catch (IOException e) {
            logger.error("Failed to initialize OpenVPN config directory: {}", e.getMessage(), e);
        }
    }
    
    private void createDefaultServerConfig() throws IOException {
        Path configPath = Paths.get(serverConfigPath);
        if (!Files.exists(configPath)) {
            List<String> lines = new ArrayList<>();
            lines.add("port " + port);
            lines.add("proto udp");
            lines.add("dev tun");
            lines.add("ca ca.crt");
            lines.add("cert server.crt");
            lines.add("key server.key");
            lines.add("dh dh.pem");
            lines.add("server 10.8.0.0 255.255.255.0");
            lines.add("ifconfig-pool-persist ipp.txt");
            lines.add("push \"redirect-gateway def1 bypass-dhcp\"");
            lines.add("push \"dhcp-option DNS 8.8.8.8\"");
            lines.add("push \"dhcp-option DNS 8.8.4.4\"");
            lines.add("keepalive 10 120");
            lines.add("cipher AES-256-GCM");
            lines.add("user nobody");
            lines.add("group nogroup");
            lines.add("persist-key");
            lines.add("persist-tun");
            lines.add("status openvpn-status.log");
            lines.add("verb 3");
            
            Files.write(configPath, lines);
            logger.info("Created default OpenVPN server configuration");
        }
    }
    
    private void createClientConfigTemplate() throws IOException {
        Path templatePath = Paths.get(clientConfigTemplate);
        if (!Files.exists(templatePath)) {
            List<String> lines = new ArrayList<>();
            lines.add("client");
            lines.add("dev tun");
            lines.add("proto udp");
            lines.add("remote {{SERVER_ADDRESS}} " + port);
            lines.add("resolv-retry infinite");
            lines.add("nobind");
            lines.add("persist-key");
            lines.add("persist-tun");
            lines.add("remote-cert-tls server");
            lines.add("cipher AES-256-GCM");
            lines.add("verb 3");
            lines.add("key-direction 1");
            lines.add("<ca>");
            lines.add("{{CA_CERTIFICATE}}");
            lines.add("</ca>");
            lines.add("<cert>");
            lines.add("{{CLIENT_CERTIFICATE}}");
            lines.add("</cert>");
            lines.add("<key>");
            lines.add("{{CLIENT_KEY}}");
            lines.add("</key>");
            lines.add("<tls-auth>");
            lines.add("{{TLS_AUTH_KEY}}");
            lines.add("</tls-auth>");
            
            Files.write(templatePath, lines);
            logger.info("Created OpenVPN client configuration template");
        }
    }
    
    @Override
    protected void doStart() throws Exception {
        // In a real implementation, this would execute the OpenVPN server process
        // For demonstration purposes, we'll simulate it
        
        File configFile = new File(serverConfigPath);
        if (!configFile.exists()) {
            throw new IOException("Server configuration file not found: " + serverConfigPath);
        }
        
        List<String> command = new ArrayList<>();
        command.add("openvpn");
        command.add("--config");
        command.add(serverConfigPath);
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        
        // In a real implementation, we would start the process
        // serverProcess = processBuilder.start();
        
        // For demonstration, we'll just log that we would start it
        logger.info("Would execute command: {}", String.join(" ", command));
    }
    
    @Override
    protected void doStop() throws Exception {
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
            String template = new String(Files.readAllBytes(Paths.get(clientConfigTemplate)));
            
            // In a real implementation, we would generate certificates and keys
            // For demonstration, we'll use placeholders
            String clientId = UUID.randomUUID().toString();
            
            template = template.replace("{{SERVER_ADDRESS}}", serverAddress != null ? serverAddress : "server_ip_address");
            template = template.replace("{{CA_CERTIFICATE}}", "# CA Certificate would be here");
            template = template.replace("{{CLIENT_CERTIFICATE}}", "# Client Certificate for " + username + " would be here");
            template = template.replace("{{CLIENT_KEY}}", "# Client Key for " + username + " would be here");
            template = template.replace("{{TLS_AUTH_KEY}}", "# TLS Auth Key would be here");
            
            // Save the client config to a file
            String clientConfigPath = configDir + "/" + username + ".ovpn";
            Files.write(Paths.get(clientConfigPath), template.getBytes());
            
            logger.info("Generated OpenVPN client configuration for user: {}", username);
            return template;
        } catch (IOException e) {
            logger.error("Failed to generate client configuration: {}", e.getMessage(), e);
            return null;
        }
    }
}
