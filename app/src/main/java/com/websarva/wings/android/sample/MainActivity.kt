package com.websarva.wings.android.sample

//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.util.Log
//import android.view.MotionEvent
//import android.view.View
//import android.widget.Button;
//import android.widget.ImageButton
//import androidx.annotation.UiThread
//import androidx.annotation.WorkerThread
//import androidx.lifecycle.lifecycleScope
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.fasterxml.jackson.module.kotlin.readValue
//import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.BufferedReader
//import java.io.File
//import java.io.InputStream
//import java.io.InputStreamReader
//import java.lang.Math.abs
//import java.lang.StringBuilder
//import java.net.HttpURLConnection
//import java.net.SocketTimeoutException
//import java.net.URL

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.icu.util.DateInterval
import android.os.Bundle;
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity;
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.lifecycleScope
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.lang.Math.abs
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {

    val col = 4 //縦
    val row = 4 //横

    var top: Int? = null
    var bottom: Int? = null
    var right: Int? = null
    var left: Int? = null

    var first_time: Boolean = true //まだ一度も始めたことがないとき

//    val i_init: Int? = 3
//    val j_init: Int? = 3

    val i_init: Int? = -1
    val j_init: Int? = -1

    val del_node: List<Int> = listOf(5) //indexは0スタート



//    var i_past: Int? = i_init //初期値はスタートの場所
//    var j_past: Int? = j_init//スタートの場所

    var i_past: Int? = i_init //初期値はスタートの場所
    var j_past: Int? = j_init//スタートの場所

    var flag: Boolean = false //ACTION_DOWNした時のボタンが有効だったらtrue


    var mButton = Array(col, {arrayOfNulls<ImageButton>(row)}) //ボタンのインスタンスを作成
    var mApper = Array(col, {arrayOfNulls<Int>(row)}) //押されたかどうか

    //サーバーのURLなどの記述
    companion object{
        private const val DEBUG_TAG = "Sample"
//        private const val QUBO_URL = "http://localhost:8090/" //公開するときは変更する
        private const val QUBO_URL = "http://localhost:8090/" //公開するときは変更する
    }

    data class ansData(var status: String, var result: List<Int>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getButton()
        resetBt.setOnClickListener{
            for(i in 0..(col - 1)){
                for(j in 0..(row - 1)){
                    mApper[i][j] = 0
                    mButton[i][j]?.setBackgroundResource(R.drawable.button_game)
                    //ボタンを押せるようにする
                    mButton[i][j]!!.setEnabled(true)
                    //再度初期化
                    i_past = i_init
                    j_past = j_init
                }
            }
//            mButton[i_init!!.toInt()][j_init!!.toInt()]?.setBackgroundResource(R.drawable.button_top)
            //変更点
//            mButton[0][0]?.setBackgroundResource(R.drawable.button_game_none)
//            mButton[0][1]?.setBackgroundResource(R.drawable.button_game_none)
//            mApper[0][0] = 1
//            mApper[0][1] = 1
//            mApper[3][3] = 1

            init_mApper()
        }

        answer_bt.setOnClickListener{

            //ボタンを全てもとに戻す
            for(i in 0..(col - 1)){
                for(j in 0..(row - 1)){
                    mButton[i][j]!!.setBackgroundResource(R.drawable.button_game)
                }
            }
            //全てのデータを初期化
            init_mApper()

            receiveQuboInfo(QUBO_URL)
        }
    }

    //初期値の設定
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        top = mButton[0][0]!!.getTop()
        bottom = mButton[col - 1][row - 1]!!.getBottom()
        right = mButton[0][0]!!.getRight()
        left = mButton[0][0]!!.getLeft()

        Log.d("top", "${top}")
        Log.d("bottom", "${bottom}")
        Log.d("left", "${left}")
        Log.d("right", "${right}")

        init_mApper()

        for(i in 0..(col - 1)){
            for(j in 0..(row - 1)){
                mButton[i][j]?.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
                    when (motionEvent.action){
                        MotionEvent.ACTION_DOWN -> {
                            if(i_past == i && j_past == j) {
//                                mButton[i][j]?.setBackgroundResource(R.drawable.button_top)
//                                i_past = i
//                                j_past = j
                                flag = true
                            }
                            if(first_time){
                                mButton[i][j]?.setBackgroundResource(R.drawable.button_top)
                                mApper[i][j] = 1
                                i_past = i
                                j_past = j
                                first_time = false
                                flag = true
                            }

                            if(complete()){
                                val intent_max = Intent(this@MainActivity, ScoreScreen::class.java)
                                startActivity(intent_max)
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            var x = motionEvent.getX()
                            var y = motionEvent.getY()

                            Log.d("x", "${x}")
                            Log.d("y", "${y}")

                            var x_range = bottom as Int
                            var y_range = right as Int

                            var dx: Int = (x / (x_range as Int)).toInt()
                            var dy: Int = (y / (y_range as Int)).toInt()
//
                            if(x < 0) dx -= 1
                            if(y < 0) dy -= 1

                            val i_p: Int = i_past!!.toInt()
                            val j_p: Int = j_past!!.toInt()

                            if(0 <= i + dy && i + dy < col && 0 <= j + dx && j + dx < row){
                                if(check(i + dy, j + dx, i_past!!.toInt(), j_past!!.toInt())) {
//                                    mButton[i + dy][j + dx]?.setBackgroundResource(R.drawable.button_top)
//                                    mButton[i_past!!.toInt()][j_past!!.toInt()]?.setBackgroundResource(R.drawable.button_click)
                                    if(mApper[i + dy][j + dx] == 0){
                                        mButton[i + dy][j + dx]?.setBackgroundResource(R.drawable.button_top)
                                        mButton[i_past!!.toInt()][j_past!!.toInt()]?.setBackgroundResource(R.drawable.button_click)
                                        i_past = i + dy
                                        j_past = j + dx
                                        mApper[i + dy][j + dx] = 1
                                    }
                                }
                            }

                            if(complete()){
                                val intent_max = Intent(this@MainActivity, ScoreScreen::class.java)
                                startActivity(intent_max)
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            flag = false //初期化する
                            if(complete()){
                                val intent_max = Intent(this@MainActivity, ScoreScreen::class.java)
                                startActivity(intent_max)
                            }
                        }
                    }
                    return@OnTouchListener true
                })
            }
        }

    }

    fun complete(): Boolean{
        var ans_bool: Boolean = true

        for(i in 0..(row - 1)){
            for(j in 0..(col - 1)){
                if(mApper[i][j] == 0) ans_bool = false
            }
        }

        return ans_bool
    }

    fun getButton(){
        mButton[0][0] = findViewById(R.id.Button_11)
        mButton[0][1] = findViewById(R.id.Button_12)
        mButton[0][2] = findViewById(R.id.Button_13)
        mButton[0][3] = findViewById(R.id.Button_14)

        mButton[1][0] = findViewById(R.id.Button_21)
        mButton[1][1] = findViewById(R.id.Button_22)
        mButton[1][2] = findViewById(R.id.Button_23)
        mButton[1][3] = findViewById(R.id.Button_24)

        mButton[2][0] = findViewById(R.id.Button_31)
        mButton[2][1] = findViewById(R.id.Button_32)
        mButton[2][2] = findViewById(R.id.Button_33)
        mButton[2][3] = findViewById(R.id.Button_34)

        mButton[3][0] = findViewById(R.id.Button_41)
        mButton[3][1] = findViewById(R.id.Button_42)
        mButton[3][2] = findViewById(R.id.Button_43)
        mButton[3][3] = findViewById(R.id.Button_44)

//        mButton[i_past!!.toInt()][j_past!!.toInt()]!!.setBackgroundResource(R.drawable.button_top)

    }

    fun init_mApper(){
        for(i in 0..(col - 1)){
            for(j in 0..(row - 1)){
                mApper[i][j] = 0
            }
        }
        first_time = true //どこからでもタッチできるように
//        mApper[i_past!!.toInt()][j_past!!.toInt()] = 1
        //初期値の変更はこちらから
        //変更点
        mButton[1][1]!!.setBackgroundResource(R.drawable.button_game_none)
        mApper[1][1] = 1
    }

    fun check(i: Int, j: Int, i_past: Int, j_past: Int): Boolean{
        if(!flag) return false
        if(abs(i - i_past) == 1 && j == j_past) return true
        else if(abs(j - j_past) == 1 && i == i_past) return true
        else return false
    }

    //#################################
    //サーバーとの通信

    //サーバーとの非同期処理
    //kotlinコルーチンで記述
    @UiThread
    private fun receiveQuboInfo(urlFull: String){
        //ここにコルーチンに関するコードを記述
        //コルーチンを定義して起動させる
        lifecycleScope.launch{
            val result = QuboInfoBackgroundRunner(urlFull)
            QuboInfoPostRunner(result)
        }
    }

    //当該メソッドの実行中にほかの処理を中断させるときはsuspendを記述する
    @WorkerThread
    private suspend fun QuboInfoBackgroundRunner(url: String): String{

        //スレッドの分離
        //Dispatchersクラスの定数で記述する
        //.Mainのときメインスレッド
        //.IOのときワーカースレッド

        Log.d("test", "DO_GET")
        val returnVal = withContext(Dispatchers.IO){
            var result = ""
            val url = URL(url)
            val con = url.openConnection() as? HttpURLConnection

            con?.let{
                try{
                    it.connectTimeout = 150000
                    it.readTimeout = 150000
                    it.requestMethod = "GET"
                    it.connect()
                    val stream = it.inputStream
                    result = is2String(stream)
                    stream.close()
                }catch (ex: SocketTimeoutException){
                    Log.w(DEBUG_TAG, "通信タイムアウト", ex)
                }
            }
            result
        }
        return returnVal
    }

    @UiThread
    private fun QuboInfoPostRunner(result: String){
        //JSONファイルを解析する
        Log.d("test", "JSONファイルを解析中")
        val mapper = jacksonObjectMapper()
        val responseData = mapper.readValue<ansData>(result)
        Log.d("sample", "${responseData}")
        printAns(responseData.result)
    }

    private fun is2String(stream: InputStream): String{
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var line = reader.readLine()
        while(line != null){
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }

    //#########################################

    //答えを表示させる関数
    fun printAns(ans: List<Int>){

        //ボタンを押せなくする
        //もとに戻すところはrestartのonClickメソッドで行っている
        for(i in 0..(col - 1)){
            for(j in 0..(row - 1)){
                mButton[i][j]!!.setEnabled(false)
            }
        }

        val loop = ans.size
        //いもす法みたいなノリで
        var del_list: IntArray = IntArray(col * row)
        for(i in del_node){
            del_list[i] = 1
        }

        Log.d("Sample", "${ans}")

        for(i in 1..(col * row - 1)){
            del_list[i] += del_list[i - 1]
        }

        for(i in 0..(col * row - 1)){
            Log.d("Sample", "${del_list[i]}")
        }

        var ans_pross: IntArray = IntArray(col * row) //加工後のデータ

        for(i in 0..(loop - 1)){
            ans_pross[i] = ans[i] + del_list[ans[i]]
        }

        for(i in 0..(loop - 1)){
            Log.d("ans", "${ans_pross[i]}")
        }

        Log.d("timer", "${loop}")

        val Time: Long = (loop + 1) * 100L
        val Intarval: Long = 100

        var increment = 0 //resultのindexを指定する変数

        val timer = object :CountDownTimer(Time, Intarval){ //開始時間・インターバル
            //途中経過・残り時間
            //p0：残り時間
            override fun onTick(p0: Long) {
                val k: Int = ans_pross[increment]
                val i: Int = (k / col).toInt()
                val j: Int = k % col
                mButton[i][j]!!.setBackgroundResource(R.drawable.button_click)
                increment++
                Log.d("text", "${increment}")
//                Log.d("text", "${i}" + "${ j}")
            }

            //タイマーが終了したとき
            override fun onFinish() {
                return
            }
        }

        timer.start()

    }

    fun back_bt(view: View){
        finish()
    }
}