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
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcelable;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Gestisce la rappresentazione di un lista di {@link ColorInfo}
 * con viualizzazione dettagli al click e multiselezione per funzionalità blend 
 * 
 * @author Rocco Lagrotteria
 *
 */
public abstract class ColorInfoListActivity extends ListActivity implements OnItemClickListener, OnItemLongClickListener {
	
	/* (non-Javadoc)
	 * Implementazione dell'OnItemClickListener
	 * per visualizzare i dettagli per l'elemento toccato  
	 * 
	 */
	@Override
	public void onItemClick(AdapterView<?> adapter, View view,	int position, long id) {		
		Intent showDetail = new Intent(this, ColorDetailActivity.class);
		ColorInfo colorInfo = (ColorInfo) getListAdapter().getItem(position);
		showDetail.putExtra(Constants.COLOR_CODE, colorInfo.getLabel());
		startActivity(showDetail);		
	}	
	
	/* (non-Javadoc)
	 * Implementazione dell'OnItemLongClickListener
	 * per gestire la multiselezioni di elementi per le API10 
	 */
	@Override
	public final boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,	long id) {				
				
		ColorInfoAdapter adapter = (ColorInfoAdapter) getListAdapter();
		
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
	 * Gestisce la selezione degli elementi della lista
	 * per la cancellazione di entry e la visualizzazione del colore 
	 * risultante dal blend della selezione
	 * 
	 * @author Rocco Lagrotteria
	 *
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected class ListItemChoiceListener implements MultiChoiceModeListener {
		final int CHOICE_MENU_RESOURCE; 
		int selected = 0;
		
		public ListItemChoiceListener(int choiceMenuResurce){
			this.CHOICE_MENU_RESOURCE = choiceMenuResurce;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			switch (item.getItemId()) {
				case R.id.menuDelete:
					deleteChecked();
					mode.finish();
					return true;
	
				case R.id.menuBlend:
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
	        inflater.inflate(CHOICE_MENU_RESOURCE, menu);
	       
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
			mode.setTitle(getString(R.string.menu_selected, selected));
		}
		
	}
	
	/**
	 * Elimina dalla {@link ListView} gli elementi selezionati 
	 * 
	 */
	public abstract void deleteChecked();
	
	/**
	 * Avvia la visualizzazione del colore ottenuto
	 * da un {@link ColorBlend} per i colori
	 * selezionati nella {@link ListView}  
	 * 
	 */
	public final void blend(){
		
		ColorBlend colorBlend = new ColorBlend();
		ColorInfoAdapter adapter = (ColorInfoAdapter) getListAdapter();
		
		long[] ids = getListView().getCheckedItemIds();
	    if (ids.length>0) {	
			for (Long id : ids) {			
				String colorCode = adapter.getItemById(id).getLabel();			
				colorBlend.incrementColor(Color.parseColor(colorCode));
			}
			
			String blendedColorCode = ColorInfo.intToHexCode(colorBlend.getColorBlend());
			
			Intent showDetail = new Intent(this, ColorDetailActivity.class);
			showDetail.putExtra(Constants.COLOR_CODE, blendedColorCode);
			startActivity(showDetail);
		} else {
			Toast.makeText(this, R.string.noblend_msg, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	/**
	 * Restituisce la {@link View} che il {@link ColorInfoAdapter} 
	 * utilizza per rappresentare 
	 * il {@link ColorInfo} specificato
	 * 
	 * @param colorInfo Elemento della lista
	 * @param percentRatio Valore percentuale della property {@link ColorInfo#getShare()}  
	 * 
	 */
	public abstract View buildItemView(ColorInfo colorInfo, float percentRatio); 
	
	
	/**
	 * Una implentazione di {@link Adapter} per le {@link ListView}
	 * che devono esporre dati riguardanti {@link ColorInfo}
	 * 
	 * @author Rocco Lagrotteria
	 *
	 */
	protected final class ColorInfoAdapter extends BaseAdapter {
		
		private final List<ColorInfo> listColorInfos;
		private int total;
		private final Set<Integer> selected;
		
		public Set<Integer> getSelectedPosition(){
			return selected;
		}
			
		public ColorInfoAdapter() {
			super();
			listColorInfos = new ArrayList<ColorInfo>();
			total = 0;
			selected = new LinkedHashSet<Integer>();
		}
		
		public synchronized void addColorInfo(ColorInfo colorInfo){		
			total+=colorInfo.getShare();
			listColorInfos.add(colorInfo);
		}
		
		public Parcelable[] dumpData(){
			Parcelable[] data = new Parcelable[listColorInfos.size()];
			return listColorInfos.toArray(data);
		}
		
		public void restoreData(Parcelable[] data){
			listColorInfos.clear();
			try {
				for(Parcelable p : data){
					addColorInfo((ColorInfo) p);
				}
			} catch (ClassCastException classCastException) {}	
		}
		
		@Override
		public final int getCount() {			
			return listColorInfos.size();
		}

		@Override
		public final Object getItem(int position) {			
			return listColorInfos.get(position);
		}
		
		public final ColorInfo getItemById(long id) {
			return listColorInfos.get(Long.valueOf(id).intValue());
		}

		@Override
		public final long getItemId(int position) {		
			return position;
		}

		@Override
		public final View getView(int position, View convertView, ViewGroup parent) {
			
			float percentRatio =  100F/total;
			ColorInfo colorInfo = listColorInfos.get(position);
			
			View colorInfoView = buildItemView(colorInfo, percentRatio);
			if (Build.VERSION.SDK_INT>Build.VERSION_CODES.GINGERBREAD_MR1){
				colorInfoView.setBackgroundResource(R.drawable.item_selector);				
			} else {
				/*
				 * gestione degli elementi selezionati per Gingerbread,
				 * perche' l'item selector non funziona 
				 */
				if (selected.contains(Integer.valueOf(position))) {
					colorInfoView.setBackgroundResource(R.drawable.itm_select);
				} else {
					colorInfoView.setBackgroundResource(R.drawable.itm_unselect);
				}
			}
			
			return colorInfoView;
		}

		@Override
		public final boolean hasStableIds() {		
			return true;
		}

		@Override
		public final boolean isEnabled(int position) {		
			boolean enabled;
			try {
				enabled = listColorInfos.get(position).getLabel().matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
			} catch(IndexOutOfBoundsException e) {
				enabled = false;
			}
			return enabled;
		}
		
	}
	
}