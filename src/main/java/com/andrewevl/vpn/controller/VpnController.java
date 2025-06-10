package com.andrewevl.vpn.controller;

import com.andrewevl.vpn.model.Protocol;
import com.andrewevl.vpn.service.VpnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for managing VPN protocols and connections.
 */
@Slf4j
@RestController
@RequestMapping("/api/vpn")
@RequiredArgsConstructor
public class VpnController {
    
    private final VpnService vpnService;
    
    /**
     * Get all available protocols and their status.
     * 
     * @return a map of protocol names to their status
     */
    @GetMapping("/protocols")
    public ResponseEntity<Map<String, Object>> getAllProtocols() {
        Map<String, Protocol> protocols = vpnService.getAllProtocols();
        
        Map<String, Object> result = protocols.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Protocol protocol = entry.getValue();
                            Map<String, Object> protocolInfo = new HashMap<>();
                            protocolInfo.put("name", protocol.getName());
                            protocolInfo.put("running", protocol.isRunning());
                            protocolInfo.put("port", protocol.getDefaultPort());
                            return protocolInfo;
                        }
                ));
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Start a protocol server.
     * 
     * @param name the protocol name
     * @return the result of the operation
     */
    @PostMapping("/protocols/{name}/start")
    public ResponseEntity<Map<String, Object>> startProtocol(@PathVariable String name) {
        log.info("Starting protocol: {}", name);
        boolean success = vpnService.startProtocol(name);
        
        Map<String, Object> result = new HashMap<>();
        result.put("protocol", name);
        result.put("action", "start");
        result.put("success", success);
        
        if (success) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * Stop a protocol server.
     * 
     * @param name the protocol name
     * @return the result of the operation
     */
    @PostMapping("/protocols/{name}/stop")
    public ResponseEntity<Map<String, Object>> stopProtocol(@PathVariable String name) {
        log.info("Stopping protocol: {}", name);
        boolean success = vpnService.stopProtocol(name);
        
        Map<String, Object> result = new HashMap<>();
        result.put("protocol", name);
        result.put("action", "stop");
        result.put("success", success);
        
        if (success) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * Get the status of a protocol server.
     * 
     * @param name the protocol name
     * @return the status of the protocol
     */
    @GetMapping("/protocols/{name}/status")
    public ResponseEntity<Map<String, Object>> getProtocolStatus(@PathVariable String name) {
        boolean running = vpnService.isProtocolRunning(name);
        
        Map<String, Object> result = new HashMap<>();
        result.put("protocol", name);
        result.put("running", running);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Generate a client configuration for a protocol.
     * 
     * @param name the protocol name
     * @param username the username for the client
     * @return the client configuration
     */
    @PostMapping("/protocols/{name}/client-config")
    public ResponseEntity<Map<String, Object>> generateClientConfig(
            @PathVariable String name,
            @RequestParam String username) {
        
        log.info("Generating client configuration for protocol {} and user {}", name, username);
        String config = vpnService.generateClientConfig(name, username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("protocol", name);
        result.put("username", username);
        
        if (config != null) {
            result.put("config", config);
            return ResponseEntity.ok(result);
        } else {
            result.put("error", "Failed to generate client configuration");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * Start all protocol servers.
     * 
     * @return the result of the operation for each protocol
     */
    @PostMapping("/protocols/start-all")
    public ResponseEntity<Map<String, Object>> startAllProtocols() {
        log.info("Starting all protocols");
        Map<String, Boolean> results = vpnService.startAllProtocols();
        
        Map<String, Object> result = new HashMap<>();
        result.put("action", "start-all");
        result.put("results", results);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Stop all protocol servers.
     * 
     * @return the result of the operation for each protocol
     */
    @PostMapping("/protocols/stop-all")
    public ResponseEntity<Map<String, Object>> stopAllProtocols() {
        log.info("Stopping all protocols");
        Map<String, Boolean> results = vpnService.stopAllProtocols();
        
        Map<String, Object> result = new HashMap<>();
        result.put("action", "stop-all");
        result.put("results", results);
        
        return ResponseEntity.ok(result);
    }
}
