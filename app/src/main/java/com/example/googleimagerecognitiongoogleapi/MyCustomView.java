package com.example.googleimagerecognitiongoogleapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.nio.file.Path;
import java.util.Random;

public class MyCustomView extends View {
    private Paint _myPaint = new Paint();
    private static int box_color_val;
    private static int text_color_val;
    private float _left;
    private float _top;
    private float _right;
    private float _bottom;
    private String _name;

    public MyCustomView(Context context) {
        super(context);
        init(null, 0);
    }

    public MyCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs,0);
    }
    public MyCustomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public static void setColor(int p) {
        box_color_val = p;
    }

    public static void setTextColor(int p) {
        text_color_val = p;
    }

    private void init(AttributeSet attrs, int defStyle) {
        _myPaint.setAntiAlias(true);
        _myPaint.setStyle(Paint.Style.STROKE);
        _myPaint.setStrokeWidth(1f);
        _myPaint.setTextSize(45);
        _myPaint.setColor(Color.BLACK);
        _left = 0;
        _top = 0;
        _right = 0;
        _bottom = 0;
        _name = "";
    }

    public void set_left(Double left){
        _left = left.floatValue();
    }

    public void set_boxPaint(int p) {
        box_color_val = p;
        _myPaint.setColor(p);
    }
    public void set_top(Double top) {
        _top = top.floatValue();
    }
    public void set_right(Double right) {
        _right = right.floatValue();
    }
    public void set_bottom(Double bottom) {
        _bottom = bottom.floatValue();
    }
    public void set_name(String name) {
        _name = name;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (_left == 0f) {
            return;
        }
        _myPaint.setColor(box_color_val);
        _myPaint.setStyle(Paint.Style.STROKE);
        _myPaint.setStrokeWidth(4f);
        canvas.drawRect(_left*getWidth(), _top*getHeight(), _right*getWidth(), _bottom*getHeight(), _myPaint);
        _myPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        _myPaint.setColor(text_color_val);
        _myPaint.setStrokeWidth(1f);
        canvas.drawText(_name,_left*getWidth(),_bottom*getHeight() + 20, _myPaint);
        invalidate();
    }

    public void update() {
        invalidate();
    }

}
