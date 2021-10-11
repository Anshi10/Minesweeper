package com.example.minesweeper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.start
import kotlinx.android.synthetic.main.coustom_board.*


class MainActivity : AppCompatActivity() {
    //initial values
    var Mines = 0
    var Cols = 0
    var Rows = 0
    var isclicked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //when Easy Button is clicked
        EasyButton.setOnClickListener {
            Mines = 12
            Cols = 8
            Rows = 8
            isclicked= true
        }
        //when Medium Button is clicked
        MediumButton.setOnClickListener {
            Mines = 24
            Cols = 12
            Rows = 12
            isclicked= true
        }
        //when Hard Button is clicked
        HardButton.setOnClickListener {
            Mines = 30
            Cols = 16
            Rows = 16
            isclicked= true
        }
        //when custom Button is Clicked
        coustomButton.setOnClickListener {
            setCoustomBoard()
        }
        //when start button is clicked
        start.setOnClickListener {
            if(isclicked){
            val intent = Intent(this, minesweeperGame::class.java)
            intent.putExtra("Rows", Rows)
            intent.putExtra("Cols", Cols)
            intent.putExtra("Mines", Mines)
            startActivity(intent)
            finish()}
            else{
                Toast.makeText(this,"Choose valid option!",Toast.LENGTH_SHORT).show()
            }
        }
        //question mark is for help
        help.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            with(builder){
                setTitle(getString(R.string.title))
                setMessage(R.string.Rules)
                setPositiveButton(R.string.ok
                ) { dialog, which -> }
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
        //this is to show best time and last game time
        showscore()
   }

    private fun showscore() {
        val pref : SharedPreferences = getSharedPreferences("score",Context.MODE_PRIVATE)
        val bestTime = pref.getInt("BestTime",0)
        val lastgameTime = pref.getInt("LastGameTime",0)
        if(bestTime == 0 || lastgameTime ==0 ){
            BestTime.setText(R.string.BestTime)
            LastGameTime.setText(R.string.LastGameTime)
        }
        else{
            val bestmin = (bestTime/1000)/60
            val bestsec = (bestTime/1000)%60
            val lastmin = (lastgameTime/1000)/60
            val lastsec = (lastgameTime/1000)%60
            BestTime.text = "Best Time : ${bestmin}:${bestsec}"
            LastGameTime.text = "Last Game Time : ${lastmin}:${lastsec}"
        }
    }

    //alter dialog to create custom Board
    @SuppressLint("InflateParams")
    private fun setCoustomBoard() {
        val builder = AlertDialog.Builder(this)
        val inflator = this.layoutInflater
        //inflate  a view using inflator
        val dialogView = inflator.inflate(R.layout.coustom_board, null)
        val row = dialogView.findViewById<EditText>(R.id.RowsCount)
        val col = dialogView.findViewById<EditText>(R.id.ColoumnCount)
        val mine = dialogView.findViewById<EditText>(R.id.MinesCount)
        //Error Listener for each row
        setErrorListener(row)
        setErrorListener(col)
        setErrorListener(mine)

        with(builder) {
            setView(dialogView)
            setTitle("Set Values for Custom Board")
            setPositiveButton(getString(R.string.submit)
            ) { dialog, which -> //storing values of Inputs by user
                if (col.text.toString() == "" || row.text.toString() == "" || mine.text.toString() == "") {
                    Toast.makeText(this@MainActivity, "Invalid Input", Toast.LENGTH_SHORT).show()
                } else {
                    val cols: Int = Integer.parseInt(col.text.toString())
                    val rows: Int = Integer.parseInt(row.text.toString())
                    val mines: Int = Integer.parseInt(mine.text.toString())

                    val intent = Intent(this@MainActivity, minesweeperGame::class.java)
                    intent.putExtra("Rows", rows)
                    intent.putExtra("Cols", cols)
                    intent.putExtra("Mines", mines)
                    startActivity(intent)
                    finish()
                }
            }
            setNegativeButton(getString(R.string.cancel)
            ) { dialog, which -> }
        }
        val alertdialog = builder.create()
        alertdialog.show()
    }

    private fun setErrorListener(editText: EditText) {
            editText.error = if(editText.text.toString().isNotEmpty()) null else "Field Cannot be Empty"
            editText.addTextChangedListener(object : TextWatcher {
             override fun afterTextChanged(s: Editable?) {
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    editText.error = if(editText.text.toString().isNotEmpty()) null else "Field Cannot be Empty"
                }
         })
 }

}


