package com.example.minesweeper

import android.content.Context
import androidx.appcompat.widget.AppCompatButton

class Cell(context: Context) : AppCompatButton(context) {

    var value : Int =0
    var isRevealed = false
    var isFlagged = false
    var isMined = false
}