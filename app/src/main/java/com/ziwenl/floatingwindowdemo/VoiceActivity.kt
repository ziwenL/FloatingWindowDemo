package com.ziwenl.floatingwindowdemo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_voice.*

/**
 * PackageName : com.ziwenl.floatingwindowdemo
 * Author : Ziwen Lan
 * Date : 2020/5/8
 * Time : 9:09
 * Introduction :  仿语音聊天页面，当前页面存在时不显示悬浮窗，不存在时则显示悬浮窗
 */
class VoiceActivity : Activity() {
    private var mIsClose = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice)

        iv_dismiss.setOnClickListener {
            //Android 6.0 以下无需获取权限，可直接展示悬浮窗
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //判断是否拥有悬浮窗权限，无则跳转悬浮窗权限授权页面
                if (Settings.canDrawOverlays(this)) {
                    showFloatingView()
                    finish()
                } else {
                    //跳转悬浮窗权限授权页面
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                    )
                }
            } else {
                showFloatingView()
                finish()
            }
        }

        iv_close.setOnClickListener {
            mIsClose = true
            VoiceFloatingService.stopSelf()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        dismissFloatingView()
    }

    override fun onPause() {
        super.onPause()
        if (!mIsClose) {
            //显示悬浮窗
            showFloatingView()
        }

    }

    //隐藏悬浮窗
    private fun dismissFloatingView(){
        if (VoiceFloatingService.isStart) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(VoiceFloatingService.ACTION_DISMISS_FLOATING))
        }
    }

    /**
     * 显示悬浮窗
     */
    private fun showFloatingView() {
        if (VoiceFloatingService.isStart) {
            //通知显示悬浮窗
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(VoiceFloatingService.ACTION_SHOW_FLOATING))
        } else {
            //启动悬浮窗管理服务
            startService(Intent(this, VoiceFloatingService::class.java))
        }
    }
}