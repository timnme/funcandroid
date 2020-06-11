package com.example.reactiveandroid.presentation

import android.os.Parcelable
import com.example.reactiveandroid.domain.*
import com.example.reactiveandroid.toIo
import com.example.reactiveandroid.toSingle
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ru"))

@Parcelize
data class UiNote(
    val id: Int,
    val text: String,
    val date: String
) : Parcelable

private fun Note.toUi(): UiNote = UiNote(
    id = id,
    text = text,
    date = dateFormat.format(Date(date))
)

fun Single<Unit>.onFirstRenderIntention(
    current: MainViewState
): Single<MainViewState> = this
    .toIo()
    .loadNotesUseCase()
    .reduce(
        { current.copy(notes = it.map { note -> note.toUi() }) },
        { current.copy(error = it.message.toString()) }
    )

fun Observable<String>.onNoteAddIntention(
    current: MainViewState
): Observable<MainViewState> = this
    .toIo()
    .map { it.trim() }
    .flatMap { noteText ->
        if (noteText.isNotEmpty()) {
            noteText
                .toSingle()
                .addNoteUseCase()
                .loadNoteByIdUseCase()
        } else {
            Single
                .error(Exception("Can't save empty note!"))
        }
            .reduce(
                { current.copy(text = String(), notes = current.notes + it.toUi()) },
                { current.copy(text = noteText, error = it.message.toString()) }
            )
            .toObservable()
    }

fun Single<Int>.onNoteDeleteIntention(
    current: MainViewState
): Single<MainViewState> = this
    .toIo()
    .deleteNoteUseCase()
    .reduce(
        { current.copy(notes = current.notes.filter { note -> note.id != it }) },
        { current.copy(error = it.message.toString()) }
    )

private fun <T, VS : ViewState<VS>> Single<T>.reduce(
    onSuccess: (T) -> VS,
    onError: (Throwable) -> VS
): Single<VS> = this
    .map { onSuccess(it) }
    .onErrorReturn { onError(it) }