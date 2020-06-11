package com.example.reactiveandroid.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.reactiveandroid.R
import com.example.reactiveandroid.toObservable
import com.example.reactiveandroid.toSingle
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_note.view.*

@Parcelize
data class MainViewState(
    val text: String = String(),
    val notes: List<UiNote> = emptyList(),
    val error: String? = null
) : ViewState<MainViewState>() {
    override fun eventsResetState() = copy(error = null)
}

class MainActivity : BaseActivity<MainViewState>() {
    override val layoutId: Int = R.layout.activity_main

    override var currentViewState: MainViewState = MainViewState()

    private val adapter: NoteAdapter by lazy { NoteAdapter() }

    override fun setup() {
        recyclerView.adapter = adapter
    }

    override fun inputs() {
        addButton
            .clicks()
            .flatMap {
                editText.text.toString()
                    .toObservable()
                    .onNoteAddIntention(currentViewState)
            }
            .renderSubscribe()
    }

    override fun MainViewState.render() {
        editText.text.clear()
        editText.text.insert(0, text)

        adapter.notes = notes
        adapter.notifyDataSetChanged()

        if (error != null) {
            Toast
                .makeText(this@MainActivity, error, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun Single<Unit>.onAbsentSavedViewState(): Single<MainViewState> =
        onFirstRenderIntention(currentViewState)

    private inner class NoteAdapter(
        var notes: List<UiNote> = emptyList()
    ) : RecyclerView.Adapter<NoteHolder>() {
        override fun getItemCount(): Int = notes.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NoteHolder(parent)
        override fun onBindViewHolder(holder: NoteHolder, position: Int) {
            holder.bind(notes[position])
        }
    }

    private inner class NoteHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
    ) {
        fun bind(note: UiNote) {
            with(itemView) {
                text.text = note.text
                date.text = note.date
                delete
                    .clicks()
                    .take(1)
                    .singleOrError()
                    .flatMap {
                        note.id
                            .toSingle()
                            .onNoteDeleteIntention(currentViewState)
                    }
                    .renderSubscribe()
            }
        }
    }
}


