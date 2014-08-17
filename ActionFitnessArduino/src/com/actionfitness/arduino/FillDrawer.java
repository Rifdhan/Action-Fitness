package com.actionfitness.arduino;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class FillDrawer extends View {

	// Edge boundaries for the fill region
	private static final int leftMargin = 25;
	private static final int rightMargin = 25;
	private static final int bottomMargin = 25;
	private static final int topMargin = 25;

	// Paints
	Paint fillPaint = new Paint();
	
	// Height of fill to draw
	double percentFilled = 0;
	
	// Constructor
	public FillDrawer(Context context, double percent) {
		super(context);
		
		// Initialize fill paint
		fillPaint.setColor(Color.parseColor("#0000ff"));
		
		// Get height
		percentFilled = percent;
	}

	// Draw the fill during the draw method
	@Override
	public void onDraw(Canvas c) {
		// Get canvas dimensions
		int width = c.getWidth();
		int height = c.getHeight();
		
		double scaledHeightToDraw = height - (height - topMargin) * (percentFilled / 100);
		
		// Draw fill box
		c.drawRect(0, Math.round(scaledHeightToDraw), width, height, fillPaint);
	}
}
