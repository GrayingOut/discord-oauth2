package me.grayingout.discord;

import java.io.IOException;

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
 * Discord oauth2 manager class
 */
public final class OAuth2 {
    
    /**
     * Creates the URI required to allow for granting an access code, which
     * can then be exchange for a user access token for access to the API
     * 
     * @param clientId    The id of the client
     * @param scope       The scope of the access code
     * @param redirectURI The redirect URI of the client
     * @return
     */
    public static final String createAccessCodeURI(String clientId, String scope, String redirectURI) {
        return String.format(
            "https://discord.com/oauth2/authorize?response_type=code&client_id=%s&scope=%s&redirect_uri=%s",
            clientId,
            scope,
            redirectURI);
    }

    /**
     * Exchange an obtained access code for a user
     * access token
     * 
     * @param code The access code
     * @return The user access token object
     */
    public static UserAccessToken exchangeAccessCode(String code) {
        
        // Create the form body data
        RequestBody body = new FormBody.Builder()
            .add("client_id", App.clientId)
            .add("client_secret", App.clientSecret)
            .add("code", code)
            .add("grant_type", "authorization_code")
            .add("redirect_uri", App.redirectURI)
            .build();
            
            // Create the request
            Request request = new Request.Builder()
            .url("https://discord.com/api/v10/oauth2/token")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .post(body)
            .build();
            
            OkHttpClient client = new OkHttpClient();
        
        // Send the request, get the response and
        // parse it into a JSON object
        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                throw new UserAccessTokenException("Failed to get user access token: API Endpoint returned status code " + response.code());
            }
            
            JSONObject json = (JSONObject) new JSONParser().parse(response.body().string());
            return new UserAccessToken(
                (String) json.get("access_token"),
                (String) json.get("refresh_token"),
                (String) json.get("scope"),
                (long) json.get("expires_in")
            );
        } catch (IOException | ParseException e) {
            throw new UserAccessTokenException("Failed to get user access token: " + e.getMessage());
        }
    }
}
