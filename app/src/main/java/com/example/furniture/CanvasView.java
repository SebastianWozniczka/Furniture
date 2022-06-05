package com.example.furniture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {
    public CanvasView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCirle(canvas);

    }

    private void drawCirle(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(50, 60, 50, paint);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(200, 60, 50, paint);
    }



}
