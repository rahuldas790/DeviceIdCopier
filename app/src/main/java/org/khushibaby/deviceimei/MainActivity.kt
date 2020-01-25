package org.khushibaby.deviceimei

import android.Manifest
import android.annotation.TargetApi
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import org.khushibaby.deviceimei.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding


    private var strDeviceId: String? = null
    private var strImeiNo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.btnGetId.setOnClickListener {
            checkPermission()
        }
        binding.btnCopyDeviceId.setOnClickListener {
            copyString(strDeviceId)
        }
        binding.btnCopyImeiNo.setOnClickListener {
            copyString(strImeiNo)
        }
    }

    private fun copyString(copyString: String?) {
        if (copyString == null) {
            Toast.makeText(this, "Nothing to copy!", Toast.LENGTH_SHORT).show()
            return
        }

        val clipService = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val primaryClip = ClipData.newPlainText(copyString, copyString)
        clipService.setPrimaryClip(primaryClip)

        Toast.makeText(this, "Copied to clipboard!!", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                logIds()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    1
                )
            }
        } else {
            logIds()
        }
    }

    private fun logIds() {
        strDeviceId = logDeviceId()
        strImeiNo = getImei()
        binding.tvDeviceId.text = String.format(getString(R.string.device_id_s), strDeviceId)
        binding.tvImeiNo.text = String.format(getString(R.string.imei_no_s), strImeiNo)
        binding.clDetails.visibility = View.VISIBLE
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            val list = ArrayList<String>()
            val listRational = ArrayList<String>()

            if (permissions.isNotEmpty()) {
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        val showRationale = shouldShowRequestPermissionRationale(permissions[i])
                        if (!showRationale) {
                            listRational.add(permissions[i])
                        } else if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            list.add(permissions[i])
                        }
                    }
                }
            }

            if (list.isNotEmpty()) {
                Toast.makeText(this, "Permission Not granted!!", Toast.LENGTH_SHORT).show()
            } else if (!listRational.isEmpty()) { //Show diolog
            } else {
                logIds()
            }
        }
    }

    private fun logDeviceId(): String? {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun getImei(): String? {
        val telephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telephonyManager.imei
        } else {
            telephonyManager.deviceId
        }
    }
}
