package com.coolingye.playerlive

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.coolingye.playerlive.databinding.ActivityPullBinding
import kotlinx.coroutines.launch
import org.webrtc.EglBase

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