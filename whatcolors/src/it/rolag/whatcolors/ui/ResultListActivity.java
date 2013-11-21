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
import it.rolag.whatcolors.model.ColorBlend;
import it.rolag.whatcolors.model.ColorInfo;
import it.rolag.whatcolors.tools.ActivityUtil;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;

/**
 * Elabora l'immagine specificata nella property {@code data} dell'{@link Intent} di avvio;
 * L'elaborazione avviene in background, usando la classe nidificata {@code ImageParserTask}
 * che, al termine, mostra i risultati in una {@link ListView}
 * 
 * @author Rocco Lagrotteria
 * 
 */
public class ResultListActivity extends ColorInfoListActivity {	
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
		}		
		
		/*
		 * La lista dei risultati viene generata dal task di elaborazione 
		 * solo alla prima esecuzione (stato della view assente).
		 * 
		 * In caso di ricostruzione della view, viene ripristinata
		 * la lista in base ai dati salvati nello stato.
		 * 
		 */
		if (savedInstanceState != null) {			
			restoreList(savedInstanceState);
		} else {			
			Uri imgUri = getIntent().getData();
			new ImageParserTask().execute(imgUri);		 
		}	
	}
	
	/**
	 * Ripristina la {@link ListView} dei risultati in base ai dati
	 * precedentemente salvati
	 * 
	 * @param savedInstanceState {@link Bundle} con i dati dello stato salvato
	 * 
	 * @author Rocco Lagrotteria	 
	 * 
	 */
	private void restoreList(Bundle savedInstanceState) {	
		Parcelable[] listData = savedInstanceState.getParcelableArray(Constants.COLOR_ENTRIES);		
		if (listData!=null) {			
			ColorInfoAdapter colorInfoAdapter = (ColorInfoAdapter) new ColorInfoAdapter();			
		    colorInfoAdapter.restoreData(listData);
		    
		    renderList(colorInfoAdapter);		    	
		}		
	}
	
	@Override
	protected void onPostResume() {
		super.onPostResume();		
		getListView().clearChoices();		
	}
	
	@Override
	public View buildItemView(ColorInfo colorInfo, float percentRatio) {
		View colorInfoView;
							
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		colorInfoView = layoutInflater.inflate(R.layout.list_element, null);			
		
		colorInfoView.setId(colorInfo.hashCode());
		colorInfoView.setTag(colorInfo.getLabel());
		TextView txtColorCode = (TextView) colorInfoView.findViewById(R.id.txtColorCode);
		txtColorCode.setText(colorInfo.getLabel());
		
		TextView txtColorShare = (TextView) colorInfoView.findViewById(R.id.txtColorShare);
		txtColorShare.setText(Math.round(colorInfo.getShare()*percentRatio) + "%");
		
		ImageView imgColorSample = (ImageView) colorInfoView.findViewById(R.id.imgColorSample);
		if (colorInfo.getLabel().matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
			imgColorSample.setBackgroundColor(colorInfo.getRelatedColor());	
		} else {
			colorInfoView.setEnabled(false);
		}
		
		return colorInfoView;
	}
	
	
	/**
	 * Popola la {@link ListView} dei risultati
	 * 	
	 * @param colorInfoAdapter {@link ColorInfoAdapter} con i dati da visualizzare
	 * 
	 * @author Rocco Lagrotteria	
	 */
	@SuppressLint("NewApi")
	public void renderList(ColorInfoAdapter colorInfoAdapter) {			
		
		setListAdapter(colorInfoAdapter);
		getListView().setOnItemClickListener(this);
		
		if (Build.VERSION.SDK_INT>Build.VERSION_CODES.GINGERBREAD_MR1){
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			getListView().setMultiChoiceModeListener(new ListItemChoiceListener(R.menu.result));
		} else {
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			getListView().setOnItemLongClickListener(this);
		}				
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB){
			getMenuInflater().inflate(R.menu.result, menu);
		}
		return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		super.onOptionsItemSelected(item);
		if (item.getItemId() == android.R.id.home) {
			finish();
		} else
		if (item.getItemId() == R.id.menuBlend) {
			blend();
		}
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		ColorInfoAdapter colorInfoAdapter = (ColorInfoAdapter) getListAdapter();
		if (colorInfoAdapter!=null) {				
			outState.putParcelableArray(Constants.COLOR_ENTRIES, colorInfoAdapter.dumpData());				
		}	
	}
	
	/**
	 * Elabora l'immagine riferita dall'{@link Uri} ricevuto come parametro,
	 * al termine genera un {@link ColorInfoAdapter} per popolare
	 * la {@link ListView} dei risultati
	 * 
	 * @author Rocco Lagrotteria	 
	 */
	private class ImageParserTask extends AsyncTask<Uri, Void, Boolean> {		
		
		private int totalPixels, precision, fidelity;		
		private SortedSet<ColorInfo> colorInfos;				
		private ProgressDialog progressDialog;
				
		
		@Override
		protected void onPreExecute() {
			
			/*
			 * nesessario bloccare l'orientamento della view
			 * per impedire il crash in caso di rotazione
			 * dello schermo
			 */
			ActivityUtil.lockOrientation(ResultListActivity.this);
			progressDialog = new ProgressDialog(ResultListActivity.this);
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.setMessage(getString(R.string.scan_message));
			progressDialog.show();
			
			/*
			 * Recupero dei settaggi per l'elaborazione
			 * precision - fattore di scala dell'immagine originale
			 * fidelity - soglia percentuale di somiglianza dei colori per il mash
			 */			
			try {
				SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
				precision = Integer.parseInt(prefs.getString(Constants.PRECISION, getString(R.string.default_precision)));
				fidelity = Integer.parseInt(prefs.getString(Constants.FIDELITY, getString(R.string.default_fidelity)));
			} catch (Exception e) {
				precision = Integer.parseInt(getString(R.string.default_precision));
				fidelity = Integer.parseInt(getString(R.string.default_fidelity));
			}			
		}		
		
		@Override
		protected Boolean doInBackground(Uri... params) {			
			
			Boolean error = Boolean.FALSE;
			
			try {			
				colorInfos = new TreeSet<ColorInfo>();				
				
				Queue<ColorInfo> allColorInfos = getAllColorInfos(params[0]);
				ColorInfo mainColor = allColorInfos.poll();
				
				/*
				 * Logica di accorpamento (mash) dei colori che hanno somiglianza 
				 * entro la soglia fidelity
				 */
				do {
					ColorBlend colorBlend = new ColorBlend();
					int matchCount = mainColor.getShare();				
					colorBlend.incrementColor(mainColor.getRelatedColor());
					
					do {					
						ColorInfo otherColor = allColorInfos.poll();
						if (mainColor.isMatchingColor(otherColor, fidelity)) {
							colorBlend.incrementColor(otherColor.getRelatedColor());
							matchCount += otherColor.getShare();
						} else {						
							mainColor = otherColor.clone();
							break;
						}
					} while(!allColorInfos.isEmpty());
					
					colorInfos.add(new ColorInfo(colorBlend.getColorBlend(), matchCount));
				
				} while(!allColorInfos.isEmpty());			
				 
				
			} catch (Exception exception) {
				error = Boolean.TRUE;
				Log.wtf(Constants.LOG_TAG, "Errore elaborazione file ", exception);
			}
			return error;
		}
		
		@Override
		protected void onPostExecute(Boolean error) {
			progressDialog.dismiss();
			if (error.booleanValue()) {				
				AlertDialog.Builder alertBuilding = new AlertDialog.Builder(ResultListActivity.this);
				alertBuilding.setMessage(R.string.scan_error);
				alertBuilding.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ResultListActivity.this.finish();						
					}
				});
				alertBuilding.create().show();
			} else {
				/*
				 * L'adapter viene construito con i dati del set colorInfos
				 * ordinato per share del colorInfo
				 */
				ColorInfoAdapter colorInfoAdapter = new ColorInfoAdapter();				
				byte size = 1;			
				for (Iterator<ColorInfo> colorsInfoIterator = colorInfos.iterator(); colorsInfoIterator.hasNext(); size++) {				
					ColorInfo colorInfo = colorsInfoIterator.next();
					
					if (size < Constants.MAX_LIST_ENTRIES) {
						colorInfoAdapter.addColorInfo(colorInfo);
						totalPixels -= colorInfo.getShare();					
					} else {
						colorInfoAdapter.addColorInfo(new ColorInfo(getString(R.string.color_mist), totalPixels));
						break;
					}
				}
				ActivityUtil.unlockOrientation(ResultListActivity.this);
				renderList(colorInfoAdapter);				
			}			
			
		}	
		
		/**
		 * Censisce i colori dell'imagine riferita dall'{@link Uri} ricevuto come parametro
		 * restiuindeli in una coda prioritaria per diffusione
		 * 
		 * @param imageUri {@link Uri} dell'immagine da elaborare
		 * @return {@link PriorityQueue} di {@link ColorInfo} in ordinati per tono (i colori simili sono vicini)
		 * @throws FileNotFoundException immagine non trovate
		 */
		private Queue<ColorInfo> getAllColorInfos(Uri imageUri) throws FileNotFoundException {
			BitmapFactory.Options bmpopts = new BitmapFactory.Options();
			bmpopts.inSampleSize = precision;			
			bmpopts.inDither = true;
			
			Bitmap imageReaded =  BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, bmpopts);			
			int imageWidth = imageReaded.getWidth();
			int imageHeight = imageReaded.getHeight();
			
			Map<String, Integer> colorsCount = new LinkedHashMap<String, Integer>();
			totalPixels = imageWidth * imageHeight;
		
			/*
			 * Censimento dei colori
			 */
			for (int y = 0; y<imageHeight; y++){
				for (int x = 0; x < imageWidth; x++) {
					String colorKey = Integer.toHexString(imageReaded.getPixel(x, y));
					while (colorKey.length()<8) {
						colorKey = "0"+colorKey;
					}
					colorKey = "#" + colorKey.substring(2);
					if (colorsCount.containsKey(colorKey)) {
						Integer tot = colorsCount.get(colorKey);
						colorsCount.put(colorKey, ++tot);
					} else {
						colorsCount.put(colorKey, 1);
					}
				}
			}
			
			imageReaded.recycle();
			
			/*
			 * Ordinamento per tonalita'
			 */
			Queue<ColorInfo> allColorInfos = new PriorityQueue<ColorInfo>(colorsCount.size(),
					/**
					 * Compara per tono i colori
					 */
					new Comparator<ColorInfo>() {
						@Override
						public int compare(ColorInfo lhs, ColorInfo rhs) {						
							return lhs.compareHSV(rhs);
						}
						
					});
			
			for (Entry<String, Integer> color : colorsCount.entrySet()) {				
				allColorInfos.add(new ColorInfo(color.getKey(), color.getValue().intValue()));
			}	
			
			return allColorInfos;
		}		
	}

	@Override
	public void deleteChecked() {}
	
}