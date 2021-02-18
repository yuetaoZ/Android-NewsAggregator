package com.example.newsaggregator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final ArrayList<String> currNewsSourceDisplayed = new ArrayList<>();
    private ArrayList<Article> articleList = new ArrayList<>();
    private final List<Source> sourceListData = new ArrayList<>();
    private Menu opt_menu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<Fragment> fragments;
    private MyPageAdapter pageAdapter;
    private ViewPager pager;
    private String currentSource;
    private final HashMap<String, String> Code2Country = new HashMap<>();
    private final HashMap<String, String> Code2Language = new HashMap<>();
    private HashSet<String> topicSet = new HashSet<>();
    private final HashSet<String> countrySet = new HashSet<>();
    private final HashSet<String> languageSet = new HashSet<>();
    private String topicFilter = "all";
    private String countryFilter = "all";
    private String languageFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.drawer_list);

        // Set up the drawer item click callback method
        mDrawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectItem(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
        );

        // Create the drawer toggle
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        fragments = new ArrayList<>();

        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);

        setupHashMaps();

        // Load the data
        if (sourceListData.isEmpty())
            new Thread(new SourceLoader(this)).start();
    }

    private void setupHashMaps() {
        String code, name;
        JSONObject jObjMain;
        JSONArray jObjCountries, jObjLanguages;
        try {
            String jCountryString = readJSONfile(R.raw.country_codes);
            jObjMain = new JSONObject(jCountryString);
            jObjCountries = jObjMain.getJSONArray("countries");

            for (int i = 0; i < jObjCountries.length(); i++) {
                JSONObject jCountry = (JSONObject) jObjCountries.get(i);

                code = jCountry.getString("code");
                name = jCountry.getString("name");

                Code2Country.put(code.toLowerCase(), name);
                countrySet.add(name);
            }

            String jLanguageString = readJSONfile(R.raw.language_codes);
            jObjMain = new JSONObject(jLanguageString);
            jObjLanguages = jObjMain.getJSONArray("languages");

            for (int i = 0; i < jObjLanguages.length(); i++) {
                JSONObject jLanguage = (JSONObject) jObjLanguages.get(i);

                code = jLanguage.getString("code");
                name = jLanguage.getString("name");

                Code2Language.put(code.toLowerCase(), name);
                languageSet.add(name);
            }

        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }

    private String readJSONfile(int codes) {
        InputStream is = getResources().openRawResource(codes);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return writer.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        opt_menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public void setupSources(List<Source> sourceListIn) {
        sourceListData.clear();
        topicSet.clear();


        setTitle("News Aggregator (" + sourceListIn.size() + ")");

        Set<String> topics = new HashSet<>();
        Set<String> countries = new HashSet<>();
        Set<String> languages = new HashSet<>();
        Set<String> sources = new HashSet<>();

        String topic, country, language, source;

        for (Source s: sourceListIn) {
            topic = s.getCategory();
            country = s.getCountry();
            language = s.getLanguage();
            source = s.getId();

            topics.add(topic);
            countries.add(country);
            languages.add(language);
            sources.add(source);
            sourceListData.add(s);
        }

        topicSet = new HashSet<>(topics);
        List<String> topicList = new ArrayList<>(topics);
        List<String> countryCodeList = new ArrayList<>(countries);
        List<String> languageCodeList = new ArrayList<>(languages);
        List<String> sourceList = new ArrayList<>(sources);
        List<String> countryList = new ArrayList<>();
        List<String> languageList = new ArrayList<>();

        for (String s: countryCodeList) {
            countryList.add(Code2Country.getOrDefault(s, "unknown"));
        }

        for (String s: languageCodeList) {
            languageList.add(Code2Language.getOrDefault(s, "unknown"));
        }

        Collections.sort(topicList);
        Collections.sort(countryList);
        Collections.sort(languageList);

        MenuItem topicsItem = opt_menu.getItem(0);
        MenuItem countryItem = opt_menu.getItem(1);
        MenuItem languageItem = opt_menu.getItem(2);

        for (String s: topicList)
            topicsItem.getSubMenu().add(s);

        for (String s: countryList)
            countryItem.getSubMenu().add(s);

        for (String s: languageList)
            languageItem.getSubMenu().add(s);


        currNewsSourceDisplayed.addAll(sourceList);

        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_item, currNewsSourceDisplayed));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private void selectItem(int position) {
        pager.setBackground(null);
        currentSource = currNewsSourceDisplayed.get(position); // need to be id
        new Thread(new CurrSourceLoader(this, currentSource)).start();
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public void setArticles(ArrayList<Article> aListIn) {
        articleList.clear();
        if (aListIn != null) {
            articleList = new ArrayList<>(aListIn);
        } else {
            return;
        }

        setTitle(currentSource);

        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);
        fragments.clear();

        for (int i = 0; i < articleList.size(); i++) {
            fragments.add(
                    ArticleFragment.newInstance(articleList.get(i), i+1, articleList.size()));
        }

        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);

    }

    // You need the 2 below to make the drawer-toggle work properly:

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    // You need the below to open the drawer when the toggle is clicked
    // Same method is called when an options menu item is selected.

    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }

        currNewsSourceDisplayed.clear();
        ArrayList<String> lst = updateCurrSourceDisplayed(item);
        currNewsSourceDisplayed.addAll(lst);

        ((ArrayAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
        setTitle("News Aggregator (" + lst.size() + ")");

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<String> updateCurrSourceDisplayed(MenuItem item) {
        String filter = item.getTitle().toString();
        ArrayList<String> lst = new ArrayList<>();

        if (filter.equals("all")) {
            int itemId = item.getItemId();
            if (itemId == R.id.topicsAll) topicFilter = "all";
            else if (itemId == R.id.countriesAll) countryFilter = "all";
            else if (itemId == R.id.languagesAll) languageFilter = "all";
        } else {
            if (countrySet.contains(filter)) countryFilter = filter;
            else if (languageSet.contains(filter)) languageFilter = filter;
            else if (topicSet.contains(filter)) topicFilter = filter;
        }

        for (Source s: sourceListData) {
            String topic = s.getCategory();
            String country = Code2Country.get(s.getCountry());
            String language = Code2Language.get(s.getLanguage());
            String id = s.getId();

            if (topicFilter.equals("all") || topicFilter.equals(topic)) {
                if (countryFilter.equals("all") || countryFilter.equals(country)) {
                    if (languageFilter.equals("all") || languageFilter.equals(language)) {
                        lst.add(id);
                    }
                }
            }
        }

        return lst;
    }

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;


        MyPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }

    }
}