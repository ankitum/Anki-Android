/****************************************************************************************
 * Copyright (c) 2009 Casey Link <unnamedrambler@gmail.com>                             *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package com.ichi2.anki;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Preferences for the current deck.
 */
public class DeckPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    static final String TAG = "AnkiDroid";

    public class DeckPreferenceHack implements SharedPreferences {

        protected Map<String, String> values = new HashMap<String, String>();


        public DeckPreferenceHack() {
            this.cacheValues();

        }


        protected void cacheValues() {
            Log.i(TAG, "DeckPreferences - CacheValues");
            values.put("newCardsPDay", String.valueOf(AnkiDroidApp.deck().getNewCardsPerDay()));
            values.put("sessionQLimit", String.valueOf(AnkiDroidApp.deck().getSessionRepLimit()));
            values.put("sessionTLimit", String.valueOf(AnkiDroidApp.deck().getSessionTimeLimit() / 60));
            values.put("newCardOrder", String.valueOf(AnkiDroidApp.deck().getNewCardOrder()));
            values.put("newCardSpacing", String.valueOf(AnkiDroidApp.deck().getNewCardSpacing()));
            values.put("revCardOrder", String.valueOf(AnkiDroidApp.deck().getRevCardOrder()));
        }

        public class Editor implements SharedPreferences.Editor {

            public ContentValues update = new ContentValues();


            @Override
            public SharedPreferences.Editor clear() {
                Log.d(TAG, "clear()");
                update = new ContentValues();
                return this;
            }


            @Override
            public boolean commit() {
                Log.d(TAG, "DeckPreferences - commit() changes back to database");

                // make sure we refresh the parent cached values
                // cacheValues();

                for (Entry<String, Object> entry : update.valueSet()) {
                    if (entry.getKey().equals("newCardsPDay")) {
                        AnkiDroidApp.deck().setNewCardsPerDay(Integer.parseInt(entry.getValue().toString()));
                    } else if (entry.getKey().equals("sessionQLimit")) {
                        AnkiDroidApp.deck().setSessionRepLimit(Long.parseLong(entry.getValue().toString()));
                    } else if (entry.getKey().equals("sessionTLimit")) {
                        AnkiDroidApp.deck().setSessionTimeLimit(60 * Long.parseLong(entry.getValue().toString()));
                    } else if (entry.getKey().equals("newCardOrder")) {
                        AnkiDroidApp.deck().setNewCardOrder(Integer.parseInt(entry.getValue().toString()));
                    } else if (entry.getKey().equals("newCardSpacing")) {
                        AnkiDroidApp.deck().setNewCardSpacing(Integer.parseInt(entry.getValue().toString()));
                    } else if (entry.getKey().equals("revCardOrder")) {
                        AnkiDroidApp.deck().setRevCardOrder(Integer.parseInt(entry.getValue().toString()));
                    }
                }
                // make sure we refresh the parent cached values
                cacheValues();

                // and update any listeners
                for (OnSharedPreferenceChangeListener listener : listeners) {
                    listener.onSharedPreferenceChanged(DeckPreferenceHack.this, null);
                }

                return true;
            }


            @Override
            public android.content.SharedPreferences.Editor putBoolean(String key, boolean value) {
                return this.putString(key, Boolean.toString(value));
            }


            @Override
            public android.content.SharedPreferences.Editor putFloat(String key, float value) {
                return this.putString(key, Float.toString(value));
            }


            @Override
            public android.content.SharedPreferences.Editor putInt(String key, int value) {
                return this.putString(key, Integer.toString(value));
            }


            @Override
            public android.content.SharedPreferences.Editor putLong(String key, long value) {
                return this.putString(key, Long.toString(value));
            }


            @Override
            public android.content.SharedPreferences.Editor putString(String key, String value) {
                Log.d(this.getClass().toString(), String.format("Editor.putString(key=%s, value=%s)", key, value));
                update.put(key, value);
                return this;
            }


            @Override
            public android.content.SharedPreferences.Editor remove(String key) {
                Log.d(this.getClass().toString(), String.format("Editor.remove(key=%s)", key));
                update.remove(key);
                return this;
            }

        }


        @Override
        public boolean contains(String key) {
            return values.containsKey(key);
        }


        @Override
        public Editor edit() {
            return new Editor();
        }


        @Override
        public Map<String, ?> getAll() {
            return values;
        }


        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return Boolean.valueOf(this.getString(key, Boolean.toString(defValue)));
        }


        @Override
        public float getFloat(String key, float defValue) {
            return Float.valueOf(this.getString(key, Float.toString(defValue)));
        }


        @Override
        public int getInt(String key, int defValue) {
            return Integer.valueOf(this.getString(key, Integer.toString(defValue)));
        }


        @Override
        public long getLong(String key, long defValue) {
            return Long.valueOf(this.getString(key, Long.toString(defValue)));
        }


        @Override
        public String getString(String key, String defValue) {
            Log.d(this.getClass().toString(), String.format("getString(key=%s, defValue=%s)", key, defValue));

            if (!values.containsKey(key)) {
                return defValue;
            }
            return values.get(key);
        }

        public List<OnSharedPreferenceChangeListener> listeners = new LinkedList<OnSharedPreferenceChangeListener>();


        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            listeners.add(listener);
        }


        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            listeners.remove(listener);
        }

    }

    protected DeckPreferenceHack pref;


    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        Log.d(this.getClass().toString(), String.format("getSharedPreferences(name=%s)", name));
        return pref;
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (AnkiDroidApp.deck() == null) {
            Log.i(TAG, "DeckPreferences - Selected Deck is NULL");
            finish();
        } else {
            // requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

            pref = new DeckPreferenceHack();
            pref.registerOnSharedPreferenceChangeListener(this);

            this.addPreferencesFromResource(R.layout.deck_preferences);
            // this.updateSummaries();
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // update values on changed preference
        // this.updateSummaries();
    }


    protected void updateSummaries() {
        // for all text preferences, set summary as current database value
        for (String key : pref.values.keySet()) {
            Preference pref = this.findPreference(key);
            if (pref == null) {
                continue;
            }
            if (pref instanceof CheckBoxPreference) {
                continue;
            }
            pref.setSummary(this.pref.getString(key, ""));
        }
    }
}
