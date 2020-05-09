package com.ziwenl.floatingwindowdemo

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ziwenl.floatingwindowdemo.widgets.VoiceFloatingView

/**
 * PackageName : com.ziwenl.floatingwindowdemo
 * Author : Ziwen Lan
 * Date : 2020/5/7
 * Time : 17:48
 * Introduction : 仿微信语音聊天悬浮窗管理服务 -- 一般在此处理业务逻辑
 */
class VoiceFloatingService : Service() {

    companion object {
        private var mServiceVoice: VoiceFloatingService? = null
        const val ACTION_SHOW_FLOATING = "action_show_floating"
        const val ACTION_DISMISS_FLOATING = "action_dismiss_floating"
        var isStart = false

        fun stopSelf() {
            mServiceVoice?.stopSelf()
            mServiceVoice = null
        }
    }

    private var mVoiceFloatingView: VoiceFloatingView? = null
    /**
     * 监听本地广播显示或隐藏悬浮窗
     */
    private var mLocalBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ACTION_SHOW_FLOATING.equals(intent?.action)) {
                mVoiceFloatingView?.show()
            } else if (ACTION_DISMISS_FLOATING.equals(intent?.action)) {
                mVoiceFloatingView?.dismiss()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        mServiceVoice = this
        isStart = true
        //初始化悬浮View
        mVoiceFloatingView = VoiceFloatingView(this)
        //注册监听本地广播
        val intentFilter = IntentFilter(ACTION_SHOW_FLOATING)
        intentFilter.addAction(ACTION_DISMISS_FLOATING)
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //显示悬浮窗
        mVoiceFloatingView?.show()
        mVoiceFloatingView?.setOnLongClickListener {
            val voiceActivityIntent = Intent(this@VoiceFloatingService, VoiceActivity::class.java)
            voiceActivityIntent.flags = FLAG_ACTIVITY_NEW_TASK
            startActivity(voiceActivityIntent)
            mVoiceFloatingView?.dismiss()
            true
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mVoiceFloatingView?.dismiss()
        mVoiceFloatingView = null
        isStart = false
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadcastReceiver)
        super.onDestroy()
    }
}