package com.ziwenl.floatingwindowdemo.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.ziwenl.floatingwindowdemo.R;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Author : Ziwen Lan
 * Date : 2020/5/7
 * Time : 17:07
 * Introduction : 仿微信语音通话悬浮弹窗
 * 内部管理实现悬浮窗功能
 * 实现拖动时判断左右方向自动粘边效果
 * 实现粘边处圆角转直角、非粘边时直角转圆角效果
 */
public class VoiceFloatingView extends View {

    private final String TAG = VoiceFloatingView.class.getSimpleName();
    /**
     * 默认宽高与当前View实际宽高
     */
    private int mDefaultWidth, mDefaultHeight;
    private int mWidth, mHeight;
    /**
     * 当前View绘制相关
     */
    private Paint mPaint;
    private Bitmap mBitmap;
    private PorterDuffXfermode mPorterDuffXfermode;
    private Direction mDirection = Direction.right;
    private int mOrientation;
    private int mWidthPixels;
    /**
     * 悬浮窗管理相关
     */
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private boolean mIsShow;


    public VoiceFloatingView(Context context) {
        super(context);
        init();
    }

    private void init() {
        //悬浮窗管理相关
        mWindowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;

        //当前View绘制相关
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP);
        mBitmap = ((BitmapDrawable) getResources().getDrawable(R.mipmap.b8e)).getBitmap();
        mDefaultHeight = 210;
        mDefaultWidth = 210;

        //记录当前屏幕方向和屏幕宽度
        recordScreenWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = measureSize(mDefaultWidth, heightMeasureSpec);
        mHeight = measureSize(mDefaultHeight, widthMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //间隔和圆角
        int d = 20;
        int r = 30;
        //画透明色圆角背景
        mPaint.setColor(Color.parseColor("#D9E1E1E1"));
        canvas.drawRoundRect(0, 0, mWidth, mHeight, r, r, mPaint);
        //根据最后停留方向（left or right）绘制多一层直角矩形，覆盖圆角
        switch (mDirection) {
            default:
            case right:
                mPaint.setXfermode(mPorterDuffXfermode);
                canvas.drawRoundRect(mWidth / 2, 0, mWidth, mHeight, 0, 0, mPaint);
                break;
            case left:
                mPaint.setXfermode(mPorterDuffXfermode);
                canvas.drawRoundRect(0, 0, mWidth / 2, mHeight, 0, 0, mPaint);
                break;
            case move:
                break;
        }
        mPaint.setXfermode(null);
        //画实色圆角矩形
        mPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(d, d, mWidth - d, mHeight - d, r, r, mPaint);
        //居中填充icon
        canvas.drawBitmap(mBitmap, (mWidth - mBitmap.getWidth()) / 2, (mHeight - mBitmap.getHeight()) / 2, mPaint);
    }

    private int x;
    private int y;

    /**
     * 处理触摸事件，实现拖动、形状变更和粘边效果
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mWindowManager != null) {
            if (getResources().getConfiguration().orientation != mOrientation) {
                //屏幕方向翻转了，重新获取并记录屏幕宽度
                recordScreenWidth();
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    mLayoutParams.x = mLayoutParams.x + movedX;
                    mLayoutParams.y = mLayoutParams.y + movedY;
                    if (mLayoutParams.x < 0) {
                        mLayoutParams.x = 0;
                    }
                    if (mLayoutParams.y < 0) {
                        mLayoutParams.y = 0;
                    }
                    if (mDirection != Direction.move) {
                        mDirection = Direction.move;
                        invalidate();
                    }
                    mWindowManager.updateViewLayout(this, mLayoutParams);
                    break;
                case MotionEvent.ACTION_UP:
                    handleDirection((int) event.getRawX(), (int) event.getRawY());
                    invalidate();
                    mWindowManager.updateViewLayout(this, mLayoutParams);
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 计算宽高
     */
    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        //UNSPECIFIED	父容器没有对当前View有任何限制，当前View可以任意取尺寸
        //EXACTLY	当前的尺寸就是当前View应该取的尺寸
        //AT_MOST	当前尺寸是当前View能取的最大尺寸
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    /**
     * 记录当前屏幕方向和屏幕宽度
     */
    private void recordScreenWidth() {
        mOrientation = getResources().getConfiguration().orientation;
        DisplayMetrics outMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(outMetrics);
        mWidthPixels = outMetrics.widthPixels;
    }

    /**
     * 判定所处方向
     */
    private void handleDirection(int x, int y) {
        if (x > (mWidthPixels / 2)) {
            mDirection = Direction.right;
            mLayoutParams.x = mWidthPixels - getMeasuredWidth();
        } else {
            mDirection = Direction.left;
            mLayoutParams.x = 0;
        }
    }

    /**
     * show
     */
    public void show() {
        if (!mIsShow) {
            if (mLayoutParams.x == 0 && mLayoutParams.y == 0 && mDirection == Direction.right) {
                mLayoutParams.x = mWidthPixels - mDefaultWidth;
                mLayoutParams.y = 0;
            }
            if (mDirection == Direction.move) {
                handleDirection(mLayoutParams.x, mLayoutParams.y);
            }
            mWindowManager.addView(this, mLayoutParams);
            mIsShow = true;
        }
    }

    /**
     * 调整悬浮窗位置
     * 根据提供坐标自动判断粘边
     */
    public void updateViewLayout(int x, int y) {
        if (mIsShow) {
            handleDirection(x, y);
            invalidate();
            mLayoutParams.y = y;
            mWindowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    /**
     * dismiss
     */
    public void dismiss() {
        if (mIsShow) {
            mWindowManager.removeView(this);
            mIsShow = false;
        }
    }

    /**
     * 方向
     */
    public enum Direction {
        /**
         * 左、右、移动
         */
        left,
        right,
        move
    }
}
