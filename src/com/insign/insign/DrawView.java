package com.insign.insign;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ilion on 13.02.2015.
 */
public class DrawView extends View {

	private Bitmap bitmap;
	private Canvas canvas;
	private Paint paint = new Paint();

	public DrawView(Context context) {
		super(context);

	}

	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		//canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.OVERLAY);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(bitmap, 0, 0, paint);
	}

	public Canvas getCanvas() {
		return canvas;
	}


}
