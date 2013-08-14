package fr.projetcookie.boussole;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {

	
	private float direction = 0;
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private boolean firstDraw;
	
	
	public CompassView(Context context) {
		super(context);
		init();
	}
	
	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public CompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
		paint.setColor(Color.BLACK);
		paint.setTextSize(30);
		
		firstDraw = true;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int cxCompass = getMeasuredWidth()/2;
		int cyCompass = getMeasuredHeight()/2;
		float radiusCompass;
		
		if(cxCompass > cyCompass){
		 radiusCompass = (float) (cyCompass * 0.9);
		}
		else{
		 radiusCompass = (float) (cxCompass * 0.9);
		}
		canvas.drawCircle(cxCompass, cyCompass, radiusCompass, paint);
		canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
		
		if(!firstDraw){
		
			float pointeX = (float)(cxCompass + radiusCompass * Math.sin((double)(-direction) * Math.PI/180));
			float pointeY = (float)(cyCompass - radiusCompass * Math.cos((double)(-direction) * Math.PI/180));
		
			
			canvas.drawLine(cxCompass, cyCompass, pointeX, pointeY,paint);
			
			canvas.drawText(String.valueOf(direction), cxCompass, cyCompass, paint);
		}
	
	}
	
	public void updateDirection(float dir) {
		firstDraw = false;
		direction = Math.round(dir*10.0)/10.0f;
		invalidate();
	}


}
