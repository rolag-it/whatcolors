/*
 * Copyright (C) 2013 Rocco Lagrotteria
 * 
 * This file is part of WhatColors app for Android(tm). 
 *
 *   WhatColors is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   WhatColors is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with WhatColors.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.rolag.whatcolors.ui;

import it.rolag.whatcolors.Constants;
import it.rolag.whatcolors.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Gestisce la schermata delle impostazione in un
 * modo deprecato ma sesplice
 * 
 * 
 * @author Rocco Lagrotteria 
 */
public class SettingsActivity extends PreferenceActivity {
		
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PreferenceManager pmanager = getPreferenceManager();
        pmanager.setSharedPreferencesName(Constants.PREF_NAME);
        pmanager.setSharedPreferencesMode(MODE_PRIVATE);
        
        if(!pmanager.getSharedPreferences().contains(Constants.PRECISION)){
        	pmanager.getSharedPreferences().edit().putString(Constants.PRECISION, getString(R.string.default_precision)).commit();
        }
        
        if(!pmanager.getSharedPreferences().contains(Constants.FIDELITY)){
        	pmanager.getSharedPreferences().edit().putString(Constants.FIDELITY, getString(R.string.default_fidelity)).commit();
        }
        
        addPreferencesFromResource(R.xml.preferences);     
	}

}