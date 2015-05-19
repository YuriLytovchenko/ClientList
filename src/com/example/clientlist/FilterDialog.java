package com.example.clientlist;
import com.example.clientlist.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class FilterDialog extends DialogFragment {
	
	public TextView titleView;
	public EditText nameEdit, includeEdit, excludeEdit;
	public TextView helpView;
	
	public interface FilterDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);
		public void onDialogNegativeClick(DialogFragment dialog);
	}
	
	FilterDialogListener mListener;
	
	//instantiate the listener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		//check if host activity has our interface
		try {
			mListener = (FilterDialogListener) activity;
		} catch (ClassCastException e) {
			Log.e(MainActivity.TAG_ERROR, "The activity doesn't implement the interface for FilterDialog interaction");
		}
	}
	
	//add custom view and two buttons to the dialog
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_layout, null);
		
		titleView = (TextView)dialogView.findViewById(R.id.title);
		nameEdit = (EditText)dialogView.findViewById(R.id.name);
		includeEdit = (EditText)dialogView.findViewById(R.id.include);
		excludeEdit = (EditText)dialogView.findViewById(R.id.exclude);
		helpView = (TextView)dialogView.findViewById(R.id.help_text);
		
		//define arguments as final, so we can access them later in save button listener
		//if null was passed, create new bundle
		Bundle args = (null == getArguments()) ? new Bundle() : getArguments();
		
		//if there are any arguments, then we edit existing filter
		//and should fill its values into edits
		if(null != getArguments()) {
			titleView.setText(args.getString("titleView"));
			nameEdit.setText(args.getString("nameEdit"));
			includeEdit.setText(args.getString("includeEdit"));
			excludeEdit.setText(args.getString("excludeEdit"));
		}
		else {
			titleView.setText(getString(R.string.add_filter));
		}
		
		builder.setView(dialogView);
		//positive button listener will be defined during onShow
		//so we can prevent the dialog from closing with wrong data entered
		builder.setPositiveButton("Save", null);
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onDialogNegativeClick(FilterDialog.this);
			}
		});
		
		//change layout params programm-ly because it doesn't work via XML
		
		
		final AlertDialog result = builder.create();
		
		//rewrite the onClick listener of save button
		//to handle wrong data
		result.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Button saveButton = result.getButton(AlertDialog.BUTTON_POSITIVE);
				saveButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
							//check if the name is empty
							if(nameEdit.getText().toString().equals("")) {
								helpView.setText("Filter name cannot be empty\n");
							}
							else {
								//if both keywords fields are empty, then the filter doesn't make sense
								if(includeEdit.getText().toString().equals("") && 
										excludeEdit.getText().toString().equals("")) {
									helpView.setText("Please specify at least one keyword\n");
								}
								else {
									//everything's OK
									//put all values into a bundle so the activity could read them
									//TODO returns null if new filter is added
									
									Bundle newArgs = new Bundle();
									newArgs.putString("nameEdit", nameEdit.getText().toString());
									newArgs.putString("includeEdit", includeEdit.getText().toString());
									newArgs.putString("excludeEdit", excludeEdit.getText().toString());
									
									//cannot reuse old arguments bundle
									//so creating new dialog is necessary
									FilterDialog fd = new FilterDialog();
									fd.setArguments(newArgs);
									
									mListener.onDialogPositiveClick(fd);
									result.dismiss();
								}
									
							}
							
						}
				});
				
			}
		});
		return result;
		
	}

	public FilterDialog() {
		// Auto-generated constructor stub
	}

}
