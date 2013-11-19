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
package it.rolag.whatcolors;

import android.content.SharedPreferences;

/**
 * Ospita esempi di costanza inaudita
 * 
 * @author Rocco Lagrotteria
 *
 */
public final class Constants {
	
	private Constants(){}
	
	/**
	 * Chiave per ottenere le {@link SharedPreferences}
	 */
    public static final String PREF_NAME = "it.rolag.whatcolors.prefs";
	
    /* chiavi delle preferenze configurabili */
    
    /**
     * Chiave del parametro prescion, ossia il fattore di scala per la riduzione delle immagini
     */
	public static final String PRECISION = "precision";
	
	/**
     * Chiave del parametro fidelity, ossia quanto (in percentuale) 
     * si devono somigliare i colori per essere accorpati nei risultati 
     */
	public static final String FIDELITY = "fidelity";	
	
	/**
     * Chiave dei preferiti
     */
	public static final String FAVOURITES = "favourites";
	
	/**
     * Chiave con cui si salva momentaneamente l'uri dell'immagine da elaborare (fotocamera)
     */
	public static final String IMG_URI = "img_uri";
	
	/**
	 * Chiave con cui si potrebbero salvare i dati di una lista per ricostruirla in un secondo momento
	 */
	public static final String COLOR_ENTRIES = "lstdata";
	
	/**
	 * Chiave che si puo' usare per passare un codice colore come extra tra un'activity e l'altra
	 */
	public static final String COLOR_CODE = "colorcode";	
	
	/**
	 * Massimo numero di colori visualizzati nella lista dei risultati
	 */
	public static final int MAX_LIST_ENTRIES = 24;

}