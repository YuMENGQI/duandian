package com.permissionx.duandian

import android.database.Observable
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import cz.msebera.android.httpclient.client.methods.HttpGet
import cz.msebera.android.httpclient.client.methods.HttpHead
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient
import java.io.*
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var pbProgress: ProgressBar
    private lateinit var btDownload: Button
    private lateinit var btPause: Button
    private lateinit var tvInfo: TextView
    private val uri =
        "https://test-file.fooww.com/groupabc/M01/00/6D/rBFpkmAGnPiAFrefAADZ07_aIDE588.jpg"

    companion object {
        private val TAG = "OtherActivity"

        //下载线程的数量
        private val threadSize = 3
        private val SET_MAX = 0
        private val UPDATE_VIEW = 1
        private var flag = false//是否在下载


        /**
         * 更新下载记录
         */
        private fun updateDownloadInfo(threadId: Int, newDownloadLength: Int) {

            //下载记录文件
            val file = File(Environment.getExternalStorageDirectory(), "${threadId}.txt")
            val fw = FileWriter(file)
            fw.write(newDownloadLength)
            fw.close()
        }

        /**
         * 读取指定线程的下载数据量
         */
        fun readDownloadInfo(threadId: Int): Int {
            //下载记录文件
            val file = File(Environment.getExternalStorageDirectory(), "${threadId}.txt")
            val br = BufferedReader(FileReader(file))
            //读取一行数据
            val content = br.readLine()
            var downLength = 0
            //如果该文件第一次创建去执行读取操作 文件里面的内容是null
            if (content.isNotEmpty()) {
                downLength = content.toInt()
            }
            //关闭流
            br.close()
            return downLength
        }
    }

    //显示进度和更新进度
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == SET_MAX) {
                //设置进度条的最大值
                val fileLength = msg.arg1
                pbProgress.max = fileLength
                tvInfo.text = fileLength.toString()
            } else if (msg.what == UPDATE_VIEW) {
                //更新进度条和下载的比率
                val len = msg.arg1
                pbProgress.progress = pbProgress.progress + len
                val max = pbProgress.max
                val progress = pbProgress.progress
                val result = (progress * 100) / max
                tvInfo.text = "下载${result}%"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pbProgress = findViewById(R.id.pbProgress)
        btDownload = findViewById(R.id.btDownload)

        btPause = findViewById(R.id.btPause)
        tvInfo = findViewById(R.id.tvInfo)
        val name = getFileName(uri)
        val file = File(Environment.getExternalStorageDirectory(), name)
        if (file.exists()) {
            //文件存在回显
            val fileLength = file.length()
            pbProgress.max = fileLength.toInt()
            try {
                //统计原来所有的下载量
                var count = 0
                //读取下载记录文件
                for (threadId in 0 until threadSize) {
                    //获取原来指定线程的下载记录
                    val existDownloadLength = readDownloadInfo(threadId)//原来下载的数据量
                    count += existDownloadLength
                    //设置进度条的刻度
                    pbProgress.progress = count

                    //计算比率
                    val result = (count * 100) / fileLength
                    tvInfo.text = "下载：${result}%"
                }
                //计算最新的下载
                //val newDownloadLength=existDownloadLength+le
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        btDownload.setOnClickListener {
            download()
        }
        btPause.setOnClickListener {
            pause()
        }
    }

    private fun pause() {
        flag = false
        btDownload.isEnabled = true
        btPause.isEnabled = false
    }

    //下载
    private fun download() {
        flag = true
        btDownload.isEnabled = false
        btPause.isEnabled = true
        object : Thread() {
            override fun run() {
                try {
                    //获取服务器上文件的大小
                    val client = DefaultHttpClient()
                    val request = HttpHead(uri)

                    val response = client.execute(request)
                    //response只有响应头 没有响应体
                    if (response.statusLine.statusCode == 200) {
                        val headers = response.getHeaders("Content-Length")
                        val value = headers[0].value
                        //文件大小
                        val fileLength = value.toInt()
                        Log.i(TAG, "fileLength${fileLength}")

                        //设置进度条的最大值
                        val msg_setmax = Message.obtain(mHandler, SET_MAX, fileLength, 0)
                        msg_setmax.sendToTarget()

                        //处理下载记录文件
                        for (threadId in 0 until threadSize) {
                            //对应的下载记录文件
                            val file =
                                File(Environment.getExternalStorageDirectory(), "${threadId}.txt")
                            if (!file.exists()) {
                                file.createNewFile()
                            }
                        }

                        //在sdcard创建和服务器大小一样的文件
                        val name = getFileName(uri)
                        val file = File(Environment.getExternalStorageDirectory(), name)
                        //随机访问文件
                        val raf = RandomAccessFile(file, "rwd")
                        //设置文件的大小
                        raf.setLength(fileLength.toLong())
                        //关闭
                        raf.close()

                        //计算每条线程的下载量
                        val block =
                            if (fileLength % threadSize == 0) (fileLength / threadSize) else (fileLength / threadSize + 1)
                        //开启三条线程执行下载
                        for (threadId in 0 until threadSize) {
                            DownloadThread(threadId, uri, file, block).start()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    inner class DownloadThread() : Thread() {

        private var threadId = 0
        private var uri = ""
        private lateinit var file: File
        private var block = 0
        private var start = 0
        private var end = 0

        constructor(threadId: Int, uri: String, file: File, block: Int) : this() {
            this.threadId = threadId
            this.uri = uri
            this.file = file
            this.block = block
            //计算下载的开始位置和结束位置
            start = threadId * block
            end = (threadId + 1) * block - 1
            try {
                //读取该条线程原来的下载记录
                val existDownloadLength = readDownloadInfo(threadId)

                //修改下载的开始位置 重新下载
                start += existDownloadLength

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //下载 200 普通下载 206分段下载
        override fun run() {
            super.run()
            try {
                val raf = RandomAccessFile(file, "rwd")
                //跳转到起始位置
                raf.seek(start.toLong())
                //分段下载
                val client = DefaultHttpClient()
                val request = HttpGet(uri)
                request.addHeader("Range", "bytes:${start}-${end}")//添加请求头
                val response = client.execute(request)

                    val inputStream = response.entity.content
                    //把流写入文件
                    val buffer = ByteArray(1024)
                    var len = inputStream.read(buffer)
                    while (inputStream.read(buffer) != -1) {
                        //如果暂停下载 点击暂停false 就直接 return 点击下载true接着下载
                        if (!flag) {
                            return //标准线程结束
                        }
                        //写数据
                        raf.write(buffer, 0, len)
                        //读取原来下载的数据量 这里读取是为了更新下载记录
                        val existDownloadLength = readDownloadInfo(threadId)

                        //计算最新的下载
                        val newDownloadLength = existDownloadLength + len
                        //更新下载记录
                        updateDownloadInfo(threadId, newDownloadLength)
                        //更新进度条的显示 下载的百分比
                        val update_msg = Message.obtain(mHandler, UPDATE_VIEW, len, 0)
                        update_msg.sendToTarget()
                        //模拟 看到进度条动的效果
                        SystemClock.sleep(50)
                    }
                    inputStream.close()
                    raf.close()
                    Log.i(TAG, "第${threadId}条线程下载完成")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    /**
     * 获取文件名
     */
    private fun getFileName(uri: String): String {
        return uri.substring(uri.lastIndexOf("/") + 1)
    }
}