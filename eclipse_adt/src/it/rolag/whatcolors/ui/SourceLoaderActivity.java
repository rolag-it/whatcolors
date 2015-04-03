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

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Gestisce la shermata di avvio e la ricezione di ACTION_SEND da altre app 
 * 
 * @author Rocco Lagrotteria
 * 
 * */
public class SourceLoaderActivity extends Activity {	 
		
	private Uri mImageUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_loader);		
		
		/*
		 * Disattivazione del pulsante per l'acquisizione da fotocamera
		 * se il device non ne dispone
		 */
		if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			findViewById(R.id.imbCamera).setEnabled(false);
		}
		
		if(savedInstanceState!=null) mImageUri = savedInstanceState.getParcelable(Constants.IMG_URI);
		
		/*
		 * Gestione ricezione immagini da altre app, tramite ACTION_SEND		
		 */
		try {
			if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
				String sentObjType = getIntent().getType(); 
				
				if (sentObjType !=null && sentObjType.startsWith("image/")) {
					mImageUri = (Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
					startScanning();
				} else {
					throw new UnsupportedOperationException();
				}			
			}
		} catch (Exception e) {
			Toast.makeText(this, R.string.type_missmatch, Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {		
		super.onSaveInstanceState(outState);
		if (mImageUri!=null) outState.putParcelable(Constants.IMG_URI, mImageUri);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		
		switch (item.getItemId()) {
			
			case R.id.menuSettings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;				
			
			case R.id.menuFavoritesList:
				startActivity(new Intent(this, FavoritesListActivity.class));
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}		
	}
	
	/**
	 * Associato all'attributo onClick dei pulsunti nella schermata,
	 * determina se avviare la cattura da fotocamera o dalla gallery
	 * in base all'id del widget chiamante
	 * 
	 * @author Rocco Lagrotteria
	 * 
	 */
	public void startCapturing(View caller){
		
		Intent takePictureIntent = null;
		switch (caller.getId()) {
			case R.id.imbCamera:
				mImageUri = createPictureFile();
				
				takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);				
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);	
				break;
				
			case R.id.imbGallery:
				takePictureIntent = new Intent(Intent.ACTION_PICK);
				takePictureIntent.setType("image/*");
				break;
				
			default:
				throw new IllegalArgumentException();
		}
		
		startActivityForResult(takePictureIntent, caller.getId());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		if (resultCode == RESULT_OK) {				
			
			if (R.id.imbCamera == requestCode) {
				/* Invio di un broadcast per l'aggiornamento della gallery
				 * in modo che venga mostrata la foto appena scattata
				 */
				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);					
				mediaScanIntent.setData(mImageUri);
				sendBroadcast(mediaScanIntent);					
			} else 
			if (R.id.imbGallery == requestCode){
				mImageUri = data.getData();
			}
		
			startScanning();			
		}		
	}
	
	/**
	 * Avvia l'activity che elabora l'immagine
	 * 
	 * @param imageUri {@link Uri} di riferimento dell'immagine da elaborare
	 * 
	 * @author Rocco Lagrotteria
	 * 
	 */
	private void startScanning() {
		if (mImageUri!=null) {
			Intent scanIntent = new Intent(this, ResultListActivity.class);
			scanIntent.setData(mImageUri);
			startActivity(scanIntent);
		} else {
			Toast.makeText(this, R.string.type_missmatch, Toast.LENGTH_SHORT).show();
		}
	}	
	
	/**
	 * Crea un file  
	 * per ospitare l'immagine catturata dalla fotocamera
	 * 
	 * @return {@link Uri} di riferimento del file creato
	 */
	private Uri createPictureFile(){
		DateFormat timestampFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
		String timestamp = timestampFormatter.format(new Date()); 
		String fileName = timestamp.concat(".jpg");	
		
		File imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name));		
		imageDir.mkdirs();
		
		File pictureFile = new File(imageDir, fileName);
		
		return Uri.fromFile(pictureFile);
	}
		
}
