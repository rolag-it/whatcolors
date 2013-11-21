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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import it.rolag.whatcolors.Constants;
import it.rolag.whatcolors.R;
import it.rolag.whatcolors.model.ColorBlend;
import it.rolag.whatcolors.model.ColorInfo;
import it.rolag.whatcolors.tools.FavoritesManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
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
public class FavoritesListActivity extends ListActivity implements OnItemLongClickListener {
	
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
			getListView().setMultiChoiceModeListener(new FavoriteChoiceListener());			
		} else {
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			getListView().setOnItemLongClickListener(this);
		}
		
		initList();		
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
			
			case R.id.menuFavoritesBlend:
				blend();
				break;
				
			case R.id.menuFavoritesDelete:
				deleteChecked();
				break;
		} 
		return true;
	}
	
	/* (non-Javadoc)
	 * Implementazione dell'OnItemClickListener per la ListView,
	 * per gestire la multiselezioni di elementi su GingerBread 
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,	long id) {				
				
		FavoritesListAdapter adapter = (FavoritesListAdapter) getListAdapter();
		
		boolean selected = adapter.getSelectedPosition().contains(Integer.valueOf(position));				
		if (selected) {
			getListView().setItemChecked(position, false);
			adapter.getSelectedPosition().remove(Integer.valueOf(position));
		} else {
			getListView().setItemChecked(position, true);
			adapter.getSelectedPosition().add(Integer.valueOf(position));
		}		
		return true;
	}
	
	/**
	 * Popola la lista dei preferiti costruendo un {@link FavoritesListAdapter}
	 */
	public void initList() {
		FavoritesListAdapter adapter = new FavoritesListAdapter(this);
		if (adapter.isEmpty()) {
			Toast.makeText(this, R.string.favempty_msg, Toast.LENGTH_SHORT).show();
			finish();
		} else {			
			setListAdapter(adapter);			
			getListView().setOnItemClickListener(new ColorDetailActivity.OpenerOnItemClick(this));
		}
	}
	
	public void deleteChecked(){
		long[] ids = getListView().getCheckedItemIds();
		if (ids.length>0) {
			List<String> toDeleteList = new ArrayList<String>(ids.length);
			FavoritesListAdapter adapter = (FavoritesListAdapter) getListAdapter();
			
			for (Long id : ids) {
				toDeleteList.add(adapter.getItem(id.intValue()));
			}
			
			FavoritesManager.delete(this, toDeleteList);
			
			initList();
		} else {
			Toast.makeText(this, R.string.noblend_msg, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Avvia (con l'attesa di un risultato) la visualizzazione del colore ottenuto
	 * da un {@link ColorBlend} per i colori
	 * selezionati nella {@link ListView}  
	 * 
	 */
	public void blend(){
		
		ColorBlend colorBlend = new ColorBlend();
		FavoritesListAdapter adapter = (FavoritesListAdapter) getListAdapter();
		
		long[] ids = getListView().getCheckedItemIds();
	    if (ids.length>0) {	
			for (Long id : ids) {
				
				String colorCode = adapter.getItem(id.intValue());			
				colorBlend.incrementColor(Color.parseColor(colorCode));
			}
			
			String blendedColorCode = ColorInfo.intToHexCode(colorBlend.getColorBlend());
			
			Intent showDetail = new Intent(this, ColorDetailActivity.class);
			showDetail.putExtra(Constants.COLOR_CODE, blendedColorCode);
			startActivityForResult(showDetail, R.id.menuFavoritesBlend);
		} else {
			Toast.makeText(this, R.string.noblend_msg, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	/*
	 * Se si ottione un RESULT_OK da l'intent avviato con mash(), 
	 * si rigenera la lista (colore aggiunto ai preferiti)
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == R.id.menuFavoritesBlend && resultCode == RESULT_OK) {
			initList();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	/**
	 * Gestisce la selezione degli elementi della lista
	 * per la cancellazione di entry e la visualizzazione del colore 
	 * risultante dal mash della selezione
	 * 
	 * @author Rocco Lagrotteria
	 *
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private class FavoriteChoiceListener implements MultiChoiceModeListener {

		int selected = 0;
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			switch (item.getItemId()) {
				case R.id.menuFavoritesDelete:
					deleteChecked();
					mode.finish();
					return true;
	
				case R.id.menuFavoritesBlend:
					blend();
					mode.finish();
					return true;
				
				default:
					return false;
			}
			
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.favs, menu);
	       
	        return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			selected = 0;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {			
			return true;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
			if (checked) {
				selected++;
			} else {
				selected--;
			}			
			mode.setTitle(getString(R.string.menu_favtitle, selected));
		}
		
	}
	
	/**
	 * 
	 * Incapsula l'array di {@link String} dei colori preferiti
	 * per popolare la {@link ListView}. 
	 * 
	 * I dati vengono recuperati per usando il {@link FavoritesManager}
	 * 
	 * @author Rocco Lagrotteria
	 *
	 */
	private class FavoritesListAdapter extends ArrayAdapter<String> {

		private final Set<Integer> selected;
		
		public Set<Integer> getSelectedPosition(){
			return selected;
		}
		
		public FavoritesListAdapter(Activity context) {
			super(context, R.layout.list_element, FavoritesManager.list(context));
			selected = new LinkedHashSet<Integer>();
		}	

		@Override
		public long getItemId(int position) {			
			return position;
		}
		
		@Override
		public boolean hasStableIds() {			
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String colorCode = getItem(position);
			
			LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View favoriteView = layoutInflater.inflate(R.layout.list_element, null);
			
			if (Build.VERSION.SDK_INT>Build.VERSION_CODES.GINGERBREAD_MR1){
				favoriteView.setBackgroundResource(R.drawable.item_selector);				
			} else {
				/*
				 * gestione degli elementi selezionati per Gingerbread,
				 * perche' l'item selector non funziona 
				 */
				if (selected.contains(Integer.valueOf(position))) {
					favoriteView.setBackgroundResource(R.drawable.itm_select);
				} else {
					favoriteView.setBackgroundResource(R.drawable.itm_unselect);
				}
			}
			
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
	
}