package me.grayingout.discord;

/**
 * Used to detect the code from a access code server
 */
@FunctionalInterface
public interface OAuth2AccessCodeHandler {
    
    /**
     * Called when a new request comes in
     * 
     * @param code The code from the request
     */
    void onCode(String code);
}
