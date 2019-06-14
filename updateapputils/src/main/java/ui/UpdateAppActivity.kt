package ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.TextView
import model.DownLoadBy
import teprinciple.updateapputils.R
import update.DownloadAppUtils
import update.UpdateAppService
import update.UpdateAppUtils

internal class UpdateAppActivity : AppCompatActivity() {

    private var content: TextView? = null
    private var sureBtn: TextView? = null
    private var cancleBtn: TextView? = null

    private val updateConfig by lazy { UpdateAppUtils.updateConfig }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO 可以从这里设置不同的UI
        setContentView(R.layout.view_version_tips_dialog)

        initView()
        initOperation()
    }

    private fun initView() {
        sureBtn = findViewById(R.id.dialog_confirm_sure) as TextView
        cancleBtn = findViewById(R.id.dialog_confirm_cancle) as TextView
        content = findViewById(R.id.dialog_confirm_title) as TextView


        var contentStr = "发现新版本:" + updateConfig.serverVersionName + "\n是否下载更新?"
        if (!TextUtils.isEmpty(updateConfig.updateInfo)) {
            contentStr = "发现新版本:" + updateConfig.serverVersionName + "是否下载更新?\n\n" + updateConfig.updateInfo
        }

        content!!.text = contentStr

        if (updateConfig.force!!) {
            cancleBtn!!.text = "退出"
        } else {
            cancleBtn!!.text = "取消"
        }
    }


    private fun initOperation() {

        cancleBtn!!.setOnClickListener {
            if (updateConfig!!.force!!) {
                System.exit(0)
            } else {
                finish()
            }
        }

        sureBtn!!.setOnClickListener { preDownLoad() }
    }

    override fun onBackPressed() {
        // do noting
    }


    /**
     * 预备下载 进行 6.0权限检查
     */
    private fun preDownLoad() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            download()
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                download()

            } else {//申请权限
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_CODE)
            }
        }
    }


    private fun download() {

        startService(Intent(this, UpdateAppService::class.java))

        if (updateConfig?.downloadBy == DownLoadBy.APP) {


            DownloadAppUtils.download(this, updateConfig?.apkUrl
                ?: "", updateConfig?.serverVersionName ?: "")


            // TODO 优化
//            if (isWifiConnected(this)) {
//
//            } else {
//                ConfirmDialog(this, ConfirmDialog.Callback { position ->
//                    if (position == 1) {
//                        DownloadAppUtils.download(this@UpdateAppActivity, updateConfig!!.apkUrl, updateConfig!!.serverVersionName)
//                    } else {
//                        if (updateConfig!!.force!!) {
//                            System.exit(0)
//                        } else {
//                            finish()
//                        }
//                    }
//                }).setContent("目前手机不是WiFi状态\n确认是否继续下载更新？").show()
//            }
        } else {
            DownloadAppUtils.downloadForWebView(this, updateConfig?.apkUrl ?: "")
        }

        finish()
    }


    /**
     * 权限请求结果
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download()
            } else {
//                ConfirmDialog(this, ConfirmDialog.Callback { position ->
//                    if (position == 1) {
//                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                        intent.data = Uri.parse("package:$packageName") // 根据包名打开对应的设置界面
//                        startActivity(intent)
//                    }
//                }).setContent("暂无读写SD卡权限\n是否前往设置？").show()
            }
        }
    }


    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, UpdateAppActivity::class.java)
            context.startActivity(intent)
        }

        private val PERMISSION_CODE = 1001
    }
}
