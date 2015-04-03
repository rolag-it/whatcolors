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

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

/**
 * Utility per operazioni non banali su {@link Activity} 
 * 
 * 
 * @author Rocco Lagrotteria
 *
 */
public final class ActivityUtil {
	
	private ActivityUtil(){}
	
	/**
	 * Quando il device viene ruotato, l'{@link Activity} corrente viene distrutta 
	 * per essere ricreata in base al nuovo orientamento dello schermo
	 * {@linkplain (http://developer.android.com/training/basics/activity-lifecycle/recreating.html)}. 
	 * Questo metodo rende l'{@link Activity} specificata insensibile all'orientamento dello schermo,
	 * principalmente per evitare la distruzione inaspettata dell'{@link Activity}
	 * durante operazioni di front-end critiche
	 * 
	 * @param activity Instanza dell'{@link Activity} da bloccare
	 */
	public static void lockOrientation(final Activity activity){		
		switch (activity.getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_PORTRAIT:
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
			
			case Configuration.ORIENTATION_LANDSCAPE:
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
		}
	}
	
	/**
	 * il contrario di {@link #lockOrientation(Activity)}
	 */
	public static void unlockOrientation(final Activity activity){		
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);		
	}
}
