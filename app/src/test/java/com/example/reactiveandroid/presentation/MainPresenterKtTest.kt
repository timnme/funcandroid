package com.example.reactiveandroid.presentation

import com.example.reactiveandroid.domain.*
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MainPresenterKtTest {

    @BeforeAll
    fun beforeAll() {
        mockkStatic("com.example.reactiveandroid.domain.MainInteractorKt")
    }

    @Test
    fun onViewStateRenderIntention_NotesLoadSuccess_VerifyNoError() {
        val testNotes = listOf(
            Note(
                id = 1,
                text = "Hello",
                date = 1589691395000
            )
        )
        every { any<Single<Unit>>().loadNotesUseCase() } returns Single.just(testNotes)
        val expected = MainViewState(
            text = "",
            notes = listOf(
                UiNote(
                    id = 1,
                    text = "Hello",
                    date = "2020/05/17"
                )
            ),
            error = null
        )

        val testObserver = Single
            .just(Unit)
            .onFirstRenderIntention(MainViewState())
            .test()

        testObserver.assertValue {
            it == expected
        }
    }

    @Test
    fun onNoteAddIntention_NonEmptyNoteText_VerifyCorrectList() {
        val testNote = Note(
            id = 1,
            text = "Hello",
            date = 1589691395000
        )
        val existingUiNote = UiNote(
            id = 0,
            text = "Hi",
            date = "2020/05/16"
        )
        every { currentTime() } returns 1589691395000
        every { any<Single<String>>().addNoteUseCase() } returns Single.just(1)
        every { any<Single<Int>>().loadNoteByIdUseCase() } returns Single.just(testNote)
        val expected = MainViewState(
            text = "",
            notes = listOf(
                existingUiNote,
                UiNote(
                    id = 1,
                    text = "Hello",
                    date = "2020/05/17"
                )
            ),
            error = null
        )

        val testObserver = Observable
            .just("Hello")
            .onNoteAddIntention(MainViewState(notes = listOf(existingUiNote)))
            .test()

        testObserver.assertValueAt(0, expected)
    }

    @Test
    fun onNoteDeleteIntention() {

    }
}