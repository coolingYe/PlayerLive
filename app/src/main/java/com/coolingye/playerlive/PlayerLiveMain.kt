package com.coolingye.playerlive

import android.app.Application
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import org.webrtc.PeerConnectionFactory

class PlayerLiveMain : Application() {
    override fun onCreate() {
        super.onCreate()
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(applicationContext).createInitializationOptions()
        )

        XLog.init(LogLevel.ALL)
    }
}