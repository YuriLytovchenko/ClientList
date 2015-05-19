package com.example.clientlist;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppInfoView extends LinearLayout {
	Context context = null;
    private String packageName = "";


	public AppInfoView(Context context) {
		super(context);
		Init(context, null);
	}

	public AppInfoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Init(context, attrs);
	}
	
	public void Init(Context context, AttributeSet attrs) {
		this.context = context;
	}

	//set text for inside elements - pass a corresponding id out of:
	//appinfo_version_name
	//appinfo_version_code
	//appinfo_package_name
	public void setText(int id, CharSequence text) {
		View view = findViewById(id);
		if (view != null && view instanceof TextView)
			((TextView)view).setText(text);
	}
	
	//set icon
	public void setIcon(Drawable icon) {
		ImageView imageView = (ImageView) findViewById(R.id.appinfo_icon);
		imageView.setImageDrawable(icon);
	}
	
	//set all data at once through AppInfo instances
	public void setData(AppInfo appInfo) {
		//to be used later when launching the application
		this.packageName = appInfo.getPackageName();
		
		this.setIcon(appInfo.getIcon());
		this.setText(R.id.appinfo_app_name, appInfo.getAppname());
		this.setText(R.id.appinfo_package_name, appInfo.getPackageName());
		this.setText(R.id.appinfo_version_code, ((Integer)appInfo.getVersionCode()).toString());
		this.setText(R.id.appinfo_version_name, appInfo.getVersionName());
	}

	// launch the corresponding application on tap
	// performClick won't work if the parent component will be changed to custom
	// onTouchEvent will work anyway
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		PackageManager manager = context.getPackageManager();
		try {
			Intent intent = manager.getLaunchIntentForPackage(packageName);
			if (intent == null)
				throw new PackageManager.NameNotFoundException();
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			context.startActivity(intent);
		} catch (PackageManager.NameNotFoundException e) {
			Log.v(MainActivity.TAG_ERROR, "Failed to find an activity with package name: " + packageName);
			e.printStackTrace();
		}
		return 		super.onTouchEvent(event);

	}
	
}
