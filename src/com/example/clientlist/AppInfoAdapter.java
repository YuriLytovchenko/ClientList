package com.example.clientlist;

import java.util.ArrayList;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//there won't be big lists, so no scrolling problems expected
@SuppressLint("ViewHolder") public class AppInfoAdapter extends ArrayAdapter<AppInfo> {
	private final Context context;
	private final ArrayList<AppInfo> appViews;
	
	public AppInfoAdapter(Context context, ArrayList<AppInfo> appViews) {
		super(context, R.layout.appinfoview, appViews);
		this.context = context;
		this.appViews = appViews;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View appInfoView = inflater.inflate(R.layout.appinfoview, parent, false);
		
		TextView appName = (TextView) appInfoView.findViewById(R.id.appinfo_app_name);
		TextView packageName = (TextView) appInfoView.findViewById(R.id.appinfo_package_name);
		TextView versionName = (TextView) appInfoView.findViewById(R.id.appinfo_version_name);
		TextView versionCode = (TextView) appInfoView.findViewById(R.id.appinfo_version_code);
		ImageView imageView = (ImageView) appInfoView.findViewById(R.id.appinfo_icon);
		
		appName.setText(appViews.get(position).getAppname());
		packageName.setText(appViews.get(position).getPackageName());
		versionName.setText(appViews.get(position).getVersionName());
		//some version code, I don't know what does it mean
		versionCode.setText("(" + ((Integer)appViews.get(position).getVersionCode()).toString() + ")");
		imageView.setImageDrawable(appViews.get(position).getIcon());
		appInfoView.setPadding(5, 5, 5, 20);
		
		return appInfoView;

	}

}
