package com.example.newsaggregator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class ArticleFragment extends Fragment {

    public ArticleFragment() {
        // Required empty public constructor
    }

    public static ArticleFragment newInstance(Article article,
                                              int index, int max)
    {
        ArticleFragment f = new ArticleFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable("ARTICLE_DATA", article);
        bdl.putSerializable("INDEX", index);
        bdl.putSerializable("TOTAL_COUNT", max);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment_layout = inflater.inflate(R.layout.fragment_article, container, false);

        Bundle args = getArguments();
        if (args != null) {
            final Article currentArticle = (Article) args.getSerializable("ARTICLE_DATA");
            if (currentArticle == null) {
                return null;
            }
            int index = args.getInt("INDEX");
            int total = args.getInt("TOTAL_COUNT");

            TextView headline = fragment_layout.findViewById(R.id.headline);
            headline.setText(currentArticle.getTitle());
            headline.setOnClickListener(v -> openWebsite(currentArticle.getUrl()));

            TextView date = fragment_layout.findViewById(R.id.date);
            String pubDate = currentArticle.getPublishedAt();
            if (!pubDate.isEmpty() && !pubDate.equals("null")) {
                String easyDate = pubDate.substring(0, 10);
                date.setText(easyDate);
            } else {
                date.setText("");
            }


            TextView author = fragment_layout.findViewById(R.id.Author);
            String authorStr = currentArticle.getAuthor();
            if (!authorStr.isEmpty() && !authorStr.equals("null")) {
                author.setText(authorStr);
            } else {
                author.setText("");
            }

            TextView content = fragment_layout.findViewById(R.id.Content);
            String contentStr = currentArticle.getDescription();
            if (!contentStr.isEmpty() && !contentStr.equals("null")) {
                content.setText(contentStr);
            } else {
                content.setText("");
            }
            content.setText(currentArticle.getTitle());
            content.setOnClickListener(v -> openWebsite(currentArticle.getUrl()));

            TextView pageNum = fragment_layout.findViewById(R.id.page_num);
            pageNum.setText(String.format(Locale.US, "%d of %d", index, total));

            ImageView imageView = fragment_layout.findViewById(R.id.imageView);
            imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            imageView.setImageDrawable(currentArticle.getDrawable());
            imageView.setOnClickListener(v -> openWebsite(currentArticle.getUrl()));
            return fragment_layout;
        } else {
            return null;
        }
    }

    public void openWebsite(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(url));
        startActivity(browserIntent);
    }

}
