package com.example.newsaggregator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    //My API Key: 65d5db554f32406b97783439ae2d53cc
    private static final String TAG = "MainActivity";
    private final ArrayList<String> newsSourceDisplayed = new ArrayList<>();
    private final List<Source> sourceListData = new ArrayList<>();
    private Menu opt_menu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<Fragment> fragments;
    private MyPageAdapter pageAdapter;
    private ViewPager pager;
    private String currentSource;
    public static int screenWidth, screenHeight;
    private final HashMap<String, String> Code2Country = new HashMap<>();
    private final HashMap<String, String> Code2Language = new HashMap<>();

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

                Code2Country.put(code, name);
            }

            String jLanguageString = readJSONfile(R.raw.language_codes);
            jObjMain = new JSONObject(jLanguageString);
            jObjLanguages = jObjMain.getJSONArray("languages");

            for (int i = 0; i < jObjLanguages.length(); i++) {
                JSONObject jLanguage = (JSONObject) jObjLanguages.get(i);

                code = jLanguage.getString("code");
                name = jLanguage.getString("name");

                Code2Language.put(code, name);
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        opt_menu = menu;
        return true;
    }

    private void selectItem(int position) {
    }

    public void setupSources(List<Source> sourceListIn) {
        sourceListData.clear();

        Set<String> topics = new HashSet<>();
        Set<String> countries = new HashSet<>();
        Set<String> languages = new HashSet<>();

        String topic, country, language;

        for (Source s: sourceListIn) {
            topic = s.getCategory();
            country = s.getCountry();
            language = s.getLanguage();

            topics.add(topic);
            countries.add(country);
            languages.add(language);
        }

        setupHashMaps();

        List<String> topicList = new ArrayList<>(topics);
        List<String> countryCodeList = new ArrayList<>(countries);
        List<String> languageCodeList = new ArrayList<>(languages);
        List<String> countryList = new ArrayList<>();
        List<String> languageList = new ArrayList<>();

        for (String s: countryCodeList) {
            countryList.add(Code2Country.getOrDefault(s, "unkown"));
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