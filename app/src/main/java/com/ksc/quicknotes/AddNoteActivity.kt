package com.ksc.quicknotes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.ksc.quicknotes.databinding.ActivityAddNoteBinding
import com.ksc.quicknotes.models.Note
import java.text.SimpleDateFormat
import java.util.Date

class AddNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var note: Note
    private lateinit var oldNote: Note
    private var isUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            oldNote = intent.getSerializableExtra("current_note") as Note
            binding.etTitle.setText(oldNote.title)
            binding.etNote.setText(oldNote.note)
            isUpdate = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.imgCheck.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val noteDesc = binding.etNote.text.toString()

            if (title.isNotEmpty() || noteDesc.isNotEmpty()) {
                val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")
                if (isUpdate) {
                    note = Note(
                        oldNote.id,
                        title,
                        noteDesc, formatter.format(Date())
                    )
                } else {
                    note = Note(null, title, noteDesc, formatter.format(Date()))
                }
                val intent = Intent()
                intent.putExtra("note", note)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Snackbar.make(binding.root, "Please enter some data", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

        }

        binding.imgBackArrow.setOnClickListener {
            startActivity(Intent(this@AddNoteActivity, MainActivity::class.java))
            finish()
        }
    }
}