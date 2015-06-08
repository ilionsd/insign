package com.insign.insign;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.View;

import android.widget.TextView;
import com.insign.dinamic_curves.matching.matcher.SignatureMatcher;
import com.insign.dinamic_curves.matching.matcher.SignatureMatching;
import com.insign.dinamic_curves.points.SignaturePoint;
import com.insign.utils.Entry;
import com.insign.utils.EntryWrapper;
import com.insign.common.function.differentialgeometry.CubicSplineParametricCurve;
import com.insign.common.function.differentialgeometry.NaturalCubicSplineParametricCurve;
import com.insign.common.function.differentialgeometry.ParametricCurve;
import com.insign.common.function.interpolation.Interpolation;
import com.insign.common.function.Point2D;

import com.insign.dinamic_curves.Signature;
import com.insign.dinamic_curves.SignatureUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by ilion on 06.02.2015.
 */
public class MainActivity extends Activity implements View.OnTouchListener{

	TextView textView;
	View view;
	DrawView drawView;
	Paint paint = new Paint();
	List<Entry<Double, Point2D>> points;
	List<Entry<Double, Point2D>> referencePoints = null;
	Signature referenceSignature = null;
	SignatureMatcher signatureMatcher = new SignatureMatcher();
	long t0 = 0;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		textView = (TextView) findViewById(R.id.textView);
		view = findViewById(R.id.view);
		drawView = (DrawView)findViewById(R.id.canvas);
		view.setOnTouchListener(this);

		referencePoints = getReferencePoints(R.raw.signature_point_grid_1);
		referenceSignature = buildSignature(referencePoints);
		signatureMatcher.setReference(referenceSignature);
		//initialize();
	}

	private List<Entry<Double, Point2D>> getReferencePoints(int rawResId) {
		InputStream is = getResources().openRawResource(rawResId);
		Scanner scanner = new Scanner(is);
		scanner.useDelimiter(" |\n");
		List<Entry<Double, Point2D>> points = new ArrayList<Entry<Double, Point2D>>();
		while (scanner.hasNextLine()) {
			String next = scanner.next();
			double t = Double.parseDouble(next);
			next = scanner.next();
			double x = Double.parseDouble(next);
			next = scanner.next();
			double y = Double.parseDouble(next);
			Entry<Double, Point2D> entry = new EntryWrapper<Double, Point2D>(t, new Point2D(x, y));
			points.add(entry);
		}
		return points;
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
				moveEventActionMove();
				break;
			case MotionEvent.ACTION_UP:
				if (points.size() < 10)
					break;
				moveEventActionUp();
				break;
		}
		return true;
	}

	private void initialize() {
		if (points == null)
			points = new ArrayList<Entry<Double, Point2D>>();

		points.clear();

		textView.setText("List View");
		drawView.getCanvas().drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		drawView.invalidate();
	}

	private void addPoint(double x, double y, long t) {
		points.add(new EntryWrapper<Double, Point2D>((double) t, new Point2D(x, y)));
		textView.append(System.lineSeparator() + "t=" + t + "(" + x + "; " + y + ")");
	}

	private void drawCurve(ParametricCurve curve, double step, int color) {
		double tMin = curve.getParameterMin();
		double tMax = curve.getParameterMax();
		double t1 = 0, t2 = 0;
		double segmentSize = tMax - tMin;
		int segmentCount = (int)Math.ceil(segmentSize / step);
		double actualStep = segmentSize / segmentCount;

		paint.setColor(color);
		paint.setStrokeWidth(3);

		t2 = tMin;
		for (int segment = 0; segment < segmentCount; segment++) {
			t1 = tMin + actualStep * segment;
			t2 = tMin + actualStep * (segment + 1);
			Point2D t1Value = curve.valueIn(t1);
			Point2D t2Value = curve.valueIn(t2);
			drawView.getCanvas().drawLine((float)t1Value.getX(), (float)t1Value.getY(), (float)t2Value.getX(), (float)t2Value.getY(), paint);
		}
	}

	private void drawSkeleton(Signature signature, int color) {
		paint.setColor(color);
		paint.setStrokeWidth(7);

		for (SignaturePoint point: signature.getSkeleton()) {
			drawView.getCanvas().drawPoint((float)point.getValue().getX(), (float)point.getValue().getY(), paint);
		}
	}

	private void moveEventActionMove() {
		int lastIndex = points.size() - 1;
		float startX = (float)points.get(lastIndex - 1).getValue().getX();
		float startY = (float)points.get(lastIndex - 1).getValue().getY();
		float stopX = (float)points.get(lastIndex).getValue().getX();
		float stopY = (float)points.get(lastIndex).getValue().getY();
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(10);
		drawView.getCanvas().drawLine(startX, startY, stopX, stopY, paint);
		drawView.invalidate();
	}

	private void moveEventActionUp() {
		long start = System.currentTimeMillis();
		Signature signature = buildSignature(points);
		long execTime = System.currentTimeMillis() - start;

		SignatureMatching matching = signatureMatcher.match(signature);

		drawSkeleton(signature, Color.BLUE);
		paint.setColor(Color.RED);
		paint.setTextSize(70);
		//drawView.getCanvas().drawColor(Color.WHITE);
		drawView.getCanvas().drawText(Double.toString(execTime), 100.0F, 100.0F, paint);
		drawView.invalidate();
	}

	private static String pointsToString(List<Entry<Double, Point2D>> points) {
		StringBuilder sb = new StringBuilder();
		for (Entry<Double, Point2D> entry : points) {
			sb.append(entry.getKey() + " " + entry.getValue().getX() + " " + entry.getValue().getY() + "\n");
		}
		return sb.toString();
	}

	private Signature buildSignature(List<Entry<Double, Point2D>> points) {
		List<Entry<Double, Point2D>> scaledPoints = scale(points);
		CubicSplineParametricCurve curve = Interpolation.ParametricCurves.bySmoothingSpline(scaledPoints, 0.5);
		NaturalCubicSplineParametricCurve naturalCurve = NaturalCubicSplineParametricCurve.fromCurve(curve);
		Signature signature = SignatureUtils.createFromCurve(naturalCurve);
		return signature;
	}

	private List<Entry<Double, Point2D>> scale(List<Entry<Double, Point2D>> points) {
		double minX = Double.MAX_VALUE,
				minY = Double.MAX_VALUE,
				maxX = 0,
				maxY = 0;
		for (Entry<Double, Point2D> entry : points) {
			minX = Math.min(minX, entry.getValue().getX());
			minY = Math.min(minY, entry.getValue().getY());
			maxX = Math.max(maxX, entry.getValue().getX());
			maxY = Math.max(maxY, entry.getValue().getY());
		}
		double scaleCoefficient = Math.max(maxX - minX, maxY - minY);
		List<Entry<Double, Point2D>> scaled = new ArrayList<Entry<Double, Point2D>>();
		for (Entry<Double, Point2D> entry : points) {
			double scaledX = (entry.getValue().getX() - minX) / scaleCoefficient;
			double scaledY = (entry.getValue().getY() - minY) / scaleCoefficient;
			Entry<Double, Point2D> scaledEntry = new EntryWrapper<Double, Point2D>(entry.getKey(), new Point2D(scaledX, scaledY));
			scaled.add(scaledEntry);
		}
		return scaled;
	}

}



















