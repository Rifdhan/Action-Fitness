package com.actionfitness;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.actionfitness.graphing.RealtimeChartSurfaceView;

import android.support.v7.app.ActionBar.LayoutParams;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Build;

public class PlayActivity extends ActionBarActivity {

	private boolean running = true;
	private boolean error = false;
	
	static LinearLayout fillLayout = null;
	static TextView percentFullBox = null;
	static TextView angleBox = null, weightBox = null;
	static LinearLayout angleGraphLayout = null;
	static LinearLayout weightGraphLayout = null;
	static Button startStopButton = null;

	private RealtimeChartSurfaceView angleChart = null;
	private RealtimeChartSurfaceView weightChart = null;
	
	// WiFi connection information
	private String arduinoIP = "10.0.0.102"; // 192.168.43.19 Rifdhan, 192.168.43.70 Steve
	
    // Arduino WiFi variables
	Socket socket = null;
	DataOutputStream dataOutputStream = null;
	DataInputStream dataInputStream = null;
	static public int angleRead = -1;
	static public int weightRead = -1;
	
	boolean centerSet = false;
	double centerOffset = 0;
	double currentOffset = 0;
	double currentPercent = 0;
	boolean overflowed = false;

	float[] angleData = new float[350];
	float[] weightData = new float[350];
	int currentIndex = 0;

