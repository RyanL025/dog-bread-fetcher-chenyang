package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) {
        if (breed == null || breed.trim().isEmpty()) {
            throw new BreedNotFoundException("breed is null or blank");
        }

        String normalized = breed.trim().toLowerCase(Locale.ROOT);
        String url = "https://dog.ceo/api/breed/" + normalized + "/list";
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()){
            ResponseBody responseBody = response.body();
            int httpCode = response.code();
            if (responseBody == null) {
                throw new BreedNotFoundException(breed);
            }

            String response_string = responseBody.string();
            JSONObject responseJson = new JSONObject(response_string);
            String status = responseJson.optString("status", "");

            if ("success".equalsIgnoreCase(status)) {
                JSONArray message =  responseJson.optJSONArray("message");
                if (message == null) {
                    throw new BreedNotFoundException(breed);
                }
                List<String> breeds = new ArrayList<>(message.length());
                for (int i = 0; i < message.length(); i++) {
                    breeds.add(message.optString(i, ""));
                }
                return breeds;
            }
            else {
                String msg = responseJson.optString("message", "");
                int code = responseJson.optInt("code", httpCode);
                boolean notFound = code == 404 || msg.toLowerCase(Locale.ROOT).contains("breed not found");
                if (notFound) {
                    throw new BreedNotFoundException(msg);
                }
                throw new BreedNotFoundException("HTTP " + code + ": " + msg);
            }
        }catch (IOException | JSONException e) {
            throw new BreedNotFoundException(breed);
        }
    }
}