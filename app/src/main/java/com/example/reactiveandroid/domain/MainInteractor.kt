package com.example.reactiveandroid.domain

import com.example.reactiveandroid.data.addNote
import com.example.reactiveandroid.data.deleteNote
import com.example.reactiveandroid.data.loadNoteById
import com.example.reactiveandroid.data.loadNotes
import com.example.reactiveandroid.toSingle
import io.reactivex.Single

data class Note(
    val id: Int,
    val text: String,
    val date: Long
)

fun currentTime(): Long = System.currentTimeMillis()

fun Single<*>.loadNotesUseCase(): Single<List<Note>> = this
    .loadNotes()

fun Single<Int>.loadNoteByIdUseCase(): Single<Note> = this
    .loadNoteById()

fun Single<String>.addNoteUseCase(): Single<Int> = this
    .map { it to currentTime() }
    .addNote()

fun Single<Int>.deleteNoteUseCase(): Single<Int> = this
    .flatMap { id ->
        id
            .toSingle()
            .deleteNote()
            .map { if (it > 0) id else -1 }
    }