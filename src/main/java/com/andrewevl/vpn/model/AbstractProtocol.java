package com.andrewevl.vpn.model;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for VPN protocol implementations.
 * Provides common functionality for all protocols.
 */
public abstract class AbstractProtocol implements Protocol {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Getter
    @Setter
    protected int port;
    
    @Getter
    @Setter
    protected String serverAddress;
    
    @Getter
    protected boolean running = false;
    
    /**
     * Constructor with default port.
     */
    public AbstractProtocol() {
        this.port = getDefaultPort();
    }
    
    /**
     * Constructor with custom port.
     * 
     * @param port the port to use for this protocol
     */
    public AbstractProtocol(int port) {
        this.port = port;
    }
    
    /**
     * Constructor with custom port and server address.
     * 
     * @param port the port to use for this protocol
     * @param serverAddress the server address to bind to
     */
    public AbstractProtocol(int port, String serverAddress) {
        this.port = port;
        this.serverAddress = serverAddress;
    }
    
    @Override
    public boolean startServer() {
        logger.info("Starting {} server on port {}", getName(), port);
        try {
            doStart();
            running = true;
            logger.info("{} server started successfully", getName());
            return true;
        } catch (Exception e) {
            logger.error("Failed to start {} server: {}", getName(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean stopServer() {
        logger.info("Stopping {} server", getName());
        try {
            doStop();
            running = false;
            logger.info("{} server stopped successfully", getName());
            return true;
        } catch (Exception e) {
            logger.error("Failed to stop {} server: {}", getName(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Implementation-specific start logic.
     * 
     * @throws Exception if the server fails to start
     */
    protected abstract void doStart() throws Exception;
    
    /**
     * Implementation-specific stop logic.
     * 
     * @throws Exception if the server fails to stop
     */
    protected abstract void doStop() throws Exception;
}
