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

import static java.net.HttpURLConnection.HTTP_OK;

public class CurrSourceLoader implements Runnable{
    private static final String TAG = "CurrSourceLoader";
    private final MainActivity mainActivity;
    private final String selectedSource;
    private ArrayList<Article> aList = new ArrayList<>();

    private static final String dataURL = "https://newsapi.org/v2/top-headlines?sources=";
    private static final String apiKey= "2e237bfc14ec4dabb390e9120b0c836a";

    CurrSourceLoader(MainActivity ma, String selectedSource) {
        mainActivity = ma;
        this.selectedSource = selectedSource;
    }

    public void run() {

        String combinedUrl = dataURL + selectedSource + "&apiKey=" + apiKey;
        Uri dataUri = Uri.parse(combinedUrl);
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
                aList = parseJSON(sb.toString());
                mainActivity.runOnUiThread(() -> mainActivity.setArticles(aList));

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

    private ArrayList<Article> parseJSON(String s) {

        ArrayList<Article> articleList = new ArrayList<>();
        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray jObjArticles = jObjMain.getJSONArray("articles");

            for (int i = 0; i < jObjArticles.length(); i++) {
                JSONObject jArticle = (JSONObject) jObjArticles.get(i);

                JSONObject jSource = jArticle.getJSONObject("source");
                String id = jSource.getString("id");
                String name = jSource.getString("name");
                String author = jArticle.getString("author");
                String title = jArticle.getString("title");
                String description = jArticle.getString("description");
                String url = jArticle.getString("url");
                String urlToImage = jArticle.getString("urlToImage");
                String publishedAt = jArticle.getString("publishedAt");
                String content = jArticle.getString("content");


                if (author.isEmpty())
                    author = "Unspecified";

                if (!id.equals(selectedSource))
                    continue;

                articleList.add(
                        new Article(id, name, author, title, description, url, urlToImage, publishedAt,
                                content, null));

            }
            return articleList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
