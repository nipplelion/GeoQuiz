package com.bignerdranch.android.geoquiz

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider


private const val KEY_INDEX = "index"
private const val KEY_CHEATER = "cheater"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {

    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var cheatButton: Button
    private lateinit var questionTextView: TextView

    private val TAG = "MainActivity"

    private val quizViewModel: QuizViewModel by lazy { ViewModelProvider(this)[QuizViewModel::class.java] }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate(Bundle?) called")

        // New way to do onActivityResult()
        var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                quizViewModel.isCheater[quizViewModel.getIndex] = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) == true
            }
        }

        val currentIndex = savedInstanceState?.get(KEY_INDEX) ?: 0
        quizViewModel.setCurrentIndex(currentIndex as Int)

        val isCheater = (savedInstanceState?.get(KEY_CHEATER) ?: false)
        quizViewModel.isCheater[currentIndex] = isCheater as Boolean

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        prevButton = findViewById(R.id.prev_button)
        questionTextView = findViewById(R.id.question_text_view)
        cheatButton = findViewById(R.id.cheat_button)

        updateAnswerButtons()

        questionTextView.setOnClickListener { view: View ->
            changeQuestionIndex()
            updateQuestion()
        }

        trueButton.setOnClickListener { view: View ->
            checkAnswer(true)
            quizViewModel.updateButtonStatus(true)
            updateAnswerButtons()
        }

        falseButton.setOnClickListener { view: View ->
            checkAnswer(false)
            quizViewModel.updateButtonStatus(true)
            updateAnswerButtons()
        }

        nextButton.setOnClickListener {
            changeQuestionIndex()
            updateQuestion()
            updateAnswerButtons()
        }

        prevButton.setOnClickListener {
            changeQuestionIndex(false)
            updateQuestion()
            updateAnswerButtons()
        }

        cheatButton.setOnClickListener { view ->
            // Start CheatActivity
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            val options = ActivityOptionsCompat.makeClipRevealAnimation(view, 0, 0, view.width, view.height)
            resultLauncher.launch(intent, options)
        }

        updateQuestion()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(TAG, "onSaveInstanceState")
        outState.putInt(KEY_INDEX, quizViewModel.getIndex)
        outState.putBoolean(KEY_CHEATER, quizViewModel.isCheater[quizViewModel.getIndex])

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    private fun updateAnswerButtons() {
        Log.d("status", quizViewModel.getIndex.toString())
        if (!quizViewModel.currentButtonStatus) {
            enableAnswerButtons()
        } else {
            disableAnswerButtons()
        }

        var questionsCorrect = quizViewModel.getQuestionsCorrect
        var questionBankSize = quizViewModel.getQuestionBankSize
        Log.d("status", String.format("%.2f", questionsCorrect.toDouble() / questionBankSize * 100.0))
        if (quizViewModel.allQuestionsAnswered) {
            Log.d("status", "hello")
            Toast.makeText(this, getString(R.string.score_text) + String.format("%.2f", questionsCorrect.toDouble() / questionBankSize * 100.0) + "%", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableAnswerButtons() {
        trueButton.isEnabled = false
        falseButton.isEnabled = false
    }

    private fun enableAnswerButtons() {
        trueButton.isEnabled = true
        falseButton.isEnabled = true
    }

    private fun updateQuestion() {
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)
    }

    private fun changeQuestionIndex(goNext: Boolean = true) {
        var currentIndex = quizViewModel.getIndex
        var questionBankSize = quizViewModel.getQuestionBankSize
        var traverseIndex = if (goNext) currentIndex + 1 else currentIndex - 1
        quizViewModel.setCurrentIndex(if (traverseIndex >= 0) (traverseIndex) % questionBankSize else (questionBankSize - (traverseIndex % questionBankSize) - 2))
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer

        if (userAnswer == correctAnswer) quizViewModel.updateQuestionsCorrect()
        Log.d("status", quizViewModel.isCheater[quizViewModel.getIndex].toString())
        val messageResId = when {
            quizViewModel.isCheater[quizViewModel.getIndex] -> R.string.judgment_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT)
            .show()
    }
}