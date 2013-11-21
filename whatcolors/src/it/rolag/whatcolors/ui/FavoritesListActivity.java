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

import java.util.ArrayList;
import java.util.List;
import it.rolag.whatcolors.R;
import it.rolag.whatcolors.model.ColorInfo;
import it.rolag.whatcolors.tools.FavoritesManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Gestisce la lista di colori preferiti
 * 
 * @author Rocco Lagrotteria
 *
 */
public class FavoritesListActivity extends ColorInfoListActivity {
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Build.VERSION.SDK_INT>Build.VERSION_CODES.GINGERBREAD_MR1){
			ActionBar actionBar = getActionBar();
			if (actionBar != null) {
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			}
			
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			getListView().setMultiChoiceModeListener(new ListItemChoiceListener(R.menu.favs));			
		} else {
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			getListView().setOnItemLongClickListener(this);
		}		
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
		renderList();
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB){
			getMenuInflater().inflate(R.menu.favs, menu);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) { 
			case android.R.id.home: 
				finish();
				break;
			
			case R.id.menuBlend:
				blend();
				break;
				
			case R.id.menuDelete:
				deleteChecked();
				break;
		} 
		return true;
	}
	
	@Override
	public void deleteChecked(){
		long[] ids = getListView().getCheckedItemIds();
		if (ids.length>0) {
			List<String> toDeleteList = new ArrayList<String>(ids.length);
			ColorInfoAdapter adapter = (ColorInfoAdapter) getListAdapter();
			
			for (Long id : ids) {
				toDeleteList.add(adapter.getItemById(id).getLabel());
			}
			
			FavoritesManager.delete(this, toDeleteList);
			
			renderList();
		} else {
			Toast.makeText(this, R.string.noblend_msg, Toast.LENGTH_SHORT).show();
		}
	}
		
	public void renderList() {
		String[] favs = FavoritesManager.list(this);		
		
		if (favs.length>0) {
			ColorInfoAdapter adapter = new ColorInfoAdapter();
			for (String code : favs) {
				adapter.addColorInfo(new ColorInfo(code, 1));
			}			
			
			setListAdapter(adapter);
			getListView().setOnItemClickListener(this);
		} else {
			Toast.makeText(this, R.string.favempty_msg, Toast.LENGTH_SHORT).show();
			finish();
		}
		
	}	
	
	@Override
	public View buildItemView(ColorInfo colorInfo, float percentRatio) {
		String colorCode = colorInfo.getLabel();
		
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View favoriteView = layoutInflater.inflate(R.layout.list_element, null);
			
		int color = Color.parseColor(colorCode);
		favoriteView.setId(color);
		favoriteView.setTag(colorCode);
		TextView txtColorCode = (TextView) favoriteView.findViewById(R.id.txtColorCode);
		txtColorCode.setText(colorCode);
		
		favoriteView.findViewById(R.id.txtColorShare).setVisibility(View.INVISIBLE);
		
		ImageView imgColorSample = (ImageView) favoriteView.findViewById(R.id.imgColorSample);
		if (colorCode.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
			imgColorSample.setBackgroundColor(color);	
		} else {
			favoriteView.setEnabled(false);
		}
		return favoriteView;
	}
	
}