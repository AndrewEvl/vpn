package com.andrewevl.vpn.model;

/**
 * Interface representing a VPN protocol.
 * Each protocol implementation should provide methods for starting, stopping,
 * and managing the VPN connection.
 */
public interface Protocol {
    
    /**
     * Start the VPN protocol server.
     * 
     * @return true if the server was started successfully, false otherwise
     */
    boolean startServer();
    
    /**
     * Stop the VPN protocol server.
     * 
     * @return true if the server was stopped successfully, false otherwise
     */
    boolean stopServer();
    
    /**
     * Check if the server is currently running.
     * 
     * @return true if the server is running, false otherwise
     */
    boolean isRunning();
    
    /**
     * Get the name of the protocol.
     * 
     * @return the protocol name
     */
    String getName();
    
    /**
     * Get the default port for this protocol.
     * 
     * @return the default port number
     */
    int getDefaultPort();
    
    /**
     * Generate client configuration for this protocol.
     * 
     * @param username the username for the client
     * @return the client configuration as a string
     */
    String generateClientConfig(String username);
}
