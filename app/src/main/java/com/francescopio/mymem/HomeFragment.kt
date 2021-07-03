package com.francescopio.mymem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.widget.SearchView
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.francescopio.mymem.adapter.NotesAdapter
import com.francescopio.mymem.database.NotesDatabase
import com.francescopio.mymem.entities.Notes
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


/*
All'interno della classe HomeFragment verranno gestiti ed estratte tutte le informazioni riguardanti
ai nostri oggetti, ed inoltre di andare al crearli. Il tutto viene gestito con il richiamo dell'Adapter
appartenente alla RecycleView. L'Adapter è  responsabile di estrarre dal data Source (che di conseguenza
popola i ViewHolder). Inoltre i dati conservati vengono inviati alla RecycleView.
 */


class HomeFragment : BaseFragment() {

    var arrNotes = ArrayList<Notes>()

    var notesAdapter: NotesAdapter = NotesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        recycler_view.setHasFixedSize(true)

        recycler_view.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        launch{
            //Ottengo tutte le informazioni dalla coroutine
            context?.let {

                var notes = NotesDatabase.getDatabase(it).noteDao().getAllNotes()
                notesAdapter!!.setData(notes)
                arrNotes = notes as ArrayList<Notes>
                recycler_view.adapter = notesAdapter
            }
        }

        notesAdapter!!.setOnClickListener(onClicked)

        //Implementazione del metodo setOnClickListener per la creazione dell'oggetto.
        fabBtnCreateNote.setOnClickListener {
            replaceFragment(CreateNoteFragment.newInstance(), false)
        }

        //Implementazione del metodo che permette di andare ad effettuare una search degli oggetti al momento.
        search_view. setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            /*
            /All'interno di questa funzione, i risultati della query (la ricerca avviene tramite titolo)
            verranno salvati all'interno di un array "temporaneo". Se la query di ricerca va a buon fine, verranno
            restituiti tutti gli oggetti che hanno come titolo la stringa ricercata.
             */

            override fun onQueryTextChange(p0: String?): Boolean {

                var tempArr = ArrayList<Notes>()
                for (arr in arrNotes){
                    if (arr.title!!.toLowerCase(Locale.getDefault()).contains(p0.toString())){
                       tempArr.add(arr)
                    }
                }

                notesAdapter.setData(tempArr)
                notesAdapter.notifyDataSetChanged()
                return true
            }


        })

    }

    private val onClicked = object :NotesAdapter.OnItemClickListener{
        override fun onClicked(notesId: Int) {

            var fragment :Fragment
            var bundle = Bundle()
            bundle.putInt("noteId",notesId)
            fragment = CreateNoteFragment.newInstance()
            fragment.arguments = bundle


            replaceFragment(fragment, false)

        }

    }



    fun replaceFragment(fragment:Fragment, istransition:Boolean){
        val fragmentTransition = requireActivity().supportFragmentManager.beginTransaction()

        if (istransition){
            fragmentTransition.setCustomAnimations(android.R.anim.slide_out_right,android.R.anim.slide_in_left)
        }
        fragmentTransition.replace(R.id.frame_layout,fragment).addToBackStack(fragment.javaClass.simpleName).commit()
    }

}