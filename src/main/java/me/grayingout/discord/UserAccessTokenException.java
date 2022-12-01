package me.grayingout.discord;

/**
 * A generic exception for errors regarding
 * user access tokens
 */
public class UserAccessTokenException extends RuntimeException {
    
    /**
     * Constructs a new {@code UserAccessTokenException} with the
     * provided error message
     * 
     * @param message The error message
     */
    public UserAccessTokenException(String message) {
        super(message);
    }
}
