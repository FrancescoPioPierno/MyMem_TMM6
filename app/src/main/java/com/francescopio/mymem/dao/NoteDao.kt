package com.francescopio.mymem.dao

import androidx.room.*
import com.francescopio.mymem.entities.Notes

/*
All'interno della classe "Dao" si inseriranno tutte le operazioni principali tra cui, ricerca (tramite SELECT * FROM)
operazione di inserimento ed eliminazione.
Vengono implementate le suspend fun perchè SONO necessarie per gestire le Coroutines. Le suspend fun
sono funzioni che possono essere messe in pausa ed essere riprese successivamente.

*/


@Dao
interface NoteDao {

    //Funzione che mi restituirà il totale numero di oggetti.

    @Query("SELECT * FROM notes ORDER BY id DESC")
    suspend fun getAllNotes(): List<Notes>

    //Funzione che restituisce uno specifico oggetto.

    @Query("SELECT * FROM notes WHERE id =:id")
    suspend fun getSpecificNote(id:Int): Notes

    //Funzione che inserisce un oggetto all'interno del database.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     suspend fun insertNotes(note:Notes)

     //Funzione che elimina un oggetto.

    @Delete
     suspend fun deleteNote(note:Notes)

     //Funzione che elimina un oggetto specifico.

    @Query("DELETE FROM notes WHERE id =:id")
    suspend fun deleteSpecificNote(id:Int)

    //Funzione che permette di modificare i vari oggetti esistenti.

    @Update
    suspend fun updateNote(note:Notes)


}