package com.ziwenl.floatingwindowdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ziwenl.floatingwindowdemo.utils.FloatingWindowHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_show.setOnClickListener {
            startActivity(Intent(this, VoiceActivity::class.java))
        }

        btn_show_view.setOnClickListener {
            if (ExampleFloatingService.isStart) {
                //通知处理点击事件
                LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(ExampleFloatingService.ACTION_CLICK))
            } else {
                if (FloatingWindowHelper.canDrawOverlays(this, true)) {
                    startService(Intent(this, ExampleFloatingService::class.java))
                }
            }
        }

    }
}
