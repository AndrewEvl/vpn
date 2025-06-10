package com.andrewevl.vpn.config;

import com.andrewevl.vpn.model.Protocol;
import com.andrewevl.vpn.service.VpnService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Initializes VPN protocols with configuration from application properties.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VpnConfigInitializer {
    
    private final VpnConfig vpnConfig;
    private final VpnService vpnService;
    
    /**
     * Initialize protocols with configuration after bean construction.
     */
    @PostConstruct
    public void initializeProtocols() {
        log.info("Initializing VPN protocols with configuration");
        
        Map<String, Protocol> protocols = vpnService.getAllProtocols();
        
        // Apply configuration to each protocol
        for (Map.Entry<String, Protocol> entry : protocols.entrySet()) {
            String protocolName = entry.getKey();
            Protocol protocol = entry.getValue();
            
            // Get protocol-specific configuration
            VpnConfig.ProtocolConfig protocolConfig = vpnConfig.getProtocols().get(protocolName);
            if (protocolConfig == null) {
                log.warn("No configuration found for protocol: {}", protocolName);
                continue;
            }
            
            // Set server address
            try {
                Method setServerAddressMethod = protocol.getClass().getMethod("setServerAddress", String.class);
                setServerAddressMethod.invoke(protocol, vpnConfig.getServerAddress());
                log.debug("Set server address for {}: {}", protocolName, vpnConfig.getServerAddress());
            } catch (Exception e) {
                log.warn("Failed to set server address for protocol {}: {}", protocolName, e.getMessage());
            }
            
            // Set port
            if (protocolConfig.getPort() > 0) {
                try {
                    Method setPortMethod = protocol.getClass().getMethod("setPort", int.class);
                    setPortMethod.invoke(protocol, protocolConfig.getPort());
                    log.debug("Set port for {}: {}", protocolName, protocolConfig.getPort());
                } catch (Exception e) {
                    log.warn("Failed to set port for protocol {}: {}", protocolName, e.getMessage());
                }
            }
            
            // Apply additional options if supported by the protocol
            for (Map.Entry<String, String> option : protocolConfig.getOptions().entrySet()) {
                String optionName = option.getKey();
                String optionValue = option.getValue();
                
                // Convert option name to setter method name (e.g., "cipher" -> "setCipher")
                String setterName = "set" + optionName.substring(0, 1).toUpperCase() + optionName.substring(1).replace("-", "");
                
                try {
                    // Try to find a setter method for this option
                    Method setterMethod = findSetterMethod(protocol.getClass(), setterName);
                    if (setterMethod != null) {
                        Class<?> paramType = setterMethod.getParameterTypes()[0];
                        Object convertedValue = convertValue(optionValue, paramType);
                        setterMethod.invoke(protocol, convertedValue);
                        log.debug("Set option {} for {}: {}", optionName, protocolName, optionValue);
                    } else {
                        log.debug("No setter method found for option: {}", optionName);
                    }
                } catch (Exception e) {
                    log.warn("Failed to set option {} for protocol {}: {}", optionName, protocolName, e.getMessage());
                }
            }
        }
        
        // Auto-start protocols if configured
        if (vpnConfig.isAutoStartProtocols()) {
            log.info("Auto-starting VPN protocols");
            Map<String, Boolean> results = vpnService.startAllProtocols();
            results.forEach((name, success) -> {
                if (success) {
                    log.info("Successfully started protocol: {}", name);
                } else {
                    log.error("Failed to start protocol: {}", name);
                }
            });
        }
    }
    
    /**
     * Find a setter method with the given name, trying different parameter types.
     */
    private Method findSetterMethod(Class<?> clazz, String setterName) {
        // Try common parameter types
        Class<?>[] commonTypes = {String.class, int.class, boolean.class, long.class};
        
        for (Class<?> type : commonTypes) {
            try {
                return clazz.getMethod(setterName, type);
            } catch (NoSuchMethodException e) {
                // Continue to next type
            }
        }
        
        return null;
    }
    
    /**
     * Convert a string value to the target type.
     */
    private Object convertValue(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        }
        
        throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getName());
    }
}
