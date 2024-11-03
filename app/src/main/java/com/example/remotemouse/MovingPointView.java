package com.example.remotemouse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class MovingPointView extends View {
    private float x = 0;
    private float y = 0;
    private Paint paint;

    public MovingPointView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, y, 20, paint); // Dessine un point de rayon 20
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        invalidate(); // Redessine la vue
    }
}
