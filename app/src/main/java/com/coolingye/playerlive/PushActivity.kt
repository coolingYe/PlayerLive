package com.coolingye.playerlive

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.coolingye.playerlive.databinding.ActivityPullBinding
import com.coolingye.playerlive.webrtc.WebRTCClient

class PushActivity :AppCompatActivity() {

    private lateinit var mView: ActivityPullBinding
    private lateinit var webRTCClient: WebRTCClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mView = ActivityPullBinding.inflate(layoutInflater)
        setContentView(mView.root)
        webRTCClient =  WebRTCClient(this, mView.svMe, mView.svAnother, true)
        webRTCClient.execute()
    }

    override fun onDestroy() {
        super.onDestroy()
        mView.svMe.release()
        mView.svAnother.release()
        webRTCClient.destroy()
    }
}