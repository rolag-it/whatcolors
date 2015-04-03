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
package it.rolag.whatcolors.model;

import android.graphics.Color;

/**
 * Rappresenta una mescolanza di colori, basata sul calcolo della media aritmetica
 * (pensa ad un pentolone)
 * 
 * @author Rocco Lagrotteria 
 */
public class ColorBlend {
	
	private int red;
	private int green;
	private int blue;
	
	private float totalPixel;
	
	public ColorBlend(){
		this.totalPixel = 0;
		this.red = 0;
		this.green = 0;
		this.blue = 0;
	}
	
	/**
	 * Aggiunge un colore al pentolone
	 * 
	 * @param color intero che rappresenta i 4 byte di una codifica ARGB... un {@link Color} praticamente
	 */
	public synchronized void incrementColor(int color){
		totalPixel++;
		red+=Color.red(color);
		green+=Color.green(color);
		blue+=Color.blue(color);
	}	
	
	
	/**
	 * Mescola il pentolone e vedi che ne esce
	 * 
	 * @return colore mescolato come intero che rappresenta i 4 byte di una codifica ARGB... un {@link Color} praticamente
	 */
	public synchronized int getColorBlend(){
		int maschedColor;
		if (totalPixel!=0) {
			maschedColor = Color.rgb((Math.round(red/totalPixel)), (Math.round(green/totalPixel)), (Math.round(blue/totalPixel)));
		} else {
			maschedColor = Color.rgb((red), (green), (blue));
		}
		return maschedColor;
	}
}