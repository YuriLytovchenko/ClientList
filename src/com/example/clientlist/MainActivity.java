package com.example.clientlist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity implements FilterDialog.FilterDialogListener {
	public final static String TAG_INFO = "[INFO]";
	public final static String TAG_ERROR = "[ERROR]";
	public final static boolean DEBUG = false;
	public final static String FILENAME = "filters.dat";
	public final static String DEFAULT_INCLUDE_FILTER = "playtech";

	public LinearLayout buttonBar;
	public ListView listView;
	
	public static List<AppInfo> allApps = new ArrayList<AppInfo>();
	public static List<AppInfo> filteredApps = new ArrayList<AppInfo>();
	public static List<Filter> filters = new ArrayList<Filter>();
	
	private ActionMode mActionMode;
	private boolean isNewFilter = true;
	private int currentFilterNumber = -1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //get the controls
        listView = (ListView) findViewById(R.id.listView);
        buttonBar = (LinearLayout) findViewById(R.id.buttonBar);

        
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
                
        //clicking on listview will open the application by its packagename
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AppInfo selectedApp = (AppInfo) listView.getItemAtPosition(position);
				PackageManager manager = getPackageManager();
				try {
					Intent intent = manager.getLaunchIntentForPackage(selectedApp.getPackageName());
					if (intent == null)
						throw new PackageManager.NameNotFoundException();
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					startActivity(intent);
				} catch (PackageManager.NameNotFoundException e) {
					Log.e(MainActivity.TAG_ERROR, "Failed to find an activity with package name: " + selectedApp.getPackageName());
					e.printStackTrace();
				}
			}
        });
        
        //get all the installed applications
        allApps = getAllApps();
        
        //succeeded to load previously saved filters from file?
        boolean filtersLoaded = loadFiltersFromFile();
        
        //if no filter exists, create a default one
        //if there is already a filter, activate the first one
        if (!filtersLoaded || filters.size() < 0) {
        	if(DEBUG)
        		Log.i(TAG_INFO, "There are no filters or filters file doesn't exist");
        	Filter defaultFilter = new Filter("Default", DEFAULT_INCLUDE_FILTER, "");
        	filters.add(defaultFilter);
        }
        filteredApps = filterApps(filters.get(0));
        
        // fill the list of installed apps into main view
        refreshFilterList();
        refreshAppList();
        
        Toast.makeText(getApplicationContext(), "Long-tap the tab to add/edit/remove filters", Toast.LENGTH_LONG).show();
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();

    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	saveFiltersToFile();
    	//TODO: empty the array so on resuming the app it will re-read the list
    }
    
//    @Override
//    protected void onResume() {
//    	super.onResume();
//    	
//    
//    }
    
    
    //loads all the applications that are installed on device
    private ArrayList<AppInfo> getAllApps() {
    	if(DEBUG)
    		Log.d(TAG_INFO, "Entered getAllApps");
    	
        ArrayList<AppInfo> result = new ArrayList<AppInfo>();        
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        
        for(int i=0;i<packs.size();i++) {
            PackageInfo p = packs.get(i);
            //skip applications that have no version name - these are mostly the system ones
            //also skip those which don't have package name
            if (p.versionName == null || p.packageName == null)
                continue ;
            
            AppInfo newInfo = new AppInfo();
            newInfo.setPackageName(p.packageName);
            newInfo.setAppName(p.applicationInfo.loadLabel(getPackageManager()).toString());
            newInfo.setVersionName(p.versionName);
            newInfo.setVersionCode(p.versionCode);
            newInfo.setIcon(p.applicationInfo.loadIcon(getPackageManager()));
            
            if(DEBUG) {
            	Log.d(TAG_INFO, "Trying to add new app: ");
            	Log.d(TAG_INFO, "Package: " + newInfo.getPackageName());
            	Log.d(TAG_INFO, "App name: " + newInfo.getAppname());
            }
            
            result.add(newInfo);
        }
        
    	if(DEBUG)
    		Log.d(TAG_INFO, "Exiting getAllApps");

        return result; 
    }
    
    