	public static List<Double> angleList = new ArrayList<Double> ();
	public static List<Double> weightList = new ArrayList<Double> ();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);

		if (savedInstanceState == null) {
			//getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		// Get layout objects
		fillLayout = (LinearLayout) findViewById(R.id.fillLayout);
		percentFullBox = (TextView) findViewById(R.id.percentFullBox);
		angleBox = (TextView) findViewById(R.id.angleBox);
		weightBox = (TextView) findViewById(R.id.weightBox);
		angleGraphLayout = (LinearLayout) findViewById(R.id.angleGraphLayout);
		weightGraphLayout = (LinearLayout) findViewById(R.id.weightGraphLayout);
		startStopButton = (Button) findViewById(R.id.start_stop_button);
		
		// Add angle chart to the screen
		angleChart = new RealtimeChartSurfaceView(this);
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		angleGraphLayout.addView(angleChart, params1);
		
		// Add weight chart to the screen
		weightChart = new RealtimeChartSurfaceView(this);
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		weightGraphLayout.addView(weightChart, params2);
		
		// Open socket for network communication
		Thread openSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
            	Log.d("information", "Opening socket for Arduino communication");
            	
            	try {
            		socket = new Socket(arduinoIP, 7);
            		
            		dataOutputStream = new DataOutputStream(socket.getOutputStream());
            		dataInputStream = new DataInputStream(socket.getInputStream());
            	} catch (UnknownHostException e) {
            		Log.d("error", "Error opening socket: unknown host exception");
            		e.printStackTrace();
            	} catch (IOException e) {
            		Log.d("error", "Error opening socket: input/output exception");
            		e.printStackTrace();
            	}
            };
        });
           
        openSocketThread.start();
    }
	
	// Start the main thread
	@Override
	public void onResume() {
		super.onResume();
		
		// Main loop thread
		Thread mainLoopThread = new Thread() {
            public void run() {
                while (running) {
                    PlayActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        	
                        	// Get new data from Arduino and perform handshake
							Thread networkCommunicationThread = new Thread(
							    new Runnable() {
								    @Override
								    public void run() {
										try {
											// Check if network connection is working
											if ((socket == null || dataInputStream == null || dataInputStream == null) && running) {
												Log.d("error", "Error: sockets and/or I/O streams are null - check Arduino's IP");
												//running = false;
												//error = true;
												return;
											}
											
											// Write byte to socket
											dataOutputStream.writeByte(48);
											
											// Read bytes from socket
											angleRead = dataInputStream.readUnsignedByte();
											Log.d("information", "Read byte: " + angleRead);
											weightRead = dataInputStream.readUnsignedByte();
											Log.d("information", "Read byte: " + weightRead);
											
										} catch (UnknownHostException e) {
											Log.d("error", "Error: unknown host exception during Arduino I/O");
											e.printStackTrace();
										} catch (IOException e) {
											Log.d("error", "Error: inout/output exception during Arduino I/O");
											e.printStackTrace();
										}
									};
								});

							networkCommunicationThread.start();
							
							// Check for errors
							if(angleRead == -1 || weightRead == -1 || error) {
								//networkErrorPopup();
							}
							
							// Update latest values to text boxes
							angleBox.setText("Angle: " + angleRead);
							weightBox.setText("Force: " + weightRead);
							
							// Calculate the current offset from center
							currentOffset = ((double)angleRead / 255) * 100 - centerOffset;
                            
							// Display current offset or fill amount as necessary
							if(centerSet) {
								// Increase fill amount as required
								currentPercent += Math.abs(currentOffset * 0.1);
								
								// Check for overflow
								if(currentPercent >= 100) {
									currentPercent = 100;
								}
								
								// Save latest data to lists and logs
								angleList.add(currentPercent);
								weightList.add((double)weightRead);
								appendLog("File3 Nitin:Angle index: " + angleList.size() + " Angle: " + angleList.get(angleList.size() - 1) + " Weight Index: " + weightList.size() + " Weight Value: " + weightList.get(weightList.size() - 1));
								
								Log.d("info", "Angle " + angleList.size() + ": " + angleList.get(angleList.size() - 1));
								Log.d("info", "Weight " + weightList.size() + ": " + weightList.get(weightList.size() - 1));
								
								// Update percent full text
								percentFullBox.setText("Bucket: " + ((double)Math.round(currentPercent * 10) / 10) + "%");
								
								// Check if the user has lost
								if(currentPercent == 100 && !overflowed) {
									overflowed = true;
									running = false;
									startStopButton.setText("View Results");
									overflowPopup();
								}
							} else {
								// Display current offset
								currentPercent = ((double)angleRead / 255) * 100;
							}
							
							// Update screen with current values
        					DrawFill();
							
							// Update angle graph
							angleData[currentIndex] = (float) angleRead;
							angleChart.setChartData(angleData);
        					
        					// Update weight graph
							weightData[currentIndex] = (float) weightRead;
							weightChart.setChartData(weightData);
							
							// Advance counter
							currentIndex ++;
							if(currentIndex >= 350) {
								currentIndex = 0;
							}
                        }
                    });
                    
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        
        mainLoopThread.start();
	}
	
	public void DrawFill() {
		// Initialize fill drawer object
		FillDrawer fillDrawer = new FillDrawer(this, currentPercent);
		
		// Remove any existing drawings
		fillLayout.removeAllViews();
		
		// Update the graphic on-screen
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		fillLayout.addView(fillDrawer, params);
		
	}
	
	// When the start/stop button is pressed
	public void startStopPressed(View v) {
		// Don't do anything if an error was encountered
		if(error) {
			return;
		}
		
		// Check if the game has started already, if so, end it or move to results activity
		if(centerSet) {
			// Check if the game has ended, if not, end it
			if(running) {
				running = false;
				startStopButton.setText("View Results");
			// If so, switch to results activity
			} else {
				Intent switchActivity = new Intent(this, ResultsActivity.class);
				startActivity(switchActivity);
			}
		// If not, start the game
		} else {
			centerOffset = currentOffset;
			currentPercent = 0;
			centerSet = true;
			startStopButton.setText("Stop");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.play, menu);
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

	// Arduino communication issue pop-up box
	public void networkErrorPopup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Network Error")
				.setMessage("Unable to establish a network connection with the Arduino. Try resetting the Arduino and trying again.")
				.setPositiveButton("OK", null);

		builder.create().show();
	}

	// Player lost pop-up box
	public void overflowPopup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("You've Lost!")
				.setMessage("The bucket has overflowed!")
				.setPositiveButton("OK", null);

		builder.create().show();
	}
	
	// Saves data to log files
	public void appendLog(String text)
	{       
		File logFile = new File("sdcard/log.file");
		if (!logFile.exists())
		{
			try
			{
				logFile.createNewFile();
			} 
			catch (IOException e)
			{
				Log.d("error", "Error: unable to create log file.");
				e.printStackTrace();
			}
		}
		
		try
		{
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
			buf.append(text);
			buf.newLine();
			buf.close();
		}
		catch (IOException e)
		{
			Log.d("error", "Error: unable to write data to log file.");
			e.printStackTrace();
		}
	}
}
