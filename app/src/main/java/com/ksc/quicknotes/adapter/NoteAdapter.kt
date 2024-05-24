package com.ksc.quicknotes.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.ksc.quicknotes.R
import com.ksc.quicknotes.models.Note

class NoteAdapter(context: Context, private val listener: NoteItemClickListener) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteLayout: CardView = itemView.findViewById(R.id.list_item_card_view)
        val title: TextView = itemView.findViewById(R.id.tv_title)
        val note: TextView = itemView.findViewById(R.id.tv_note)
        val dateTime: TextView = itemView.findViewById(R.id.tv_time_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_items, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = noteList[position]
        holder.title.text = currentNote.title
        holder.title.isSelected = true
        holder.note.text = currentNote.note
        holder.dateTime.text = currentNote.date
        holder.dateTime.isSelected = true

        holder.noteLayout.setCardBackgroundColor(holder.itemView.resources.getColor(randomColor()))
        holder.noteLayout.setOnClickListener {
            listener.onItemClicked(noteList[holder.adapterPosition])
        }
        holder.noteLayout.setOnLongClickListener {
            listener.onLongItemClicked(noteList[holder.adapterPosition], holder.noteLayout)
            true
        }
    }

    private val allNotes = ArrayList<Note>()
    private val noteList = ArrayList<Note>()

    fun searchNotes(search: String) {
        noteList.clear()
        for (note in allNotes) {
            if (note.title?.lowercase()?.contains(search.lowercase()) == true ||
                note.note?.lowercase()?.contains(search.lowercase()) == true
            ) {
                noteList.add(note)
            }
        }
        notifyDataSetChanged()
    }

    fun updateList(newList: List<Note>) {
        allNotes.clear()
        allNotes.addAll(newList)

        noteList.clear()
        noteList.addAll(allNotes)
        notifyDataSetChanged()
    }

    interface NoteItemClickListener {
        fun onItemClicked(note: Note)
        fun onLongItemClicked(note: Note, cardView: CardView)
    }

    private fun randomColor(): Int {
        val colorList = ArrayList<Int>()
        colorList.add(R.color.NoteColor1)
        colorList.add(R.color.NoteColor2)
        colorList.add(R.color.NoteColor3)
        colorList.add(R.color.NoteColor4)
        colorList.add(R.color.NoteColor5)
        colorList.add(R.color.NoteColor6)

        val randomIndex = (0..5).random()
        return colorList[randomIndex]
    }

}