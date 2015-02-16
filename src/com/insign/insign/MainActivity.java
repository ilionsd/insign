package com.insign.insign;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.View;

import android.widget.TextView;
import com.insign.common.function.Spline;
import com.insign.common.function.Interpolation;
import com.insign.common.function.Point2D;

import java.util.ArrayList;

/**
 * Created by ilion on 06.02.2015.
 */
public class MainActivity extends Activity implements View.OnTouchListener{

	TextView textView;
	View view;
	DrawView drawView;
	Paint paint = new Paint();
	ArrayList<Point2D> coords;
	ArrayList<Point2D> xt;
	ArrayList<Point2D> yt;
	long t0 = 0;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		textView = (TextView) findViewById(R.id.textView);
		view = findViewById(R.id.view);
		drawView = (DrawView)findViewById(R.id.canvas);
		view.setOnTouchListener(this);

		//initialize();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				initialize();
				t0 = event.getEventTime();
				addPoint(event.getX(), event.getY(), 0);
				break;
			case MotionEvent.ACTION_MOVE:
				addPoint(event.getX(), event.getY(), event.getEventTime() - t0);
				int lastIndex = coords.size() - 1;
				float startX = (float)coords.get(lastIndex - 1).getX();
				float startY = (float)coords.get(lastIndex - 1).getY();
				float stopX = (float)coords.get(lastIndex).getX();
				float stopY = (float)coords.get(lastIndex).getY();
				paint.setColor(Color.WHITE);
				paint.setStrokeWidth(10);
				drawView.getCanvas().drawLine(startX, startY, stopX, stopY, paint);
				drawView.invalidate();
				break;
			case MotionEvent.ACTION_UP:
				Point2D[] xtArr = xt.toArray(new Point2D[xt.size()]);
				long start = System.currentTimeMillis();
				Spline spline = Interpolation.SmoothingSpline(xtArr, 0.5);
				long execTime = System.currentTimeMillis() - start;

				paint.setColor(Color.RED);
				paint.setTextSize(70);
				drawView.getCanvas().drawColor(Color.WHITE);
				drawView.getCanvas().drawText(Double.toString(execTime), 100.0F, 100.0F, paint);
				drawView.invalidate();
				break;
		}
		return true;
	}

	private void initialize() {
		if (coords == null)
			coords = new ArrayList<Point2D>();
		if (xt == null)
			xt = new ArrayList<Point2D>();
		if (yt == null)
			yt = new ArrayList<Point2D>();

		coords.clear();
		xt.clear();
		yt.clear();

		textView.setText("List View");
		drawView.getCanvas().drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		drawView.invalidate();
	}

	private void addPoint(double x, double y, long t) {
		coords.add(new Point2D(x, y));
		xt.add(new Point2D(t, x));
		yt.add(new Point2D(t, y));
		textView.append(System.lineSeparator() + "t=" + t + "(" + x + "; " + y + ")");
	}

}



















