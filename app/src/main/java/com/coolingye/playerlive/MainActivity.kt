package com.coolingye.playerlive

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.coolingye.playerlive.databinding.ActivityMainBinding
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var mView: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mView = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mView.root)

        checkPermissions()

        mView.btnPush.setOnClickListener {
            startActivity(Intent(this, PushActivity::class.java))
        }
        mView.btnPull.setOnClickListener {
            startActivity(Intent(this, PullActivity::class.java))
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!EasyPermissions.hasPermissions(this, *permissions)) {
            // Permission is not granted
            Log.d("checkCameraPermissions", "No Camera Permissions")
            EasyPermissions.requestPermissions(this, "Please provide permissions", 1, *permissions)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG,"Permission request successful")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG,"Permission request failed")
    }

}
