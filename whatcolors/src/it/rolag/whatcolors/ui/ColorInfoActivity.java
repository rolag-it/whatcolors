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
import it.rolag.whatcolors.tools.ColorInfoAdapter;

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
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Toast;

/**
 * Elabora l'immagine specificata nella property {@code data} dell'{@link Intent} di avvio;
 * L'elaborazione avviene in background, usando la classe nidificata {@code ImageParserTask}
 * che, al termine, mostra i risultati in una {@link ListView}
 * 
 * @author Rocco Lagrotteria
 * 
 */
public class ColorInfoActivity extends Activity implements OnItemLongClickListener {	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);		

		setContentView(R.layout.view_result);
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
	
	/**
	 * Popola la {@link ListView} dei risultati
	 * 	
	 * @param colorInfoAdapter {@link ColorInfoAdapter} con i dati da visualizzare
	 * 
	 * @author Rocco Lagrotteria	
	 */
	@SuppressLint("NewApi")
	public void renderList(ColorInfoAdapter colorInfoAdapter) {			
		ListView lstResult = (ListView) findViewById(R.id.lstResult);		
		
		if (lstResult!=null) {			
			lstResult.setAdapter(colorInfoAdapter);	
			lstResult.setOnItemClickListener(new ColorDetailActivity.OpenerOnItemClick(this));
			
			if (Build.VERSION.SDK_INT>Build.VERSION_CODES.GINGERBREAD_MR1){
				lstResult.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
				lstResult.setMultiChoiceModeListener(new ColorChoiceListener());
			} else {
				lstResult.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				lstResult.setOnItemLongClickListener(this);
			}
		}		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB){
			getMenuInflater().inflate(R.menu.result, menu);
		}
		return true;
	}
		
	/* (non-Javadoc)
	 * Implementazione dell'OnItemClickListener per la ListView,
	 * per gestire la multiselezioni di elementi su GingerBread 
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,	long id) {				
		ListView lstResult = (ListView) findViewById(R.id.lstResult);		
		ColorInfoAdapter adapter = (ColorInfoAdapter) lstResult.getAdapter();
		
		boolean selected = adapter.getSelectedPosition().contains(Integer.valueOf(position));				
		if (selected) {
			lstResult.setItemChecked(position, false);
			adapter.getSelectedPosition().remove(Integer.valueOf(position));
		} else {
			lstResult.setItemChecked(position, true);
			adapter.getSelectedPosition().add(Integer.valueOf(position));
		}		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		super.onOptionsItemSelected(item);
		if (item.getItemId() == android.R.id.home) {
			finish();
		} else
		if (item.getItemId() == R.id.menuResultBlend) {
			blend();
		}
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		
		/*
		 * Salvataggio dei dati contenuti nell'adapter
		 * per un successivo ripristino della lista
		 */
		ListView lstResult = (ListView) findViewById(R.id.lstResult);
		if (lstResult!=null) {
			ColorInfoAdapter colorInfoAdapter = (ColorInfoAdapter) lstResult.getAdapter();
			if (colorInfoAdapter!=null) {				
				outState.putParcelableArray(Constants.COLOR_ENTRIES, colorInfoAdapter.dumpData());				
			}			
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
			ActivityUtil.lockOrientation(ColorInfoActivity.this);
			progressDialog = new ProgressDialog(ColorInfoActivity.this);
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
				AlertDialog.Builder alertBuilding = new AlertDialog.Builder(ColorInfoActivity.this);
				alertBuilding.setMessage(R.string.scan_error);
				alertBuilding.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ColorInfoActivity.this.finish();						
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
				ActivityUtil.unlockOrientation(ColorInfoActivity.this);
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
	
	/**
	 * Gestisce la selezione degli elementi della lista
	 * per la visualizzazione del colore 
	 * risultante dal mash della selezione
	 * 
	 * @author Rocco Lagrotteria
	 *
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private class ColorChoiceListener implements MultiChoiceModeListener {
		int selected = 0;
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			switch (item.getItemId()) {
				case R.id.menuResultBlend:
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
	        inflater.inflate(R.menu.result, menu);
	       
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
			mode.setTitle(getString(R.string.menu_restitle, selected));
		}
		
	}

	
	/**
	 * Avvia la visualizzazione del colore ottenuto
	 * da un {@link ColorBlend} per i colori
	 * selezionati nella {@link ListView} 
	 * 
	 */
	public void blend() {
		ListView lstResult = (ListView) findViewById(R.id.lstResult);
		if (lstResult!=null) {
			ColorInfoAdapter adapter = (ColorInfoAdapter) lstResult.getAdapter();
			if (adapter!=null) {
				ColorBlend colorBlend = new ColorBlend();
				long[] ids = lstResult.getCheckedItemIds();
				
				if (ids.length>0) {
					for (Long id : ids) {
						ColorInfo colorInfo = (ColorInfo) adapter.getItem(id.intValue());					
						colorBlend.incrementColor(colorInfo.getRelatedColor());
					}
					
					String blendedColorCode = ColorInfo.intToHexCode(colorBlend.getColorBlend());				
					Intent showDetail = new Intent(this, ColorDetailActivity.class);
					showDetail.putExtra(Constants.COLOR_CODE, blendedColorCode);
					startActivity(showDetail);
				} else {
					Toast.makeText(this, R.string.noblend_msg, Toast.LENGTH_SHORT).show();
				}
							
			}
		}
		
	}

	
}