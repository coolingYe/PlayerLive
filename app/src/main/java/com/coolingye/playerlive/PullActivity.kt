package com.coolingye.playerlive

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.coolingye.playerlive.databinding.ActivityPullBinding
import com.coolingye.playerlive.webrtc.WebRTCClient

open class PullActivity : AppCompatActivity() {

    private lateinit var mView: ActivityPullBinding
    private lateinit var webRTCClient: WebRTCClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mView = ActivityPullBinding.inflate(layoutInflater)
        setContentView(mView.root)
        webRTCClient =  WebRTCClient(this, mView.svMe, mView.svAnother)
        webRTCClient.execute()
    }

    override fun onDestroy() {
        super.onDestroy()
        webRTCClient.destroy()
    }
}