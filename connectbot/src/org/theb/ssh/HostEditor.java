/*
 * Copyright (C) 2007 Kenny Root (kenny at the-b.org)
 * 
 * This file is part of Connectbot.
 *
 *  Connectbot is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Connectbot is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Connectbot.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.theb.ssh;

import org.theb.ssh.R;
import org.theb.provider.HostDb;
import org.theb.provider.HostDb.Hosts;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;

public class HostEditor extends Activity {
	public static final String EDIT_HOST_ACTION = "org.theb.ssh.action.EDIT_HOST";

	private static final String[] PROJECTION = new String[] { HostDb.Hosts._ID,
			HostDb.Hosts.NICKNAME, HostDb.Hosts.HOSTNAME,
			HostDb.Hosts.USERNAME, HostDb.Hosts.PORT, HostDb.Hosts.EMULATION,
			HostDb.Hosts.SCROLLBACK, };

	private static final int INDEX_NICKNAME = 1, INDEX_HOSTNAME = 2,
			INDEX_USERNAME = 3, INDEX_PORT = 4, INDEX_EMULATION = 5,
			INDEX_SCROLLBACK = 6;
    
    // Set up distinct states that the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;
    
	private EditText mNickname, mHostname, mUsername, mPort, mScrollback;
	private Spinner mEmulation;

	// Cursor that will provide access to the host data we are editing
    private Cursor mCursor;
    
	private int mState;
	private Uri mURI;
	
    @Override
    public void onCreate(Bundle savedValues) {
        super.onCreate(savedValues);
        this.setContentView(R.layout.act_hosteditor);
        
        // Set up click handlers for text fields and button
		this.mNickname = (EditText) findViewById(R.id.edit_nickname);
		this.mHostname = (EditText) findViewById(R.id.edit_hostname);
		this.mUsername = (EditText) findViewById(R.id.edit_username);
		this.mPort = (EditText) findViewById(R.id.edit_port);
		this.mEmulation = (Spinner) findViewById(R.id.edit_emulation);
		this.mScrollback = (EditText) findViewById(R.id.edit_scrollback);
        
        // Do some setup based on the action being performed.
        final Intent intent = getIntent();
        final String action = intent.getAction();
        
        if (Intent.ACTION_INSERT.equals(action)) {
        	mState = STATE_INSERT;
        	mURI = getContentResolver().insert(intent.getData(), null);
        
        	// If we were unable to create a new note, then just finish
        	// this activity.  A RESULT_CANCELED will be sent back to the
        	// original activity if they requested a result.
        	if (mURI == null) {
            	Log.e("SSH", "Failed to insert new host into " + getIntent().getData());
            	finish();
            	return;
        	}
        	
        	// The new entry was created, so assume all will end well and
            // set the result to be returned.
        	intent.putExtra(Intent.EXTRA_TEXT, mURI.toString());
            setResult(RESULT_OK, intent);
        } else {
        	// Editing is the default state.
       		mState = STATE_EDIT;
        	
        	// Get the URI of the host whose properties we want to edit
            mURI = getIntent().getData();
        }
        
        // Get a cursor to access the host data
        this.mCursor = managedQuery(mURI, PROJECTION, null, null);
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Initialize the text with the host data
    	if(mCursor != null) {
    		mCursor.moveToFirst();
    		
    		this.mNickname.setText(mCursor.getString(HostEditor.INDEX_NICKNAME));
    		this.mHostname.setText(mCursor.getString(HostEditor.INDEX_HOSTNAME));
    		this.mUsername.setText(mCursor.getString(HostEditor.INDEX_USERNAME));
    		this.mPort.setText(mCursor.getString(HostEditor.INDEX_PORT));
    		//this.emulation.setText(cursor.getString(this.INDEX_EMULATION));
    		this.mScrollback.setText(mCursor.getString(HostEditor.INDEX_SCROLLBACK));
    		
    	}
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	
    	// Write the text back into the cursor
    	if(mCursor != null) {
    		ContentValues values = new ContentValues();
    		values.put(Hosts.NICKNAME, mNickname.getText().toString());
    		values.put(Hosts.HOSTNAME, mHostname.getText().toString());
    		values.put(Hosts.USERNAME, mUsername.getText().toString());
    		//mEmulation.getSelectedItemPosition();
    		int port;
    		try {
    			port = Integer.parseInt(mPort.getText().toString());
    		} catch (Exception e) {
    			port = 22;
    		}
    		values.put(Hosts.PORT, port);
 
    		int scrollback;
    		try {
    			scrollback = Integer.parseInt(mScrollback.getText().toString());
    		} catch (Exception e) {
    			scrollback = 1000; // TODO: grab from R.whatever
    		}
    		values.put(Hosts.SCROLLBACK, scrollback);
    		
    		getContentResolver().update(mURI, values, null, null);
    	}
    }
    
    private final void cancelEdit() {
    	if (mCursor != null) {
    		if (mState == STATE_EDIT) {
    			mCursor.deactivate();
    			mCursor = null;
    		} else if (mState == STATE_INSERT) {
    			deleteHost();
    		}
    	}
    }
    
    private final void deleteHost() {
    	if (mCursor != null) {
    		mHostname.setText("");
    		mUsername.setText("");
    		mPort.setText("");
    		getContentResolver().delete(mURI, null, null);
    		mCursor.deactivate();
    		mCursor = null;
    	}
    }
    
}
