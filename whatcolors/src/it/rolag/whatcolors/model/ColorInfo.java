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

import java.util.Locale;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Rappresenta le informazioni di diffusione (percentuale di presenza) 
 * di un determinato colore in una immagine 
 * 
 * 
 * @author Rocco Lagrotteria
 *
 */
public class ColorInfo implements Comparable<ColorInfo>, Cloneable, Parcelable {
	
	private final String label;
	private final int share;
	
	/**
	 * Rappresenta le informazioni di diffusione (percentuale di presenza) 
     * di un determinato colore in una immagine
	 * 
	 * @param color intero che rappresenta i 4 byte di una codifica ARGB... un {@link Color} praticamente
	 * @param share percentuale di diffusione
	 */
	public ColorInfo(int color, int share) {		
		this(intToHexCode(color), share);
	}	
	
	/**
	 * Rappresenta le informazioni di diffusione (percentuale di presenza) 
     * di un determinato colore in una immagine
	 * 
	 * @param label codice colore esadecimale (6 caratteri) con sharp all'inizio
	 * @param share percentuale di diffusione
	 */
	public ColorInfo(String label, int share) {			
		if (label != null) {
			this.label = label.toUpperCase(Locale.getDefault());
		} else {
			this.label = "UNKNOW";
		}
		this.share = share;
	}
	
	public ColorInfo(Parcel in){
		this.label = in.readString();
		this.share = in.readInt();
	}

	/**
	 * 
	 * @return codice colore esadecimale (6 caratteri) con sharp all'inizio
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * 
	 * @return percentuale di diffusione
	 */
	public int getShare() {
		return share;
	}
	
	/**
	 * 
	 * @return intero che rappresenta i 4 byte di una codifica ARGB... un {@link Color} praticamente
	 */
	public int getRelatedColor(){
		int relatedColor;
		try {
			relatedColor = Color.parseColor(label);
		} catch (IllegalArgumentException exception){
			relatedColor = Color.TRANSPARENT;
		}
		
		return relatedColor;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + share;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ColorInfo other = (ColorInfo) obj;
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
		if (share != other.share) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "ColorInfo [label=" + label + ", share=" + share + "]";
	}
	
	public String toJSON() {
		return "{label:\""+label+"\",share:"+share+"}";
	}

	@Override
	public int compareTo(ColorInfo another) {
		if (another.share>this.share) {
			return 1;
		} else if (another.share<this.share) {
			return -1;
		} else {
			return another.label.compareTo(this.label);
		}			
	}
	
	public int compareHSV(ColorInfo another){
		float[] hsv = new float[3];		
		Color.colorToHSV(this.getRelatedColor(), hsv);
		Float thisHue = hsv[0];
		Color.colorToHSV(another.getRelatedColor(), hsv);			
		return thisHue.compareTo(hsv[0]);
	}
	
	/**
	 * Sice se il colore specificato e somigliante 
	 * nei limiti della soglia percentuale indicata 
	 * 
	 * @param another un altro {@link ColorInfo}
	 * @param tollerance soglia percentuale (se 0, i colori devono essere proprio identici) 
	 * @return true se i colori si somigliano
	 */
	public boolean isMatchingColor(ColorInfo another, int tollerance) {
		if (tollerance>100 || tollerance<0) {
			throw new IllegalArgumentException("Tollerance must be a percent value");
		}
		
		tollerance*=2.56;
		int base = this.getRelatedColor();
		int color = another.getRelatedColor();
				
		int redDiff = Color.red(base) - Color.red(color);
		int greenDiff = Color.green(base) - Color.green(color);
		int blueDiff = Color.blue(base) - Color.blue(color);		
		
		return Math.abs(redDiff) < tollerance &&
			   Math.abs(greenDiff) < tollerance &&
			   Math.abs(blueDiff) < tollerance;	
	}
	
	@Override
	public ColorInfo clone() {
		ColorInfo cloned = null;
		try {
			cloned = (ColorInfo) super.clone();
		} catch (CloneNotSupportedException cloneNotSupportedException) {
			cloned = new ColorInfo(label, share);
		}
		
		return cloned;
	}

	@Override
	public int describeContents() {		
		return 0;		
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {		
		dest.writeString(label);
		dest.writeInt(share);
	}
	
	public static final Parcelable.Creator<ColorInfo> CREATOR = new Parcelable.Creator<ColorInfo>() {
        public ColorInfo createFromParcel(Parcel in) {
            return new ColorInfo(in); 
        }

        public ColorInfo[] newArray(int size) {
            return new ColorInfo[size];
        }
    };
    
    public static String intToHexCode(int color) {
		return "#" + Integer.toHexString(color).substring(2).toUpperCase(Locale.getDefault());
	}

}