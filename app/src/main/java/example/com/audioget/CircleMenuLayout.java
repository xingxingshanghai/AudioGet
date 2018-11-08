package example.com.audioget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CircleMenuLayout extends ViewGroup
{

	/**
	 * layout的半径
	 */
	private int mRadius;

	private float mMaxChildDimesionRadio = 1 / 4f;
	private float mCenterItemDimesionRadio = 1 / 3f;

	private LayoutInflater mInflater;

	private double mStartAngle = 0;

	//private String[] mItemTexts = new String[] { "肠音录制", "肺音录制", "心音录制"};
	private String[] mItemTexts = new String[] {  "肺音录制", "心音录制"};
//	private int[] mItemImgs = new int[] { R.drawable.home_mbank_1_normal,
//			R.drawable.home_mbank_2_normal, R.drawable.home_mbank_3_normal,
//			R.drawable.home_mbank_4_normal, R.drawable.home_mbank_5_normal,
//			R.drawable.home_mbank_6_normal };
//    private int[] mItemImgs = new int[] { R.drawable.home_mbank_2_normal,
//		R.drawable.home_mbank_1_normal, R.drawable.home_mbank_3_normal};

	private int[] mItemImgs = new int[] {
			R.drawable.home_mbank_1_normal, R.drawable.home_mbank_3_normal};

	private int mTouchSlop;

	/**
	 * 加速度检测
	 */

	private float mDownAngle;
	private float mTmpAngle;
	private long mDownTime;
	private boolean isFling;

	public CircleMenuLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		//动态加载一个布局文件...将我们的布局文件封装成一个View对象...
		mInflater = LayoutInflater.from(context);
		for (int i = 0; i < mItemImgs.length; i++)
		{
			final int j = i;
			View view = mInflater.inflate(R.layout.turnpalte_inner_view, this, false);//动态加载布局文件...并封装...
			ImageView iv = (ImageView) view.findViewById(R.id.id_circle_menu_item_image);
			TextView tv = (TextView) view.findViewById(R.id.id_circle_menu_item_text);
			iv.setImageResource(mItemImgs[i]);
			tv.setText(mItemTexts[i]);
			//设置监听...
			view.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					if(mItemTexts[j].equals("肺音录制")){
						Intent mintent = new Intent(getContext().getApplicationContext(),MainActivity.class);
					    getContext().startActivity(mintent);
					}
					else if(mItemTexts[j].equals("心音录制")){
						//Intent mintent = new Intent(getContext().getApplicationContext(),MainActivity_h.class);
						//getContext().startActivity(mintent);
					}else
					Toast.makeText(getContext(), mItemTexts[j], Toast.LENGTH_SHORT).show();
				}
			});
			addView(view);//放入到一个View集合当中...
		}
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}
    //测量过程...
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
	    /*
	     * 调用这个方法的目的是为View设置大小...
	     * 直接设置成系统根据父容器算出的一个推荐的最小值...
	     * */
		setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
		// 获得半径
		mRadius = Math.max(getWidth(), getHeight());
		//获取menu 的数量...
		final int count = getChildCount(); 
		// Log.e("TAG", count + "");
		//子View的大小...
		int childSize = (int) (mRadius * mMaxChildDimesionRadio);
		//设置测量模式设置为精确...
		int childMode = MeasureSpec.EXACTLY;
		for (int i = 0; i < count; i++)
		{
			//对所有的子View进行迭代测量...
			final View child = getChildAt(i);
			//子控件不可显示，直接跳过..
			if (child.getVisibility() == GONE)
			{
				continue;
			}
			int makeMeasureSpec = -1;
			//子控件为中心图标的时候，设置其半径大小为1/3父容器半径的大小...
			//如果子控件是其他，也就表示为周围空间的时候，设置为父容器半径的1/4大小...
			if (child.getId() == R.id.id_circle_menu_item_center){
				//此步骤是对数据和模式的一个封装...最后measure()方法会根据封装的模式，对我们设置的参数进行设置...
				makeMeasureSpec = MeasureSpec.makeMeasureSpec((int) (mRadius * mCenterItemDimesionRadio), childMode);
			}else{
				makeMeasureSpec = MeasureSpec.makeMeasureSpec(childSize, childMode);
			}
			//由于是圆形menu，因此传递的值是相同的...
			child.measure(makeMeasureSpec, makeMeasureSpec);
		}

	}

	//完成了测量之后，正式根据参数进行布局...
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		int layoutWidth = r - l;
		int layoutHeight = b - t;
		//对父容器进行布局...
		int layoutRadius = Math.max(layoutWidth, layoutHeight);
		// Laying out the child views
		final int childCount = getChildCount();
		int left, top;
		int radius = (int) (layoutRadius * mMaxChildDimesionRadio);
		//根据子元素的个数，设置角度...
		float angleDelay = 360 / (getChildCount() - 1);
		for (int i = 0; i < childCount; i++)
		{
			final View child = getChildAt(i);
			if (child.getId() == R.id.id_circle_menu_item_center)
				continue;
			if (child.getVisibility() == GONE){
				continue;
			   }
			//取角度值..
			mStartAngle %= 360;
			float tmp = layoutRadius * 1f / 3 - 1 / 22f * layoutRadius;
			left = layoutRadius / 2 + (int) Math.round(tmp * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f * radius);
			top = layoutRadius / 2 + (int) Math.round(tmp * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f * radius);
			//由于前面还有1/8长度用来放置那个文本框...因此需要求出整体父容器的定位点...
			child.layout(left, top, left + radius, top + radius);
			mStartAngle += angleDelay;
		}

		View cView = findViewById(R.id.id_circle_menu_item_center);
		cView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Toast.makeText(getContext(), "上海建世生物科技有限公司研发", Toast.LENGTH_SHORT).show();
			}
		});
		// Log.e("TAG",
		// cView.getMeasuredWidth() + " , " + cView.getMeasuredWidth());
		System.out.println(cView.getMeasuredWidth() + " " + cView.getMeasuredWidth());
		//设置中心...
		int cl = layoutRadius / 2 - cView.getMeasuredWidth() / 2;
		int cr = cl + cView.getMeasuredWidth();
		cView.layout(cl, cl, cr, cr);

	}

	private float mLastX;
	private float mLastY;

	private FlingRunnable mFlingRunnable;
	// @Override
	// public boolean onTouchEvent(MotionEvent event)
	// {
	// }
	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		//随手指滑动特效...
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			mLastX = x;
			mLastY = y;
			mDownAngle = getAngle(x, y);//获取角度...
			mDownTime = System.currentTimeMillis();  //系统的当前时间...
			mTmpAngle = 0;
			//如果当前在进行快速滚动，那么移除对快速移动的回调...
			if (isFling)
			{
				removeCallbacks(mFlingRunnable);
				isFling = false;
				return true ; 
			}
			break;
		case MotionEvent.ACTION_MOVE:
			//获取开始和结束后的角度...
			float start = getAngle(mLastX, mLastY);
			float end = getAngle(x, y);
			// Log.e("TAG", "start = " + start + " , end =" + end);
			//判断x,y的值是否在1，4象限...
			if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4){
			 //在一四象限角度为正...
				mStartAngle += end - start;
				mTmpAngle += end - start;
			} else
			{
				mStartAngle += start - end;
				mTmpAngle += start - end;
			}
			// rotateButtons((float) (mStartAngle - currentAngle));
			//重新对布局进行设置...
			requestLayout();
			//将初始值设置为旋转后的值...
			mLastX = x;
			mLastY = y;
			break;
		case MotionEvent.ACTION_UP:
			//计算每秒钟移动的角度...
			float anglePrMillionSecond = mTmpAngle * 1000 / (System.currentTimeMillis() - mDownTime);
			//如果数值大于这个指定的数值，那么就会认为是加速滚动...
			if (Math.abs(anglePrMillionSecond) > 230 && !isFling)
			{
				//开启一个新的线程，让其进行自由滚动...
				post(mFlingRunnable = new FlingRunnable(anglePrMillionSecond));
			}
			if(Math.abs(anglePrMillionSecond) >230 || isFling)
			{
				return true ; 
			}
			
			break;
		}
		return super.dispatchTouchEvent(event);
	}

	private float getAngle(float xTouch, float yTouch)
	{
		double x = xTouch - (mRadius / 2d);
		double y = yTouch - (mRadius / 2d);
		return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
	}

	private int getQuadrant(float x, float y)
	{
		int tmpX = (int) (x - mRadius / 2);
		int tmpY = (int) (y - mRadius / 2);
		if (tmpX >= 0)
		{ return tmpY >= 0 ? 4 : 1; }
		else
		{ return tmpY >= 0 ? 3 : 2; }
	}

	private class FlingRunnable implements Runnable
	{

		private float velocity;

		public FlingRunnable(float velocity)
		{
			this.velocity = velocity;
		}

		public void run()
		{
			if ((int) Math.abs(velocity) < 20)
			{
				isFling = false;
				return;
			}
			isFling = true;
			// rotateButtons(velocity / 75);
			mStartAngle += (velocity / 30);
			velocity /= 1.0666F;
			postDelayed(this, 30);
			requestLayout();
			Log.e("TAG", velocity + "");
		}
	}
}
