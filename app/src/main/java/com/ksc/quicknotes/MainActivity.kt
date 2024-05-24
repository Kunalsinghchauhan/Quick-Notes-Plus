package com.ksc.quicknotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ksc.quicknotes.adapter.NoteAdapter
import com.ksc.quicknotes.database.NoteDatabase
import com.ksc.quicknotes.databinding.ActivityMainBinding
import com.ksc.quicknotes.models.Note
import com.ksc.quicknotes.models.NoteViewModel


class MainActivity : AppCompatActivity(), NoteAdapter.NoteItemClickListener,
    PopupMenu.OnMenuItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var notesAdapter: NoteAdapter
    private lateinit var database: NoteDatabase
    private lateinit var selectedNote: Note
    private lateinit var viewModel: NoteViewModel
    private val updateNote =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val note = result.data?.getSerializableExtra("note") as? Note
                if (note != null) {
                    viewModel.updateNote(note)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        if (auth.currentUser == null) {
            val intent = Intent(this, SignInActivity2::class.java)
            startActivity(intent)
            finish()
        }
        else{
            Toast.makeText(this, "Welcome ${auth.currentUser?.displayName}", Toast.LENGTH_SHORT).show()
        }
        database = NoteDatabase.getDatabase(this)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[NoteViewModel::class.java]

        viewModel.allNotes.observe(this) { list ->
            list?.let {
                notesAdapter.updateList(it)
            }
        }
        initUI()
    }

    private fun initUI() {
        binding.rvMain.setHasFixedSize(true)
        binding.rvMain.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        notesAdapter = NoteAdapter(this, this)
        binding.rvMain.adapter = notesAdapter
        val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val note = it.data?.getSerializableExtra("note") as? Note
                    if (note != null) {
                        viewModel.insertNote(note)
                        db.child("notes").child(auth.currentUser?.uid.toString())
                            .child(db.push().key.toString()).setValue(note)
                            .addOnCompleteListener { firebaseTask ->
                                if (firebaseTask.isSuccessful) {
                                    Snackbar.make(binding.root, "Note Added", Snackbar.LENGTH_SHORT)
                                        .show()
                                } else {
                                    Snackbar.make(binding.root, "Some Error", Snackbar.LENGTH_SHORT)
                                        .show()
                                }
                            }
                    }
                }
            }
        binding.btnSignOut.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, SignInActivity2::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }

        binding.fabAddNote.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            getContent.launch(intent)
        }

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    notesAdapter.searchNotes(newText)
                }
                return true
            }

        })
    }

    override fun onItemClicked(note: Note) {
        val intent = Intent(this@MainActivity, AddNoteActivity::class.java)
        intent.putExtra("current_note", note)
        updateNote.launch(intent)
    }

    override fun onLongItemClicked(note: Note, cardView: CardView) {
        selectedNote = note
        popUpDisplay(cardView)
    }

    private fun popUpDisplay(cardView: CardView) {
        val popup = PopupMenu(this, cardView)
        popup.setOnMenuItemClickListener {
            Snackbar.make(binding.root, "Deleted", Snackbar.LENGTH_SHORT).show()
            viewModel.deleteNote(selectedNote)
            true
        }
        popup.inflate(R.menu.pop_up_menu)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.delete_note) {
            viewModel.deleteNote(selectedNote)
            return true
        }
        return true

    }

}


