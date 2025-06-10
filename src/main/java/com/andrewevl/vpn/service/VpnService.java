package com.andrewevl.vpn.service;

import com.andrewevl.vpn.model.Protocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing VPN protocols and connections.
 */
@Slf4j
@Service
public class VpnService {
    
    private final Map<String, Protocol> protocols;
    
    @Autowired
    public VpnService(List<Protocol> protocolList) {
        protocols = new HashMap<>();
        protocolList.forEach(protocol -> protocols.put(protocol.getName().toLowerCase(), protocol));
        log.info("VPN Service initialized with {} protocols: {}", protocols.size(), 
                protocols.keySet());
    }
    
    /**
     * Get a protocol by name.
     * 
     * @param name the protocol name
     * @return the protocol, or empty if not found
     */
    public Optional<Protocol> getProtocol(String name) {
        return Optional.ofNullable(protocols.get(name.toLowerCase()));
    }
    
    /**
     * Get all available protocols.
     * 
     * @return a map of protocol names to protocol instances
     */
    public Map<String, Protocol> getAllProtocols() {
        return new HashMap<>(protocols);
    }
    
    /**
     * Start a protocol server.
     * 
     * @param name the protocol name
     * @return true if the server was started successfully, false otherwise
     */
    public boolean startProtocol(String name) {
        return getProtocol(name)
                .map(Protocol::startServer)
                .orElseGet(() -> {
                    log.error("Protocol not found: {}", name);
                    return false;
                });
    }
    
    /**
     * Stop a protocol server.
     * 
     * @param name the protocol name
     * @return true if the server was stopped successfully, false otherwise
     */
    public boolean stopProtocol(String name) {
        return getProtocol(name)
                .map(Protocol::stopServer)
                .orElseGet(() -> {
                    log.error("Protocol not found: {}", name);
                    return false;
                });
    }
    
    /**
     * Check if a protocol server is running.
     * 
     * @param name the protocol name
     * @return true if the server is running, false otherwise or if the protocol is not found
     */
    public boolean isProtocolRunning(String name) {
        return getProtocol(name)
                .map(Protocol::isRunning)
                .orElseGet(() -> {
                    log.error("Protocol not found: {}", name);
                    return false;
                });
    }
    
    /**
     * Generate a client configuration for a protocol.
     * 
     * @param protocolName the protocol name
     * @param username the username for the client
     * @return the client configuration, or null if the protocol is not found
     */
    public String generateClientConfig(String protocolName, String username) {
        return getProtocol(protocolName)
                .map(protocol -> protocol.generateClientConfig(username))
                .orElseGet(() -> {
                    log.error("Protocol not found: {}", protocolName);
                    return null;
                });
    }
    
    /**
     * Start all protocol servers.
     * 
     * @return a map of protocol names to their start status
     */
    public Map<String, Boolean> startAllProtocols() {
        Map<String, Boolean> results = new HashMap<>();
        protocols.forEach((name, protocol) -> {
            boolean success = protocol.startServer();
            results.put(name, success);
        });
        return results;
    }
    
    /**
     * Stop all protocol servers.
     * 
     * @return a map of protocol names to their stop status
     */
    public Map<String, Boolean> stopAllProtocols() {
        Map<String, Boolean> results = new HashMap<>();
        protocols.forEach((name, protocol) -> {
            boolean success = protocol.stopServer();
            results.put(name, success);
        });
        return results;
    }
}
