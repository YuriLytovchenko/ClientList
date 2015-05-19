package com.example.clientlist;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class AppInfo {
        private String appName = "";
        private String packageName = "";
        private String versionName = "";
        private int versionCode = 0;
        private Drawable icon;
        
        public String getAppname() {
			return appName;
		}
		public void setAppName(String appname) {
			this.appName = appname;
		}
		public String getPackageName() {
			return packageName;
		}
		public void setPackageName(String pname) {
			this.packageName = pname;
		}
		public String getVersionName() {
			return versionName;
		}
		public void setVersionName(String versionName) {
			this.versionName = versionName;
		}
		public int getVersionCode() {
			return versionCode;
		}
		public void setVersionCode(int versionCode) {
			this.versionCode = versionCode;
		}
		public Drawable getIcon() {
			return icon;
		}
		public void setIcon(Drawable icon) {
			this.icon = icon;
		}

		public void prettyPrint() {
            Log.v(MainActivity.TAG_INFO, "Name: "+ appName + "\n" + 
            		"; Package: " + packageName + "\n" +
            		"; Version: " + versionName + "\n" + 
            		"; Version code: " + versionCode);
            
        }
}
