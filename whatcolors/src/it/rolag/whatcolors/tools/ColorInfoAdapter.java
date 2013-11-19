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
package it.rolag.whatcolors.tools;

import it.rolag.whatcolors.R;
import it.rolag.whatcolors.model.ColorInfo;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Una implentazione di {@link Adapter} per le {@link ListView}
 * che devono esporre dati riguardanti {@link ColorInfo}
 * 
 * @author Rocco Lagrotteria
 *
 */
public final class ColorInfoAdapter extends BaseAdapter {
	
	private final List<ColorInfo> listColorInfos;
	private int total;
		
	public ColorInfoAdapter() {
		super();
		listColorInfos = new ArrayList<ColorInfo>();
		total = 0;
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

	@Override
	public final long getItemId(int position) {		
		return position;
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {	 
		View colorInfoView;
		try {
			float percentRatio =  100F/total;
			ColorInfo colorInfo = listColorInfos.get(position);
			
			LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		} catch(IndexOutOfBoundsException e) {
			colorInfoView = null;
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