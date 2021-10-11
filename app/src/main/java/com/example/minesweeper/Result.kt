package com.example.minesweeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_result.*

class Result : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        //setting the Result
        val Result = intent.getStringExtra("status")
        Status.setText(Result)

        //changing the colour of button as per status
        if(Result?.equals("You Win!\n CONGRATULATIONS") == true){
             button.setBackgroundResource(R.drawable.winbutton)
        }

        //on clicking Play Button
        button.setOnClickListener{
            val intent = Intent(this , MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}