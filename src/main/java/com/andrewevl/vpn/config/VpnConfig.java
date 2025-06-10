package com.andrewevl.vpn.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for the VPN application.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "vpn")
public class VpnConfig {
    
    /**
     * The server's public IP address or hostname.
     */
    private String serverAddress;
    
    /**
     * Whether to start all protocols automatically on application startup.
     */
    private boolean autoStartProtocols = false;
    
    /**
     * Configuration for each protocol.
     */
    private Map<String, ProtocolConfig> protocols = new HashMap<>();
    
    /**
     * Configuration for a specific protocol.
     */
    @Data
    public static class ProtocolConfig {
        /**
         * Whether this protocol is enabled.
         */
        private boolean enabled = true;
        
        /**
         * The port to use for this protocol.
         */
        private int port;
        
        /**
         * Additional protocol-specific configuration options.
         */
        private Map<String, String> options = new HashMap<>();
    }
}
