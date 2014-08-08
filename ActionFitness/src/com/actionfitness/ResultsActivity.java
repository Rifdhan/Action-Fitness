package com.actionfitness;

import java.util.ArrayList;
import java.util.List;

import com.actionfitness.graphing.RealtimeChartSurfaceView;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.LayoutParams;
import android.support.v4.app.Fragment;
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

public class ResultsActivity extends ActionBarActivity {
	
	static LinearLayout angleGraphLayout = null;
	static LinearLayout weightGraphLayout = null;
	static TextView finalScoreBox = null;

	private RealtimeChartSurfaceView angleChart = null;
	private RealtimeChartSurfaceView weightChart = null;

	public List<Double> angleList;
	public List<Double> weightList;
	private float[] angleData;
	private float[] weightData;
	double finalScore = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		
		// Get layout objects
		angleGraphLayout = (LinearLayout) findViewById(R.id.angleResultsLayout);
		weightGraphLayout = (LinearLayout) findViewById(R.id.weightResultsLayout);
		finalScoreBox = (TextView) findViewById(R.id.finalScoreBox);
		
		// Add angle chart to the screen
		angleChart = new RealtimeChartSurfaceView(this);
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		angleGraphLayout.addView(angleChart, params1);
		
		// Add weight chart to the screen
		weightChart = new RealtimeChartSurfaceView(this);
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		weightGraphLayout.addView(weightChart, params2);
		
		// Get chart data from previous activity
		angleList = PlayActivity.angleList;
		weightList = PlayActivity.weightList;
		
		// Check that at least some data exists
		if(angleList.size() <= 0 || weightList.size() <= 0) {
			finalScoreBox.setText("Error: no data");
			return;
		}
		
		// Convert data from lists to arrays, compressing everything to 350 values
		angleData = new float[350];
		weightData = new float[350];
		for(int i = 0; i < 350; i ++) {
			angleData[i] = angleList.get((int) Math.round(i * ((double)(angleList.size() - 1) / 350))).floatValue();
			weightData[i] = weightList.get((int) Math.round(i * ((double)(angleList.size() - 1) / 350))).floatValue();
			finalScore += angleData[i];
			//Log.d("info", "Angle " + i + ": " + angleList.get(i));
			//Log.d("info", "Weight " + i + ": " + weightList.get(i));
			Log.d("info", "Angle " + i + ": " + angleData[i]);
			Log.d("info", "Weight " + i + ": " + weightData[i]);
		}
		
		// Draw graphs with data
		angleChart.setChartData(angleData);
		weightChart.setChartData(weightData);
		
		// Set final score
		finalScoreBox.setText("Final Score: " + Math.round(finalScore));
	}
	
	@Override
	public void onBackPressed() {
		// Do nothing
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.results, menu);
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

}
