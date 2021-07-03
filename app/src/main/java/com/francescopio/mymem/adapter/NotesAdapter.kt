package com.francescopio.mymem.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.francescopio.mymem.R
import com.francescopio.mymem.entities.Notes
import kotlinx.android.synthetic.main.item_rv_notes.view.*


/*
Implementazione della classe Adapter per associarla ad una classe RecyclerView.
 */

class NotesAdapter() :
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {
    var listener:OnItemClickListener? = null
    var arrList = ArrayList<Notes>()

    /* Si crea una funzione onCreateViewHolder per inizializzare alcuni campi privati che saranno utilizzati dalla
    RecyclerView
     */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_rv_notes, parent, false)
        )
    }

    //Funzione getItemCounter che mi restituisce il numero totale di oggetti all'interno del mio array.

    override fun getItemCount(): Int {
        return arrList.size
    }

    //Funzione setData che permette di andare a "settare" gli oggetti al mio array grazie all'Adapter.


    fun setData(arrNotesList: List<Notes>){

        arrList = arrNotesList as ArrayList<Notes>

    }

    //Questa funzione è stata implementata perchè di default l'Adapter non può implementare onItemClickListener.

    fun setOnClickListener(listener1:OnItemClickListener){

        listener = listener1

    }


    /*
        Associa ogni elemento dell'arrayList all'interno della View.

     */

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {

        //Associazione di tutti gli oggetti. Un elemento della RecycleView ha un titolo, una descrizione ed una data.


        holder.itemView.tvTitle.text = arrList[position].title
        holder.itemView.tvDesc.text = arrList[position].noteText
        holder.itemView.tvDateTime.text = arrList[position].dateTime

        /*
            Tutte le operazioni della RecycleView vengono gestiti tramite degli If-else. Esempio:
            Se sono presenti delle immagini, colori ed ecc, essi verranno mostrati. Se all'interno della
            nota non vi sono quest'ultimi, la RecycleView non li mostrerà.
        */

       if (arrList[position].color !=null){
           holder.itemView.cardView.setCardBackgroundColor(Color.parseColor(arrList[position].color))
       } else{
           holder.itemView.cardView.setCardBackgroundColor(Color.parseColor(R.color.ColorLightBlack.toString()))
       }
        if (arrList[position].imgPath != null){
            holder.itemView.imgNote.setImageBitmap(BitmapFactory.decodeFile(arrList[position].imgPath))
            holder.itemView.imgNote.visibility = View.VISIBLE
        }else{
            holder.itemView.imgNote.visibility = View.GONE
        }

        if (arrList[position].webLink != null){
            holder.itemView.tvWebLink.text = arrList[position].webLink
            holder.itemView.tvWebLink.visibility = View.VISIBLE
        }else{
            holder.itemView.tvWebLink.visibility = View.GONE
        }

        holder.itemView.cardView.setOnClickListener{

            listener!!.onClicked(arrList[position].id!!)
        }

    }

    class NotesViewHolder(view: View): RecyclerView.ViewHolder(view){



    }

    //Interfaccia che permette di implementare il "click" per ogni oggetto appartenente alla RecycleView.

    interface OnItemClickListener{

        fun onClicked(noteId:Int)
    }

}