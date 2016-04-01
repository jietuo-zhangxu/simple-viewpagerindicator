package com.zx.view;

import java.util.List;

import com.zx.viewpagerIndicator.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * 
 * @author jietuo
 * 
 * @since 2016.3.22
 * 
 * @version 1.0
 * 
 * @see jietuo_zx@163.com
 * 
 * 1、ViewPagerIndicator 原理掌握
 * 2、组合方式去实现自定义控件
 * 3、在自定义控件中选择合适的方法进行合适的操作
 * 4、Canvas.translate  Path
 * 5、自定义控件中使用了某个接口，那么要对外提供一个额外的接口供用户去使用。
 *
 */
public class ViewPagerIndicator extends LinearLayout {

	private Paint mPaint;

	private Path mPath;
	/**
	 * 三角形的宽
	 */
	private int mTriangleWidth;
	/**
	 * 三角形的高
	 */
	private int mTriangleHeight;

	/**
	 * 三角形底边占一个Tab的位置
	 */
	private static final float RADIO_TRIANGLE_WIDTH = 1/6F;
	
	/**
	 * 三角形地边的最大宽度
	 */
	private final int DIMENSION_TRANGLE_WIDTH_MAX = (int) (getScreenWidth()/3 * RADIO_TRIANGLE_WIDTH);
	/**
	 * 初始化的偏移量
	 */
	private int mInitTranslationX;
	/**
	 * 偏移量
	 */
	private int mTranslationX;

	/**
	 * 设置可见Tab的数量
	 */
	private int mTabVisibleCount;

	/**
	 * 默认可见Tab的数量
	 */
	private static final int COUNT_DEFAULT_TAB = 4;

	private List<String> mTitles;

	private static final int COLOR_TEXT_NORMAL = 0x77FFFFFF;
	private static final int COLOR_TEXT_HIGHLIGHT = 0xFFFFFFFF;

	public ViewPagerIndicator(Context context) {
		this(context, null);
	}

