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
package it.rolag.whatcolors.tools;

import java.util.List;

import it.rolag.whatcolors.Constants;
import it.rolag.whatcolors.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Gestisce la semplice business-logic che governa le operazioni  
 * CRUD sui colori preferiti.
 * Strato di persistenza gentilmente offerto da {@link SharedPreferences} 
 * 
 * @author Rocco Lagrotteria
 *
 */
public final class FavoritesManager {
	private final static String SEPARATOR = ":";
	private final static String EMPTY = "";
	
	private FavoritesManager(){};
	
	public static void add(final Activity ctx, final String colorCode) {
		if (ctx!=null && !TextUtils.isEmpty(colorCode)) {
			SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constants.PREF_NAME, Activity.MODE_PRIVATE);		  	 
       	 	String favorites = sharedPreferences.getString(Constants.FAVOURITES, EMPTY);
       	 	if (!favorites.contains(colorCode)) {
       	 		sharedPreferences.edit()	
       	 		.putString(Constants.FAVOURITES, favorites.concat(colorCode.concat(SEPARATOR)))
       	 		.commit();
       	 	}        	 
       	    Toast.makeText(ctx, ctx.getString(R.string.favadd_msg, colorCode), Toast.LENGTH_SHORT ).show();       	    
		}
	}	
	
	public static String[] list(final Activity ctx) {
		String[] favorites = null;
		if (ctx!=null){
			SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constants.PREF_NAME, Activity.MODE_PRIVATE);		  	 
       	 	String favoritesList = sharedPreferences.getString(Constants.FAVOURITES, EMPTY);
       	 	
			favorites = favoritesList.contains(SEPARATOR) ? favoritesList.split(SEPARATOR) : new String[]{};
		}		
		return favorites;
	}
	
	public static void delete(final Activity ctx, final List<String> entries) {
		if (ctx!=null && entries!=null) {			
			SharedPreferences sharedPreferences = ctx.getSharedPreferences(Constants.PREF_NAME, Activity.MODE_PRIVATE);		  	 
       	 	String favorites = sharedPreferences.getString(Constants.FAVOURITES, EMPTY);
       	 	
       	 	for (String colorCode : entries) {       	 		
       	 		favorites = favorites.replace(colorCode.concat(SEPARATOR), EMPTY);
       	 	}
       	    
       	 	sharedPreferences.edit().putString(Constants.FAVOURITES, favorites).commit();       	       	    
		}
	}
}
