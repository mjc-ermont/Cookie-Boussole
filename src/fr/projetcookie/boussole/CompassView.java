package fr.projetcookie.boussole;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {
	private float direction = 0;
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private boolean firstDraw;
	
	private float north = 0;
	private boolean drawNorth = false;
	private Paint redpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint textpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
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
		paint.setStrokeWidth(10);
		paint.setColor(Color.WHITE);
		paint.setTextSize(30);

		redpaint.setStyle(Paint.Style.STROKE);
		redpaint.setStrokeWidth(5);
		redpaint.setColor(Color.RED);
		redpaint.setTextSize(30);
		
		textpaint.setStyle(Paint.Style.STROKE);
		textpaint.setStrokeWidth(1);
		textpaint.setColor(Color.WHITE);
		textpaint.setTextSize(40);
		
		firstDraw = true;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		float cxCompass = getMeasuredWidth()/2;
		float cyCompass = getMeasuredHeight()/2;
		float radiusCompass;
		
		if(cxCompass > cyCompass){
		 radiusCompass = (float) (cyCompass * 0.9);
		}
		else{
		 radiusCompass = (float) (cxCompass * 0.9);
		}
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.FILL);
		canvas.drawCircle(cxCompass, cyCompass, radiusCompass, paint);
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.STROKE);
		canvas.drawCircle(cxCompass, cyCompass, radiusCompass, paint);
		
		//if(!firstDraw){
			drawArrow(canvas, direction, Color.WHITE);
			drawArrow(canvas, direction-180, Color.GRAY);
		/*float pointeX = (float)(cxCompass + radiusCompass * Math.sin((double)(-direction) * Math.PI/180));
			float pointeY = (float)(cyCompass - radiusCompass * Math.cos((double)(-direction) * Math.PI/180));
		
			
			canvas.drawLine(cxCompass, cyCompass, pointeX, pointeY,paint);
			*/canvas.drawText(String.valueOf(direction), cxCompass-radiusCompass, cyCompass-radiusCompass, textpaint );
			
			if(drawNorth) {
				float pointeX = (float)(cxCompass + radiusCompass * Math.sin((double)(-north) * Math.PI/180));
				float pointeY = (float)(cyCompass - radiusCompass * Math.cos((double)(-north) * Math.PI/180));
							
				canvas.drawLine(cxCompass, cyCompass, pointeX, pointeY,redpaint);
			}
	//	}
	
	}
	
	public void updateDirection(float dir) {
		firstDraw = false;
		direction = Math.round(dir*10.0)/10.0f;
		invalidate();
	}
	
	public void drawNorth(boolean on) {
		drawNorth = on;
		invalidate();
	}
	
	public void setNorth(float dir) {
		north = Math.round(dir*10.0)/10.0f;
		invalidate();
	}

	private void drawArrow(Canvas canvas,float direction, int color) {
		//Settings the paint
		paint.setColor(color);
		paint.setStyle(Style.FILL);
		//Computing the points
		float topX, topY, base1X, base1Y, base2X, base2Y;
		float cxCompass = getMeasuredWidth()/2;
		float cyCompass = getMeasuredHeight()/2;
		float radiusCompass;
		
		if(cxCompass > cyCompass){
		 radiusCompass = (float) (cyCompass * 0.9);
		}
		else{
		 radiusCompass = (float) (cxCompass * 0.9);
		}
		
		topX = (float)(cxCompass + radiusCompass * Math.sin((double)(-direction) * Math.PI/180));
		topY = (float)(cyCompass - radiusCompass * Math.cos((double)(-direction) * Math.PI/180));
		
		float baseW = radiusCompass / 7.5f;
		base1X = (float)(cxCompass + baseW * Math.sin((double)(-direction-90) * Math.PI/180));
		base1Y = (float)(cyCompass - baseW * Math.cos((double)(-direction-90) * Math.PI/180));
		
		base2X = (float)(cxCompass + baseW * Math.sin((double)(-direction+90) * Math.PI/180));
		base2Y = (float)(cyCompass - baseW * Math.cos((double)(-direction+90) * Math.PI/180));
		
		
		// Making the path
		Path path = new Path();
		path.reset();
		path.moveTo(topX, topY);
		path.lineTo(base1X, base1Y);
		path.lineTo(base2X, base2Y);
		path.lineTo(topX, topY);
		//Drawing the path
		canvas.drawPath(path, paint);
		//Resetting the paint
		paint.setStyle(Style.STROKE);
	}

}
