package me.grayingout;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Scanner;

import io.github.cdimascio.dotenv.Dotenv;
import me.grayingout.discord.OAuth2;
import me.grayingout.discord.UserAccessToken;

/**
 * Main class
 */
public class App {
    /**
     * Load the .env file
     */
    private static final Dotenv env = Dotenv.configure().ignoreIfMissing().load();

    /**
     * The redirect URI for the client
     */
    public static final String redirectURI = "http://localhost";

    /**
     * The client id of the client
     */
    public static final String clientId = env.get("client-id");

    /**
     * The client secret of the client
     */
    public static final String clientSecret = env.get("client-secret");

    /**
     * Main method
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        ///// Attempt to get a saved token
        UserAccessToken savedToken = UserAccessToken.fromFile(new File("access_token"));
        if (savedToken != null) {
            System.out.println(savedToken);
            return;
        }
        
        ///// Get a new token

        // Create the oauth URI to get a code
        String accessCodeURI = OAuth2.createAccessCodeURI(clientId, "identify", redirectURI);

        // Print the URI
        System.out.println(accessCodeURI);
        
        // Read in the access code
        String accessCode = "";
        
        System.out.printf("Enter code: ");
        try (Scanner scanner = new Scanner(System.in)) {
            accessCode = scanner.nextLine();
        } catch (NoSuchElementException e) {
            System.out.println("\nError: No code provided");
            return;
        }

        // Get the user access token from the oauth access code
        UserAccessToken token = OAuth2.exchangeAccessCode(accessCode);
        System.out.println(token);

        // Save the user access token
        token.saveToFile(new File("access_token"));
    }
}
