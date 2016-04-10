package com.michiganhackers.diabeticons.Core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.gson.Gson;
import com.michiganhackers.diabeticons.IconListChangeSubscriber;
import com.michiganhackers.diabeticons.Icon;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jawad on 22/10/15.
 */
public class MyApplication extends Application {
    private final static String LOGTAG = "MD/MyApplicaiton";

    private ArrayList<Icon> mIconList;
    private ArrayList<Integer> mRecentList;
    private IconListChangeSubscriber mFavSubscriber = null;

    private static final String PREFS_NAME = "LIST_PREFS";
    private static final String KEY_FAVORITES = "Favorites";
    private static final String KEY_RECENTS = "Recents";

    private static final int RECENT_LIST_LIMIT = 10;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create all the icons for the rest of the entire application to use
        generateAllIcons();
    }

    public void restoreListData() {
        SharedPreferences settings;
        settings = getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);

        // If there was a stored recent list before, use that
        if(settings.contains(KEY_FAVORITES)) {
            List favsList;
            String jsonFavorites = settings.getString(KEY_FAVORITES, null);
            Gson favsGson = new Gson();
            Integer[] favItems = favsGson.fromJson(jsonFavorites, Integer[].class);
            if(favItems != null) {
                // Converting into a list for ease of use
                    // Doing twice into an ArrayList as a (most likely over) precaution
                favsList = Arrays.asList(favItems);
                ArrayList<Integer> favsListArrayList = new ArrayList<Integer>(favsList);

                // Mark every favorite item as favorite in main list of icons
                for(int i = 0; i < favsList.size(); i++) {
                    int pos = favsListArrayList.get(i);
                    mIconList.get(pos).setIsFavorite(true);
                }
            }
        }

        // If there was a stored recent list before, use that
        if(settings.contains(KEY_RECENTS)) {
            List recentList;
            String jsonRecents = settings.getString(KEY_RECENTS, null);
            Gson recentsGson = new Gson();
            Integer[] recentItems = recentsGson.fromJson(jsonRecents, Integer[].class);
            if(recentItems != null) {
                recentList = Arrays.asList(recentItems);
                mRecentList= new ArrayList<Integer>(recentList);
            }
        }
    }

    public void saveListData() {
        // Initialize shared prefs
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        // Get list of favorites and store into gson
        Gson favsGson = new Gson();
        ArrayList<Integer> favList = new ArrayList<>();
        for(int i = 0; i < mIconList.size(); ++i) {
            if(mIconList.get(i).getIsFavorite()) {
                favList.add(i);
            }
        }
        String jsonFavorites = favsGson.toJson(favList);

        // Get list of recents and store into gson
        Gson recentsGson = new Gson();
        String jsonRecents = recentsGson.toJson(mRecentList);

        // Store gson versions of lists into shared prefs
        editor.putString(KEY_FAVORITES, jsonFavorites);
        editor.putString(KEY_RECENTS, jsonRecents);

        editor.commit();
    }

    private void generateAllIcons() {
        mIconList = new ArrayList<>();
        mRecentList = new ArrayList<>();

        // Get all the necessary images and titles
        AssetManager assetManager = getAssets(); // Necessary to access assets
        try {
            // Get all the file names
            String[] files = assetManager.list("diabeticons"); // "diabeticons" = path

            // Save all the names and images
            for(int i = 0; i < files.length; ++i) {
                // Create the new icon object for this round
                Icon curIcon = new Icon();

                // Cache the image for this item from its file path
                String path = "diabeticons/" + files[i];
                Drawable d = Drawable.createFromStream(assetManager.open(path), null);
                curIcon.setImage(d);

                // Cache the path, for later use
                curIcon.setPath(path);

                // Get the simplified, displayable name (no .filetype)
                String simplerName = files[i].substring(0, files[i].indexOf('.'));

                // Save the easier-to-read title
                curIcon.setTitle(simplerName);

                // Finally put the icon into the list for later use
                mIconList.add(curIcon);
            }

        } catch (IOException e) {
            Log.e(LOGTAG, "There was an error! Error: " + e.toString());
            e.printStackTrace();
        }
    }

    public ArrayList<Icon> getAllIcons() {
        return mIconList;
    }

    public Icon getIcon(int index) {
        return mIconList.get(index);
    }

    public ArrayList<Integer> getRecentList() { return mRecentList; }

    public void addRecentItem(int index) {
        // Check if the index already exists in the list- if so, remove it
        for(int i = 0; i < mRecentList.size(); i++) {
            if(mRecentList.get(i) == index) {
                mRecentList.remove(i);

                break;
            }
        }

        // Add the item to the beginning of the list
        mRecentList.add(0, index);

        // If list is above the max size, remove the last item
            // Safety net while loop
        while(mRecentList.size() > RECENT_LIST_LIMIT) {
            mRecentList.remove(mRecentList.size()-1); // Remove last recent item
        }
    }

    public void setFavoriteState(int index, boolean value) {
        // Change the actual stored value
        this.mIconList.get(index).setIsFavorite(value);

        // Notify the subscriber, if valid
        if(mFavSubscriber != null) {
            mFavSubscriber.favoriteStatusChanged();
        }
    }

    public void setFavoriteChangeNotifier(IconListChangeSubscriber subscriber) {
        this.mFavSubscriber = subscriber;
    }

    public void removeFavoriteChangeNotifier() {
        this.mFavSubscriber = null;
    }
}
