package com.example.newsaggregator;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;

public class SourceLoader implements Runnable {

    private static final String TAG = "SourceLoader";
    private final MainActivity mainActivity;
    private static final String dataURL = "https://newsapi.org/v2/sources?apiKey=65d5db554f32406b97783439ae2d53cc";

    public SourceLoader(MainActivity ma) {
        mainActivity = ma;
    }

    @Override
    public void run() {

        Uri dataUri = Uri.parse(dataURL);
        String urlToUse = dataUri.toString();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent","");
            conn.connect();

            StringBuilder sb = new StringBuilder();
            String line;

            if (conn.getResponseCode() == HTTP_OK) {
                BufferedReader reader =
                        new BufferedReader((new InputStreamReader(conn.getInputStream())));

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                conn.disconnect();
                List<Source> sourceList = parseJSON(sb.toString());
                if (sourceList != null) {
                    mainActivity.runOnUiThread(() -> mainActivity.setupSources(sourceList));
                }
            } else {
                BufferedReader reader =
                        new BufferedReader((new InputStreamReader(conn.getErrorStream())));

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                conn.disconnect();
                Log.d(TAG, "run: " + sb.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Source> parseJSON(String s) {
        List<Source> sourceList = new ArrayList<>();

        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray jObjSources = jObjMain.getJSONArray("sources");

            for (int i = 0; i < jObjSources.length(); i++) {
                JSONObject jSource = (JSONObject) jObjSources.get(i);

                String id = jSource.getString("id");
                String name = jSource.getString("name");
                String description = jSource.getString("description");
                String url = jSource.getString("url");
                String category = jSource.getString("category");
                String language = jSource.getString("language");
                String country = jSource.getString("country");

                if (id.isEmpty())
                    continue;

                if (name.isEmpty())
                    name = "Unspecified";

                if (description.isEmpty())
                    description = "Unspecified";

                if (url.isEmpty())
                    url = "Unspecified";

                if (category.isEmpty())
                    category = "Unspecified";

                if (language.isEmpty())
                    language = "Unspecified";

                if (country.isEmpty())
                    country = "Unspecified";

                Source newSource = new Source();
                newSource.setId(id);
                newSource.setName(name);
                newSource.setDescription(description);
                newSource.setUrl(url);
                newSource.setCategory(category);
                newSource.setLanguage(language);
                newSource.setCountry(country);

                sourceList.add(newSource);
            }
            return sourceList;
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
