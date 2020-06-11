package com.example.reactiveandroid.data

import androidx.room.*
import com.example.reactiveandroid.App
import com.example.reactiveandroid.domain.Note
import io.reactivex.Single

@Database(entities = [DbNote::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(note: DbNote): Single<Long>

    @Query("SELECT * FROM notes")
    fun getAll(): Single<List<DbNote>>

    @Query("SELECT * FROM notes WHERE id=:id LIMIT 1")
    fun getById(id: Int): Single<DbNote>

    @Query("DELETE FROM notes WHERE id=:id")
    fun deleteById(id: Int): Single<Int>
}

@Entity(tableName = "notes")

data class DbNote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val date: Long
) {
    fun toDomain() = Note(
        id = id,
        text = text,
        date = date
    )
}

private val noteDao: NoteDao by lazy {
    Room
        .databaseBuilder(App.appContext, AppDatabase::class.java, "app_database")
        .build()
        .noteDao()
}

fun Single<*>.loadNotes(): Single<List<Note>> = this
    .flatMap { noteDao.getAll() }
    .map { notes -> notes.map { it.toDomain() } }

fun Single<Int>.loadNoteById(): Single<Note> = this
    .flatMap { id -> noteDao.getById(id) }
    .map { it.toDomain() }

fun Single<Pair<String, Long>>.addNote(): Single<Int> = this
    .flatMap { (text, date) -> noteDao.add(DbNote(text = text, date = date)) }
    .map { it.toInt() }

fun Single<Int>.deleteNote(): Single<Int> = this
    .flatMap { noteDao.deleteById(it) }