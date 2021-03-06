/*
Copyright ©2011 Ezio Querini

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.naofum.epubconverter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class epubconverter extends Activity {
	String filename = "";
	static String path;
	private SharedPreferences sharedPref;
	private final String PDF_EXT = ".pdf";
	private final String EPUB_EXT = " - epubconverter.epub";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= 23) {
			if (ContextCompat.checkSelfPermission(this,
					android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED
					|| ContextCompat.checkSelfPermission(this,
					android.Manifest.permission.READ_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this,
						new String[]{
								android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
								android.Manifest.permission.READ_EXTERNAL_STORAGE},
						1);
			}
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


        
		((Button) findViewById(R.id.convert)).setOnClickListener(mConvertListener);
		((Button) findViewById(R.id.preview)).setOnClickListener(mPreviewListener);
		
		
		
	    Intent intent = getIntent();
		
		// Get last path
		sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		path = sharedPref.getString("path", Environment.getExternalStorageDirectory().getPath());

	    // To get the action of the intent use
	    String action = intent.getAction();
	    if (action.equals(Intent.ACTION_VIEW)) {
			filename = intent.getDataString().replaceAll("%20", " ");
			if (filename.startsWith("file://")) {
				filename = filename.replace("file://", "");
				pickActivity();
			}
	    }

	}

	// Inflate menu
	@SuppressWarnings("deprecation")
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.prefs), 1);
		return true;
	}

	// Menu item selected
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.prefs:
			startActivity(new Intent(epubconverter.this, Prefs.class));
			return true;
		case R.id.info:
			startActivity(new Intent(epubconverter.this, Info.class));
			return true;
		case R.id.license:
			startActivity(new Intent(epubconverter.this, License.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Convert button pressed
	View.OnClickListener mConvertListener = new OnClickListener() {
		public void onClick(View v) {
			if (Convert.started()) {
				// Conversion already started, show progress
				if (Convert.working()) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.cip), Toast.LENGTH_SHORT).show();
				}
				startActivity(new Intent(epubconverter.this, Convert.class));
			} else {
				// Select a file
				Intent chooseFile = new Intent(epubconverter.this, FileChooser.class);
				chooseFile.putExtra("path", path);
				chooseFile.putExtra("filter", PDF_EXT);
				startActivityForResult(chooseFile, 0);
			}
		}
	};

	// Preview button pressed
	View.OnClickListener mPreviewListener = new OnClickListener() {
		public void onClick(View v) {
			// Select a file
			Intent chooseFile = new Intent(epubconverter.this, FileChooser.class);
			chooseFile.putExtra("path", path);
			chooseFile.putExtra("filter", EPUB_EXT);
			startActivityForResult(chooseFile, 0);
		}
	};

	// File selected
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			filename = data.getAction();
			pickActivity();		}
	}

	// Start conversion or preview
	protected void pickActivity() {
		path = filename.substring(0, filename.lastIndexOf('/', filename.length()) + 1);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("path", path);
		editor.commit();

		if (filename.endsWith(PDF_EXT)) {
			Intent convert = new Intent(epubconverter.this, Convert.class);
			convert.putExtra("filename", filename);
			startActivity(convert);
		} else if (filename.endsWith(EPUB_EXT)) {
			Intent preview = new Intent(epubconverter.this, Preview.class);
			preview.putExtra("filename", filename);
			startActivity(preview);
		} else {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrongfile), Toast.LENGTH_SHORT).show();
		}
	}
}