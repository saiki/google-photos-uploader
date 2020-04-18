package jp.saiki;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.gax.rpc.ApiException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.proto.BatchCreateMediaItemsResponse;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.proto.NewMediaItemResult;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.library.v1.upload.UploadMediaItemResponse.Error;
import com.google.photos.library.v1.util.NewMediaItemFactory;
import com.google.photos.types.proto.MediaItem;
import com.google.rpc.Code;
import com.google.rpc.Status;

import jp.saiki.factories.PhotosLibraryClientFactory;

/**
 * Hello world!
 *
 */
public class App {

    private static final List<String> REQUIRED_SCOPES = ImmutableList.of(
            "https://www.googleapis.com/auth/photoslibrary.readonly",
            "https://www.googleapis.com/auth/photoslibrary.appendonly");

    private static final int BATCH_CREATION_LIMIT = 50;

    private static final java.io.File DATA_STORE_DIR =
        new java.io.File(System.getProperty("user.home"), ".store/google_photos_uploader");

    public static void main(final String... args) {
        var credential = Paths.get(System.getProperty("credential"));
        var recursive = Boolean.getBoolean("recursive");
        var depth = recursive ? Integer.MAX_VALUE : 1;
        if (args.length < 2) {
          throw new IllegalArgumentException();
        }
        var user = args[0];
        var root = Paths.get(args[1]);
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            try (PhotosLibraryClient client = PhotosLibraryClientFactory.createClient(user, credential, REQUIRED_SCOPES, httpTransport, dataStoreFactory)) {
                List<String> uploadTokens = Files.walk(root, depth).filter( path -> Files.isRegularFile(path) ).map( f -> {
                    try {
                        return upload(client, f);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toList());
                List<MediaItem> created = createMediaItem(client, uploadTokens);
                System.out.println(created);
                for (MediaItem item : created) {
                  System.out.println(item);
                  System.out.println(item.getFilename());
                  System.out.println(item.getProductUrl());
                }
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        } catch(GeneralSecurityException | IOException e) {
            e.printStackTrace();;
        }
    }

    public static String upload(PhotosLibraryClient client, Path imagePath) throws FileNotFoundException {
        // Create a new upload request
        // Specify the filename that will be shown to the user in Google Photos
        // and the path to the file that will be uploaded
        UploadMediaItemRequest uploadRequest =
            UploadMediaItemRequest.newBuilder()
                //filename of the media item along with the file extension
                .setFileName(imagePath.toFile().getName())
                .setDataFile(new RandomAccessFile(imagePath.toFile(), "r"))
                .build();
        // Upload and capture the response
        UploadMediaItemResponse uploadResponse = client.uploadMediaItem(uploadRequest);
        if (uploadResponse.getError().isPresent()) {
            // If the upload results in an error, handle it
            Error error = uploadResponse.getError().get();
            throw (ApiException) error.getCause();
        }
        return uploadResponse.getUploadToken().get();
    }

    public static List<MediaItem> createMediaItem(PhotosLibraryClient client, List<String> uploadTokens) {
        List<NewMediaItem> newItems = uploadTokens.stream().map(token -> NewMediaItemFactory.createNewMediaItem(token)).collect(Collectors.toList());
        List<List<NewMediaItem>> partitionNewItems = Lists.partition(newItems, BATCH_CREATION_LIMIT);
        List<MediaItem> result = new ArrayList<>(newItems.size());
        for (List<NewMediaItem> items : partitionNewItems) {
          BatchCreateMediaItemsResponse response = client.batchCreateMediaItems(items);
          for (NewMediaItemResult itemsResponse : response.getNewMediaItemResultsList()) {
              Status status = itemsResponse.getStatus();
              if (status.getCode() == Code.OK_VALUE) {
                  // The item is successfully created in the user's library
                  result.add(itemsResponse.getMediaItem());
              } else {
              // The item could not be created. Check the status and try again
              }
          }
        }
        return result;
    }
}