//    processes all applications by filtering their package names
//    excludes or includes the apps according to the filter
//    returns the list of filtered applications
    public ArrayList<AppInfo> filterApps(Filter filter) {
    	boolean shouldSkip=false;
        ArrayList<AppInfo> result = new ArrayList<AppInfo>();        
    	
    	if(DEBUG) {
    		Log.d(TAG_INFO, "Entered filterApps with parameters:");
    		Log.d(TAG_INFO, "filter name: " + filter.getName());
    		Log.d(TAG_INFO, "filter includes keywords: " + filter.getKeywordsInclude().toString());
    		Log.d(TAG_INFO, "filter excludes keywords: " + filter.getKeywordsExclude().toString());
    	}
    	
    	for(int i=0; i < allApps.size(); i++) {
    		shouldSkip = false;
    		//first of all, let's check if the app should be excluded
    		for(String keyword: filter.getKeywordsExclude()) {
    			if( keyword.equals(""))
    				continue;
    			if( ((AppInfo)allApps.get(i)).getPackageName().indexOf(keyword) > -1 ) {
    				shouldSkip = true;
    				break;
    			}
    		}
    		if(shouldSkip)
    			continue;
    		
    		//finally check if the app should be included to the list 
    		for(String keyword: filter.getKeywordsInclude()) {
    			if( keyword.equals(""))
    				continue;
    			if( ((AppInfo)allApps.get(i)).getPackageName().indexOf(keyword) > -1 ) {
    				result.add(allApps.get(i));
    				break;
    			}
    		}
    	}
		return result;
    }
    
    
    //reads the serialized filters from the file
    //empties the list of filters and refills it again
    //if the file doesn't exist, creates one but returns true
    public boolean loadFiltersFromFile() {
    	FileInputStream inStream = null;
		ObjectInputStream objectInStream = null;
		try {
			File file = getApplicationContext().getFileStreamPath(FILENAME);
			//create new file if there is no file
			if(!file.exists())
				try {
					new FileOutputStream(file, false).close();
					return false;
				} catch (FileNotFoundException e1) { e1.printStackTrace();
				} catch (IOException e1) { e1.printStackTrace();}
				
			inStream = openFileInput(FILENAME);
			objectInStream = new ObjectInputStream(inStream);
			int count = objectInStream.readInt();
			
			//empty the current filter list
			filters.clear();
			for(int i=0; i < count; i++)
				filters.add((Filter) objectInStream.readObject());
			
		} catch (Exception e) {
    		Log.e(TAG_ERROR, "Exception while reading filters from file");
    		e.printStackTrace();
    		return false;
    	} finally {
    		try {
    			if (null != inStream)	inStream.close();
    			if (null != objectInStream)	objectInStream.close();
    		} catch (Exception e) { e.printStackTrace(); }

		}
		return true;
    	
    }
    
    
    //writes the serialized filters into file
    //rewrites the file
    public boolean saveFiltersToFile() {
    	FileOutputStream outStream = null;
		ObjectOutputStream objectOutStream = null;
    	try {
    		outStream = openFileOutput(FILENAME, MODE_PRIVATE);
    		objectOutStream = new ObjectOutputStream(outStream);
    		//save the size of filters array
    		objectOutStream.writeInt(filters.size());
    		//then everything else
    		for(Filter filter: filters)
    			objectOutStream.writeObject(filter);
    		
    	} catch (Exception e) {
    		Log.e(TAG_ERROR, "Exception while saving filters to file");
    		e.printStackTrace();
    		return false;
    	} finally {
    		try {
    			if (null != outStream)	outStream.close();
    			if (null != objectOutStream)	objectOutStream.close();
    		} catch (Exception e) { }
    	}
		return true;
    }
    
    
    //refresh listview with filtered items
    public void refreshAppList() {
    	if(DEBUG)
    		Log.d(TAG_INFO, "Entered refreshAppList");
    	//this is unsafe but who cares
    	listView.setAdapter(null);  
    	AppInfoAdapter adapter = new AppInfoAdapter(this, (ArrayList<AppInfo>) filteredApps);
    	listView.setAdapter(adapter);
    }

    
    //refresh filterview when the filters are updated
    public void refreshFilterList() {
    	if(DEBUG)
    		Log.d(TAG_INFO, "Entered refreshFilterList");
    	
    	//empty the container, delete all buttons in it
    	if(buttonBar.getChildCount() > 0)
    		buttonBar.removeAllViews();
    	
    	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//create buttons from  filters, 
    	//add listeners, set tag = positions in filters array,
    	//add context menu
    	//add to linear layout
		for(int i=0; i < filters.size(); i++) {
			View filterView = inflater.inflate(R.layout.filterview, buttonBar, false);
			Button filterButton = (Button) filterView.findViewById(R.id.filterButton);
			filterButton.setText(filters.get(i).getName());
			filterButton.setTag(i);
			filterButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// refilter apps using current filter and update the apps list
					filteredApps = filterApps(filters.get((Integer)v.getTag()));
					refreshAppList();
				}
			});
			//set context menu; 
			//put current filter number into a tag of actionmode 
			//to get it out in onActionItemClicked
			filterButton.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if(null != mActionMode)
						return false;
					mActionMode = startActionMode(mActionCallback);
					mActionMode.setTag(v.getTag());
					v.setSelected(true);
					return true;
				}
			});
			
			buttonBar.addView(filterView);
		}
		buttonBar.refreshDrawableState();
    }
    
    //create context menu callback
    private ActionMode.Callback mActionCallback = new ActionMode.Callback() {
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.filter_menu, menu);
			return true;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch(item.getItemId()) {
			case R.id.add:
				addOrEditFilter(null);
				return true;
			case R.id.edit:
				//let's save the position of current filter in array
				currentFilterNumber = ((Integer)mActionMode.getTag()).intValue(); 
				addOrEditFilter(filters.get(currentFilterNumber));
				return true;
			case R.id.delete:
				deleteFilter(mActionMode.getTag());
				return true;
			default:
				return false;
			}
		}
	};
	
	//deletes a filter by its position in array
	private void deleteFilter(Object position) {
		if(DEBUG)
			Log.d(TAG_INFO, "Entered deleteFilter with position: " + position.toString());
		if(position instanceof Integer) {
			filters.remove(((Integer)position).intValue());
			mActionMode.finish();
			refreshFilterList();
		}
		
	}
	
	private void addOrEditFilter(Filter filter) {
		//if filter is null, add new filter
		isNewFilter = (null == filter);
		DialogFragment dialog = new FilterDialog();
		
		//if not, then we edit existing filter
		//and should pass all filter info to filter dialog
		if(!isNewFilter) {
			Bundle args = new Bundle();
			args.putString("titleView", getString(R.string.edit_filter) + " " + filter.getName());
			args.putString("nameEdit", filter.getName());
			args.putString("includeEdit", TextUtils.join(Filter.DELIMITER, filter.getKeywordsInclude()));
			args.putString("excludeEdit", TextUtils.join(Filter.DELIMITER, filter.getKeywordsExclude()));
			dialog.setArguments(args);
		}
		
		dialog.show(getFragmentManager(), "Some tag");
		//close the action bar
		mActionMode.finish();
	}


	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		//either save or update the filter
		Bundle args = dialog.getArguments();
		Filter filter = new Filter();
		
		//check if we've edited the existing filter
		if(currentFilterNumber >= 0) {
			//it was existing filter
			//change its values and reset currently selected number
			filter = filters.get(currentFilterNumber);
			currentFilterNumber = -1;
		}
		else
			filters.add(filter);
		filter.setName(args.getString("nameEdit"));
		filter.setKeywordsInclude(args.getString("includeEdit"));
		filter.setKeywordsExclude(args.getString("excludeEdit"));

		refreshFilterList();
		//update the list of applications with current filter
		filteredApps = filterApps(filter);
		refreshAppList();
	}


	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// nothing to do here
		
	}
    
}
