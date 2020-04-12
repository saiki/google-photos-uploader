package jp.saiki.factories;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;

/**
 * A factory class that helps initialize a {@link PhotosLibraryClient} instance.
 */
public class PhotosLibraryClientFactory {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private PhotosLibraryClientFactory() {
  }

  public static PhotosLibraryClient createClient(Path clientSecretJson, List<String> selectedScopes,
      HttpTransport httpTransport, FileDataStoreFactory dataStoreFactory) throws IOException, GeneralSecurityException {

    Credential credential = authorize(clientSecretJson, selectedScopes, httpTransport, dataStoreFactory);
    AccessToken accessToken = new AccessToken(credential.getAccessToken(), new Date(credential.getExpirationTimeMilliseconds()));
    Credentials credentials = GoogleCredentials.newBuilder().setAccessToken(accessToken).build();
    PhotosLibrarySettings settings = PhotosLibrarySettings.newBuilder()
        .setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
    return PhotosLibraryClient.initialize(settings);
  }

  private static Credential authorize(Path credential, List<String> selectedScopes, HttpTransport httpTransport,
      FileDataStoreFactory dataStoreFactory) throws IOException, GeneralSecurityException {
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
        new InputStreamReader(new FileInputStream(credential.toFile())));
    // set up authorization code flow
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY,
        clientSecrets, selectedScopes).setDataStoreFactory(dataStoreFactory).build();
    // authorize
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }
}
