package tw.app.uniload.downloaders;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tw.app.uniload.R;

public class Instagram {
    private final static String TAG = "InstagramAPI";

    private SharedPreferences apiInfo;
    private SharedPreferences settings;

    public Instagram(@NonNull Context context, @NonNull String url, @NonNull OnDataReceived listener) throws IOException {
        OkHttpClient client = new OkHttpClient();

        apiInfo = context.getSharedPreferences("apiInfo", Activity.MODE_PRIVATE);
        settings = context.getSharedPreferences("settings", Activity.MODE_PRIVATE);

        Request request = new Request.Builder()
                .url("https://instagram-media-downloader.p.rapidapi.com/rapid/post.php?url=" + URLEncoder.encode(url, "ISO-8859-1"))
                .get()
                .addHeader("X-RapidAPI-Key", settings.getString("apikey", context.getString(R.string.rapidapi_key_one)))
                .addHeader("X-RapidAPI-Host", "instagram-media-downloader.p.rapidapi.com")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() -> listener.OnError(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();

                apiInfo.edit().putString("requestsLimit", response.headers().get("x-ratelimit-requests-limit").toString()).apply();
                apiInfo.edit().putString("requestsRemaining", response.headers().get("x-ratelimit-requests-remaining").toString()).apply();
                apiInfo.edit().putString("requestsReset", response.headers().get("x-ratelimit-requests-reset").toString()).apply();

                HashMap<String, Object> responseMap = new Gson().fromJson(responseBody, HashMap.class);
                String caption = responseMap.get("caption").toString();
                responseMap.remove("caption");

                Log.d(TAG, "Response: " + responseBody);
                Log.d(TAG, "Map Size: " + responseMap.size());

                // (-1) because response contains post description.
                List<String> imagesUrlList = new ArrayList<>();
                if (responseMap.size() == 1) {
                    imagesUrlList.add(responseMap.get("image").toString());
                } else {
                    for (int i = 0; responseMap.size() > i; i++) {
                        Log.d(TAG, "Getting Object From Map at: " + i);
                        try {
                            imagesUrlList.add(responseMap.get(String.valueOf(i)).toString());
                        } catch (Exception ex){
                            try {
                                imagesUrlList.add(responseMap.get(String.valueOf("image")).toString());
                            } catch (Exception exception){
                                imagesUrlList.add(responseMap.get(String.valueOf("video")).toString());
                            }
                        }
                        Log.d(TAG, "Successfully added from position: " + i);
                    }
                }

                Log.d(TAG, "Images Count: " + imagesUrlList.size());

                ((Activity) context).runOnUiThread(() -> listener.OnReceived(imagesUrlList, caption));
            }
        });
    }

    public interface OnDataReceived {
        void OnReceived(List<String> imagesUrlList, String postDescription);

        void OnError(String reason);
    }
}