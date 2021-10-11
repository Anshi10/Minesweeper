package com.example.minesweeper

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_minesweeper_game.*
import kotlin.random.Random
import com.example.minesweeper.Cell as cellBoard

val xDir : Array<Int> = arrayOf(-1, -1, 0, 1, 1, 1, 0, -1)
val yDir : Array<Int> = arrayOf(0, 1, 1, 1, 0, -1, -1, -1)
class minesweeperGame : AppCompatActivity() {

    private lateinit var chronometer: Chronometer
    private var isChronometerRunning = false
    var status = Status.ONGOING
    //setting the flagCount = 0 ,flagCount is no. of flags marked
    private var flagCount : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minesweeper_game)

        //Recieving intents
        val Rows = intent.getIntExtra("Rows", 0)
        val Cols = intent.getIntExtra("Cols", 0)
        val Mines = intent.getIntExtra("Mines", 0)

        //this will set up the board as per the user input
        setupBoard(Rows, Cols, Mines)

        //Restarting the game
        restart.setOnClickListener {
          val intent = Intent(this , minesweeperGame::class.java)
            intent.putExtra("Rows",Rows)
            intent.putExtra("Cols",Cols)
            intent.putExtra("Mines",Mines)
            startActivity(intent)
            finish()
        }
    }
   //alert dialogues to get permission to exit the game on Back button pressed
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        with(builder){
            setTitle(R.string.onBackPressed)
            if(isChronometerRunning){
                setMessage(R.string.exitMessage)
                setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        status = Status.LOST
                        updateScore()
                        val intent = Intent(this@minesweeperGame, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                })
            }
            else{
                setMessage(R.string.exitingwithoutplaying)
                setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        val intent = Intent(this@minesweeperGame, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                })
            }
            setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }

                })
            }
            val alertdialog = builder.create()
            alertdialog.show()
    }

    //set the Board according to the level of difficulty or custom Board requirements
    private fun setupBoard(Rows: Int, Cols: Int, Mines: Int) {
        var counter = 1
        var isfirstClicked = true
        var Minespos = mutableListOf<cellBoard>()
        //setting total number of Mines
        Minesleft.text = Mines.toString()
        // Array of buttons to find the position of a particular button
        val cellBoard = Array(Cols) { Array(Rows) { cellBoard(this) } }

        val Rowparams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        )
        val Cellparams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        for (i in 0 until Rows) {
            val Row = LinearLayout(this)
            Row.layoutParams = Rowparams
            Row.orientation = LinearLayout.HORIZONTAL
            Rowparams.weight = 1.0F
            for (j in 0 until Cols) {
                val cell = cellBoard(this)
                cell.layoutParams = Cellparams
                Cellparams.weight = 1.0F
                cell.setBackgroundResource(R.drawable.plain_button)
                //Buttons are being stored to their corresponding locations in the array
                cellBoard[i][j] = cell

                 cell.setOnClickListener{
                     //when first clicked start the game
                     if (isfirstClicked) {
                         isfirstClicked = false

                         //setting up mines
                        Minespos=setMines(i,j,Mines, cellBoard, Rows, Cols)
                         //start Timer
                         startTimer()
                     }

                     Move(i, j, cellBoard, Rows, Cols,)
                     display(cellBoard)

                     //if clicked cell is flagged
                     handleFlaggedCell(i,j,cellBoard,Mines)

                 }
            cell.setOnLongClickListener{
                    longpressedButton(cell,Mines,Minespos,cellBoard)
                }

                Row.addView(cell)
                counter++
            }
            board.addView(Row)
        }
    }
    //check status for win or Loose
    private fun checkStatus(Minespos : MutableList<cellBoard>,Mines :Int,cellBoard: Array<Array<cellBoard>>) {
             val size = Minespos.size
             var count = 0
          for(i in 0 until size){
              if(Minespos[i].isFlagged)
                  count++
          }
        if(count==Mines){
            status = Status.WON
            val intent = Intent(this,Result::class.java)
            intent.putExtra("status","You Win!\n CONGRATULATIONS")
            startActivity(intent)
        }else{
            status= Status.LOST
            handleMine()
        }
        updateScore()
        display(cellBoard)
    }

    private fun handleFlaggedCell(i: Int, j: Int,cellBoard: Array<Array<com.example.minesweeper.Cell>>,Mines: Int) {
        if(cellBoard[i][j].isFlagged){
            cellBoard[i][j].isFlagged = false
            cellBoard[i][j].setBackgroundResource(R.drawable.plain_button)
            flagCount--
            Minesleft.text = (Mines - flagCount).toString()
        }
    }
    //this function is called when you mark the cell.⛳
    private fun longpressedButton(cell: cellBoard,Mines :Int,Minespos: MutableList<com.example.minesweeper.Cell>,cellBoard: Array<Array<cellBoard>>): Boolean {
        if(!cell.isRevealed){
            cell.isFlagged=true
            cell.setBackgroundResource(R.drawable.flagg)
            flagCount++
            //updating the value of minesleft = total no. of mines - no. of flagged cell
            val minesLeft = Mines- flagCount
            Minesleft.text = minesLeft.toString()
        }
        if(flagCount == Mines){
            checkStatus(Minespos,Mines,cellBoard)
        }
        return true
    }

    private fun display(cellBoard: Array<Array<cellBoard>>) {
             cellBoard.forEach {row->
                 row.forEach{
                   if(it.isRevealed && !it.isFlagged)  setNumberImage(it)
                     if(it.isFlagged) it.setBackgroundResource(R.drawable.flagg)
                     if(status == Status.LOST && it.isMined) it.setBackgroundResource(R.drawable.bomb)

                     if(status==Status.LOST && it.isFlagged && !it.isMined) it.setBackgroundResource(R.drawable.crossedflag)
                     if(status==Status.LOST && it.isFlagged && it.isMined) it.setBackgroundResource(R.drawable.flagg)
                 }

             }
    }
   //this function is called when a player make a move
    private fun Move(i: Int, j: Int, cellBoard: Array<Array<com.example.minesweeper.Cell>>, Rows: Int, Cols: Int) : Boolean{
           if(cellBoard[i][j].isRevealed || cellBoard[i][j].isFlagged)  return false
           if(cellBoard[i][j].value>0){
               cellBoard[i][j].isRevealed = true
               return true
           }
        if(cellBoard[i][j].value == -1 || cellBoard[i][j].isMined){
            status = Status.LOST
            updateScore()
            handleMine()
            return true
        }
        if(cellBoard[i][j].value==0){
            setNumberImage(cellBoard[i][j])
            handlezero(i,j,cellBoard,Rows,Cols)
            return true
        }
        return false
    }

    private fun updateScore() {
         chronometer.stop()
        isChronometerRunning = false
             val  saveTime = SystemClock.elapsedRealtime() - chronometer.base
             val time = saveTime.toInt()
        val pref: SharedPreferences = getSharedPreferences("score",Context.MODE_PRIVATE)
        val editor = pref.edit()
        if(status == Status.WON){
            val bestTime : Int =  time
            val prevbestTime = pref.getInt("BestTime", Int.MAX_VALUE)
            Log.d("before if",prevbestTime.toString())
            Log.d("beforebestime","${bestTime}")
            if(prevbestTime>=bestTime || prevbestTime <=0){
                editor.putInt("BestTime",bestTime)
                Log.d("bestime","${bestTime}")
            }
            editor.putInt("LastGameTime",time)
            editor.apply()
        }
        if(status==Status.LOST){
            editor.putInt("LastGameTime",time)
            editor.apply()
            Log.d("LastGametime","$time")
        }
    }
   //this function is called when you click mined cell.
    private fun handleMine() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this,Result::class.java)
            intent.putExtra("status","You Loose!\n BETTER LUCK NEXT TIME")
            startActivity(intent)
            finish()
        },1000)
    }
    //when the clicked cell has value = 0
    private fun handlezero(i: Int, j: Int, cellBoard: Array<Array<cellBoard>>, Rows: Int, Cols: Int) {
        cellBoard[i][j].isRevealed = true
        for(k in 0..7){
            val r  = i + xDir[k]
            val c = j+ yDir[k]
            Log.d("before for loop","Rows = ${r} , column = ${c}")
            if(r in (0 until Rows) && c in (0 until Cols)  ) {
                Log.d("Handle Zero","Row = ${r} , column = ${c}")
                if(cellBoard[r][c].value>0 && !cellBoard[r][c].isFlagged && !cellBoard[r][c].isRevealed){
                    cellBoard[r][c].isRevealed=true

                }
                if(!cellBoard[r][c].isRevealed && !cellBoard[r][c].isFlagged && cellBoard[r][c].value==0 ){
                    cellBoard[r][c].isRevealed=true
                    handlezero(r,c,cellBoard,Rows,Cols)
                }
            }
        }
    }

    //setting mines on random position on board
    private fun setMines(currRow :Int, currCol : Int,Mines: Int, cellBoard: Array<Array<cellBoard>>, Rows: Int, Cols: Int) : MutableList<cellBoard> {
        val Minespos : MutableList<cellBoard> = mutableListOf()
         var i = 1
        while(i<=Mines){
            val r = Random.nextInt(0, Rows)
            val c = Random.nextInt(0, Cols)
            if(cellBoard[r][c].isMined || cellBoard[r][c].value == -1 || cellBoard[r][c]==cellBoard[currRow][currCol]){
                Log.d("ANSHIKA","THIS ROW AND COLUMN IS ALREADY OCCUPIED")
                continue
            }
            Log.d("ANSHIKA","${r} , ${c}  mine HERE")
            cellBoard[r][c].isMined = true
            cellBoard[r][c].value = -1
            Minespos.add(cellBoard[r][c])
            i++
        }
        updateNeighbours(cellBoard, Rows, Cols)
        return Minespos
    }

    //updating Neighbours of Mined cells
    private fun updateNeighbours(cellBoard: Array<Array<cellBoard>>, Rows: Int, Cols: Int) {
        for (i in 0 until Rows) {
            for (j in 0 until Cols) {
                if(cellBoard[i][j].value != -1) {
                    for (k in 0..7) {
                        val x = xDir[k]
                        val y = yDir[k]
                        if ((i + x) in (0 until Rows) && (j + y) in (0 until Cols)) {
                            if (cellBoard[i + x][j + y].value == -1) {
                                cellBoard[i][j].value++
                            }
                        }
                    }
                }
            }
        } }

    //start timer when
    private fun startTimer() {
          chronometer = findViewById(R.id.timer)
          chronometer.base = SystemClock.elapsedRealtime()
          chronometer.start()
          isChronometerRunning = true
    }
    // This function will display images according to status
    // Game status is checked in display function (Called from display function)
    private fun setNumberImage(button: cellBoard) {
        if(button.value==0) button.setBackgroundResource(R.drawable.zero)
        if(button.value==1) button.setBackgroundResource(R.drawable.one)
        if(button.value==2) button.setBackgroundResource(R.drawable.two)
        if(button.value==3) button.setBackgroundResource(R.drawable.three)
        if(button.value==4) button.setBackgroundResource(R.drawable.four)
        if(button.value==5) button.setBackgroundResource(R.drawable.five)
        if(button.value==6) button.setBackgroundResource(R.drawable.six)
        if(button.value==7) button.setBackgroundResource(R.drawable.seven)
        if(button.value==8) button.setBackgroundResource(R.drawable.eight)
    }
}
// Includes all the three game status
enum class Status{
    WON,
    ONGOING,
    LOST
}