	public ViewPagerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);

		// 获取可见Tab的数量
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ViewPagerIndicator);

		mTabVisibleCount = a.getInt(
				R.styleable.ViewPagerIndicator_visible_tab_count,
				COUNT_DEFAULT_TAB);

		if (mTabVisibleCount < 0) {
			mTabVisibleCount = COUNT_DEFAULT_TAB;
		}
		a.recycle();

		// 初始化画笔
		mPaint = new Paint();
		// 抗锯齿功能
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.parseColor("#ffffffff"));
		mPaint.setStyle(Style.FILL);
		// 两条线连接处圆滑处理
		mPaint.setPathEffect(new CornerPathEffect(3));
	}

	/**
	 * 子view个数的确定
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		int cCount = getChildCount();
		if (cCount == 0)
			return;

		for (int i = 0; i < cCount; i++) {
			View view = getChildAt(i);
			LinearLayout.LayoutParams lp = (LayoutParams) view
					.getLayoutParams();
			lp.weight = 0;
			lp.width = getScreenWidth() / mTabVisibleCount;
			view.setLayoutParams(lp);
		}
		
		setItemClickEvent();
	}

	/**
	 * 获取屏幕的宽度
	 * 
	 * @return
	 */
	private int getScreenWidth() {
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics.widthPixels;
	}

	/**
	 * 绘制三角形
	 */
	@Override
	protected void dispatchDraw(Canvas canvas) {
		canvas.save();

		canvas.translate(mInitTranslationX + mTranslationX, getHeight() + 2);
		canvas.drawPath(mPath, mPaint);

		canvas.restore();
		super.dispatchDraw(canvas);

	}

	/**
	 * 适用于，根据控件的宽高来设置宽高 控件的宽高发生变化时会回调这个方法
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mTriangleWidth = (int) (w / mTabVisibleCount * RADIO_TRIANGLE_WIDTH);
		mTriangleWidth = Math.min(mTriangleWidth, DIMENSION_TRANGLE_WIDTH_MAX);
		mInitTranslationX = w / mTabVisibleCount / 2 - mTriangleWidth / 2;

		initTrangle();
	}

	/**
	 * 画出一个三角形
	 */
	private void initTrangle() {

		mTriangleHeight = mTriangleWidth / 2;

		mPath = new Path();
		mPath.moveTo(0, 0);
		mPath.lineTo(mTriangleWidth, 0);
		mPath.lineTo(mTriangleWidth / 2, -mTriangleHeight);
		mPath.close();
	}

	/**
	 * 指示器跟随手指进行滚动
	 * 
	 * @param position
	 * @param offset
	 */
	public void scoll(int position, float offset) {

		int tabWidth = getWidth() / mTabVisibleCount;
		mTranslationX = (int) (tabWidth * (position + offset));

		// 当前的position
		if (position >= (mTabVisibleCount - 1) && offset > 0
				&& getChildCount() > mTabVisibleCount) {

			if (mTabVisibleCount != 1) {
				this.scrollTo(
						((position - (mTabVisibleCount - 1)) * tabWidth + (int) (tabWidth * offset)),
						0);
			} else {
				this.scrollTo(position * tabWidth + (int) (tabWidth * offset),
						0);
			}
		}
		// 重绘界面
		invalidate();
	}

	public void setTabItemTitles(List<String> titles) {
		if (titles != null && titles.size() > 0) {
			this.removeAllViews();
			mTitles = titles;
			for (String title : titles) {
				addView(generateTextView(title));
			}
			
			setItemClickEvent();
		}
	}

	/**
	 * 设置可见的Tab数量，需要在创建generateTextView()前面调用
	 * 
	 * @param count
	 */
	public void setVisibleTabCount(int count) {
		mTabVisibleCount = count;
	}
 
	/**
	 * 根据title创建Tab
	 * 
	 * @param title
	 * @return
	 */
	private View generateTextView(String title) {
		TextView tv = new TextView(getContext());
		LinearLayout.LayoutParams lp = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.width = getScreenWidth() / mTabVisibleCount;
		tv.setText(title);
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setTextColor(COLOR_TEXT_NORMAL);
		tv.setLayoutParams(lp);
		return tv;

	}

	private ViewPager mViewPager;

	/**
	 * 对外提供一个接口，在出现多重滑动时，调用
	 * @author jietuo
	 *
	 */
	public interface PageOnChangeListener {
		public void onPageSelected(int position);

		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels);

		public void onPageScrollStateChanged(int state);
	}
	
	public PageOnChangeListener mListener;
	
	public void setOnPageChangeListener(PageOnChangeListener listener){
		this.mListener = listener;
	}

	/**
	 * 设置关联的viewpager
	 * 
	 * @param viewpager
	 * @param position
	 */
	public void setViewPager(ViewPager viewpager, int position) {
		mViewPager = viewpager;
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (mListener != null) {
					mListener.onPageSelected(position);
				}
				highLightTextView(position);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// tabWidth * positionOffset + position * tabWidth
				scoll(position, positionOffset);
				
				if (mListener != null) {
					mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (mListener != null) {
					mListener.onPageScrollStateChanged(state);
				}
			}
		});
		mViewPager.setCurrentItem(position);
		highLightTextView(position);
	}
	
	/**
	 * 重置TAB文本的颜色
	 */
	public void resetTextView(){
		for (int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			if (view instanceof TextView) {
				((TextView) view).setTextColor(COLOR_TEXT_NORMAL);
			}
		}
	}
	/**
	 * 高亮某个Tab的文本
	 * @param pos
	 */
	private void highLightTextView(int pos){
		resetTextView();
		View view = getChildAt(pos);
		if (view instanceof TextView) {
			((TextView) view).setTextColor(COLOR_TEXT_HIGHLIGHT);
		}
	}
	
	/**
	 * 设置TAB的点击事件
	 */
	private void setItemClickEvent(){
		int cCount = getChildCount();
		
		for (int i = 0; i < cCount; i++) {
			final int j = i;
			View view = getChildAt(i);
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mViewPager.setCurrentItem(j);
				}
			});
		}
	}
}

