package me.grayingout.discord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.grayingout.App;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Holds the data for a discord user access token
 * with utility methods for refreshing, saving,
 * and retrieving
 */
public class UserAccessToken implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * The access token
     */
    private String accessToken;

    /**
     * The token used to refresh the access token
     */
    private String refreshToken;
    
    /**
     * The datetime when the access token expires
     */
    private LocalDateTime expiration;

    /**
     * The scope of the token
     */
    private String scope;

    /**
     * Constructs a new {@code UserAccessToken}
     * 
     * @param accessToken         The access token
     * @param refreshToken        The refresh token
     * @param scope               The scope of the token
     * @param expiresInSeconds    The seconds after which the access token expires
     */
    public UserAccessToken(String accessToken, String refreshToken, String scope, long expiresInSeconds) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.expiration = LocalDateTime.now().plus(expiresInSeconds, ChronoUnit.SECONDS);
    }

    /**
     * Gets the access token
     * 
     * @return The access token
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * Gets the scope of the user access token
     * 
     * @return The scope
     */
    public String getScope() {
        return this.scope;
    }

    /**
     * Gets the access token in the form {@code Bearer <token>}
     * 
     * @return The formatted access token
     */
    public String getFormattedAccessToken() {
        return "Bearer " + accessToken;
    }

    /**
     * Refreshes the access token
     * 
     * @throws UserAccessTokenException If an error occurs refreshing the token
     */
    public void refresh() throws UserAccessTokenException {
        // Build body
        RequestBody body = new FormBody.Builder()
            .add("client_id", App.clientId)
            .add("client_secret", App.clientSecret)
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .build();

        // Build request
        Request request = new Request.Builder()
            .url("https://discord.com/api/v10/oauth2/token")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .post(body)
            .build();
        
        OkHttpClient client = new OkHttpClient();

        // Send the request
        try (Response response = client.newCall(request).execute()) {
            // Check for success
            if (response.code() < 200 || response.code() > 200) {
                throw new UserAccessTokenException("Failed to refresh token: API Endpoint returned status code " + response.code());
            }

            // Parse response
            JSONObject json = (JSONObject) new JSONParser().parse(response.body().string());

            // Update data
            this.accessToken = (String) json.get("access_token");
            this.refreshToken = (String) json.get("refresh_token");
            this.expiration = LocalDateTime.now().plus((long) json.get("expires_in"), ChronoUnit.SECONDS);
        } catch (IOException | ParseException e) {
            throw new UserAccessTokenException("Failed to refresh token: " + e.getMessage());
        }
    }

    /**
     * Gets the date when the token expires
     * 
     * @return The expiration date
     */
    public LocalDateTime getExpiration() {
        return expiration;
    }

    /**
     * Returns if the access token has expired
     * 
     * @return If the access token has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().compareTo(expiration) > 0;
    }

    /**
     * Saves the user access token to a file for
     * retrieval
     * 
     * @param file The file to write it to
     */
    public void saveToFile(File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a user access token from a file and returns a new
     * {@code UserAccessToken} or {@code null} if the file
     * does not exist or an error occurs
     * 
     * @param file The file
     * @return The constructed user access token or {@code null} if the file does not exist
     */
    public static UserAccessToken fromFile(File file) {
        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ooi = new ObjectInputStream(new FileInputStream(file))) {
            return (UserAccessToken) ooi.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserAccessToken[");

        builder.append("access_token=" + accessToken + ", ");
        builder.append("refresh_token=" + refreshToken + ", ");
        builder.append("scope=" + scope + ", ");
        builder.append("expires=" + expiration);

        builder.append("]");
        return builder.toString();
    }
}
