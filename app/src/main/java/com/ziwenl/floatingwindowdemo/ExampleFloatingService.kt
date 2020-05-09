package com.ziwenl.floatingwindowdemo

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ziwenl.floatingwindowdemo.utils.FloatingWindowHelper

/**
 * PackageName : com.ziwenl.floatingwindowdemo
 * Author : Ziwen Lan
 * Date : 2020/5/9
 * Time : 15:31
 * Introduction : 悬浮窗管理服务 -- 一般在此处理业务逻辑
 * 目的脱离 Activity 展示悬浮窗
 */
class ExampleFloatingService : Service() {

    companion object {
        const val ACTION_CLICK = "action_click"
        var isStart = false
    }

    private lateinit var mFloatingWindowHelper: FloatingWindowHelper
    private lateinit var mExampleViewA: View
    private lateinit var mExampleViewB: View
    /**
     * 监听本地广播,点击事件
     */
    private var mLocalBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ACTION_CLICK.equals(intent?.action)) {
                onClick()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        isStart = true
        //注册监听本地广播
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(mLocalBroadcastReceiver, IntentFilter(ACTION_CLICK))

        mFloatingWindowHelper = FloatingWindowHelper(this)
        val layoutInflater = LayoutInflater.from(this)
        mExampleViewA = layoutInflater.inflate(R.layout.widget_test_view, null, false)
        mExampleViewB = layoutInflater.inflate(R.layout.widget_test_view_b, null, false)

        onClick()
    }

    private fun onClick() {
        if (!mFloatingWindowHelper.contains(mExampleViewA)) {
            mFloatingWindowHelper.addView(mExampleViewA)
        } else if (!mFloatingWindowHelper.contains(mExampleViewB)) {
            mFloatingWindowHelper.addView(mExampleViewB, 100, 100, true)
        } else {
            mFloatingWindowHelper.clear()
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(mLocalBroadcastReceiver)
        mFloatingWindowHelper.destroy()
        isStart = false
        super.onDestroy()
    }
}