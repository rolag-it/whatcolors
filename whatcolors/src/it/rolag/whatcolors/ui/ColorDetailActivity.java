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

import java.lang.ref.WeakReference;

import it.rolag.whatcolors.Constants;
import it.rolag.whatcolors.R;
import it.rolag.whatcolors.tools.FavoritesManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Mostra il colore corrispondente all'hexcode ricevuto come extra
 *
 * 
 * @author Rocco Lagrotteria
 *
 */
public class ColorDetailActivity extends Activity {
	
	private String colorCode = null;	
	
	public String getColorCode(){
		return colorCode;
	}	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);		
		
		if (savedInstanceState != null) {		
		 colorCode = savedInstanceState.getString(Constants.COLOR_CODE);	
		} else {		
		 colorCode = getIntent().getStringExtra(Constants.COLOR_CODE);
		}
		
		if (TextUtils.isEmpty(colorCode)) {
			Toast.makeText(this, R.string.type_missmatch, Toast.LENGTH_SHORT).show();
			finish();
		} else {		
			setContentView(R.layout.view_color);
			
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
				ActionBar actionBar = getActionBar(); 
				if (actionBar!=null){
					actionBar.setDisplayShowTitleEnabled(true);
					actionBar.setTitle(colorCode);
					actionBar.setDisplayHomeAsUpEnabled(true);
					actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
				}	
			} else {			
		    	setTitle(colorCode);	    	
			}
		}
	}
	
	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.color, menu);
		
		/*
		 * Intent per la condivisione del colore
		 */
		final Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_msg, colorCode));
		shareIntent.setType("text/plain");
		
		if (Build.VERSION.SDK_INT>Build.VERSION_CODES.HONEYCOMB_MR2) {
			ShareActionProvider shareActionProvider = (ShareActionProvider) menu.findItem(R.id.menuShare).getActionProvider();
			shareActionProvider.setShareIntent(shareIntent);
		} else {
			MenuItem shareMenuItem = menu.findItem(R.id.menuShare);
			shareMenuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					startActivity(shareIntent);
					return true;
				}
			});
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onStart() {		
		super.onStart();
		int color = Color.parseColor(colorCode);
		View layout = findViewById(R.id.lytMash);
		layout.setBackgroundColor(color);
		
		TextView txtColorCode = (TextView) findViewById(R.id.txtColorCode);
		txtColorCode.setText(colorCode);		
		txtColorCode.setTextColor(color^0x00FFFFFF);	
		
		/*
		 * Il codice del colore viene copia in clipboard con un long-press
		 */
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
			layout.setOnLongClickListener(new OnLongClickListener() {
				
				@SuppressLint("NewApi")
				@Override
				public boolean onLongClick(View v) {
					ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					clipboardManager.setPrimaryClip(ClipData.newPlainText(getString(R.string.clip_entry), getColorCode()));
					
					Toast.makeText(ColorDetailActivity.this, getString(R.string.clip_entry_msg, getColorCode()), Toast.LENGTH_SHORT).show();
					return true;
				}
			});
		} else {
			txtColorCode.setTextIsSelectable(true);
		}		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		
		case android.R.id.home:
			finish();
			return true;
		
         case R.id.menuFavoritesAdd:
		  	 FavoritesManager.add(this, colorCode);
		  	 setResult(RESULT_OK);
        	 return true;

		default:
			return super.onOptionsItemSelected(item);
		}				
	}	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {		
		super.onSaveInstanceState(outState);
		outState.putString(Constants.COLOR_CODE, colorCode);
	}
	
	/**
	 * Implementazione di {@link OnItemClickListener}
	 * per le {@link ListView} da cui si accede alla
	 * schermata di dettaglio del colore
	 * 
	 * @author Rocco Lagrotteria
	 *
	 */
	public static class OpenerOnItemClick implements OnItemClickListener {
		
		private WeakReference<Activity> fromActivityReference;
		
		public OpenerOnItemClick(Activity fromActivity) {
			fromActivityReference = new WeakReference<Activity>(fromActivity);
		}

		@Override
		public void onItemClick(AdapterView<?> adapter, View view,	int position, long id) {
			Activity fromActivity = fromActivityReference.get();
			if (fromActivity!=null) {
				Intent showDetail = new Intent(fromActivity, ColorDetailActivity.class);
				showDetail.putExtra(Constants.COLOR_CODE, (String) view.getTag());
				fromActivity.startActivity(showDetail);
			}
		}
	}
}