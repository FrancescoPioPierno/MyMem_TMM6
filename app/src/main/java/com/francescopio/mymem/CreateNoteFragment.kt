package com.francescopio.mymem

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.app.Activity.RESULT_OK
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.francescopio.mymem.database.NotesDatabase
import com.francescopio.mymem.entities.Notes
import com.francescopio.mymem.util.NoteBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Patterns
import kotlinx.android.synthetic.main.fragment_create_note.imgMore
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_note_bottom.*
import kotlinx.android.synthetic.main.item_rv_notes.view.*
import pub.devrel.easypermissions.AppSettingsDialog


/*
La classe CreateNoteFragment permette di andare a creare il nostro oggetto, la nostra nota.
Come si vuol notare, la classe estende la classe BaseFragment e EasyPermissions.
EasyPermission consente all'applicazione di chiedere il permesso all'utente di accedere al proprio storage,
in questo caso alla nostra galleria di foto.
 */


class CreateNoteFragment : BaseFragment(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    var selectedColor = "#171C26"

    //Si inizializza la data.
    var currentDate:String? = null
    private var READ_STORAGE_PERM = 123

    //Codice richiesta per l'immagine, per l'immagine selezionata, per il link e l'id dell'oggetto.

    private var REQUEST_CODE_IMAGE = 456
    private var selectedImagePath = ""
    private var webLink = ""
    private var noteId = -1

    //Funzione che permette di creare il primo oggetto. In caso contrario l'applicazione va in crash.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        noteId = requireArguments().getInt("noteId", -1)


    }

    // Override della funzione onCreateView che visualizza il layout per la creazione della nota.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_note, container, false)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            CreateNoteFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    /*
    (Override) In questa funzione onViewCreated, si estraggono tutte le informazioni relativi all'oggetto.
    Inoltre si decide quali informazioni andare a mostrare e cosa no soddisfacendo le condizioni
    all'interno del blocco if-else.
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (noteId != -1){

            launch{
                //Ottengo tutte le informazioni dalla coroutine
                context?.let {

                    var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)

                    colorView.setBackgroundColor(Color.parseColor(notes.color))

                    etNoteTitle.setText(notes.title)
                    etNoteSubTit.setText(notes.subTitle)
                    etNoteDesc.setText(notes.noteText)

                    //Se l'immagine non è vuota, essa verrà mostrata
                    if (notes.imgPath != ""){

                        selectedImagePath = notes.imgPath!!
                        imgNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
                        layoutImage.visibility = View.VISIBLE
                        imgNote.visibility = View.VISIBLE
                        imgDelete.visibility = View.VISIBLE


                    }else{
                        layoutImage.visibility = View.GONE
                        imgNote.visibility = View.GONE
                        imgDelete.visibility = View.GONE
                    }

                    if (notes.webLink != ""){
                        webLink = notes.webLink!!
                        tvWebLink.text = notes.webLink
                        layoutWebUrl.visibility = View.VISIBLE
                        imgUrlDelete.visibility = View.VISIBLE
                        etWebLink.setText(notes.webLink)
                        imgUrlDelete.visibility = View.VISIBLE
                    }else{
                        imgUrlDelete.visibility = View.VISIBLE
                        layoutWebUrl.visibility = View.GONE
                    }

                }
            }

        }


        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )
        //Si inserisce il formato della data della creazione dell'oggetto.
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        currentDate = sdf.format(Date())

        colorView.setBackgroundColor(Color.parseColor(selectedColor))

        tvDateTime.text = currentDate

        /*
        setOnClickListener è un metodo che consente di decidere che azione deve essere eseguita
        nel momento in cui si va a cliccare quel bottone. Se l'oggetto è stato creato, si può andare ad
        effettuare le modifiche, se no l'oggetto verrà salvato (eseguibile nell'else)
         */

        imgDone.setOnClickListener {

            if(noteId != -1){

                updateNote()

            }else{
                saveNote()
            }

        }
        imgBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        //Mostra il layout relativo alla scelta del colore, inserimento immagine e web URL nel momento del click.

        imgMore.setOnClickListener{

            var noteBottomSheetFragment = NoteBottomSheetFragment.newInstance(noteId)
            noteBottomSheetFragment.show(requireActivity().supportFragmentManager, "Note bottom sheet fragment")

        }
        //Consente di andare a rimuovere l'immagine.

        imgDelete.setOnClickListener{

                selectedImagePath = ""
                layoutImage.visibility = View.GONE

            }

        /*
        btnOk.setOnClickListener permette di andare a confermare la creazione del link. Se il link
        non è valido, apparirà un messaggio di avviso.
         */


        btnOk.setOnClickListener {
            if (etWebLink.text.toString().trim().isNotEmpty()){
                checkWebUrl()
            }else{
                Toast.makeText(requireContext(), "Url è richiesto", Toast.LENGTH_SHORT).show()
            }
        }

        //Consente di annullare l'inserimento della web URL.

        btnCancel.setOnClickListener{
            if (noteId != -1){

                tvWebLink.visibility = View.VISIBLE
                layoutWebUrl.visibility = View.GONE

            }else{

                layoutWebUrl.visibility = View.GONE
            }

        }

        //Bottone che rimuove completamente la web URL

        imgUrlDelete.setOnClickListener{
            webLink = ""
            tvWebLink.visibility = View.GONE
            imgUrlDelete.visibility = View.GONE
            layoutWebUrl.visibility = View.GONE
        }

        //Bottone che consente di andare ad inserire la web URL


        tvWebLink.setOnClickListener{

            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(etWebLink.text.toString()))
            //Avvio dell'Activity con l'intent.
            startActivity(intent)

        }

    }
    //Implementazione della funzione updateNote() che consente di modificare l'oggetto in questione.

    private fun updateNote(){

        launch{
            //Si estraggono le informazioni dalla coroutine.
            context?.let {

                var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)
                notes.title = etNoteTitle.text.toString()
                notes.subTitle = etNoteSubTit.text.toString()
                notes.noteText = etNoteDesc.text.toString()
                notes.dateTime = currentDate
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.webLink = webLink


                NotesDatabase.getDatabase(it).noteDao().updateNote(notes)
                etNoteTitle.setText("")
                etNoteSubTit.setText("")
                etNoteDesc.setText("")
                layoutImage.visibility = View.GONE
                imgNote.visibility = View.GONE
                tvWebLink.visibility = View.GONE
                requireActivity().supportFragmentManager.popBackStack()

            }
        }


    }
    //Implementazione della funzione saveNote() che crea il nostro oggetto.
    private fun saveNote(){
        //Aggiunta dei controlli per i vari campi: titolo, sottotitolo e descrizione.
        if (etNoteTitle.text.isNullOrEmpty()){
            Toast.makeText(context, "Titolo richiesto", Toast.LENGTH_SHORT).show()
        }
        else if (etNoteSubTit.text.isNullOrEmpty()){
            Toast.makeText(context, "Sottotitolo richiesto", Toast.LENGTH_SHORT).show()

        }

        else if (etNoteDesc.text.isNullOrEmpty()){
            Toast.makeText(context, "Descrizione richiesta", Toast.LENGTH_SHORT).show()
        }

        else{


        launch{
            //Si vanno ad inserire i dati all'interno del database tramite la Coroutine.
            var notes = Notes()
            notes.title = etNoteTitle.text.toString()
            notes.subTitle = etNoteSubTit.text.toString()
            notes.noteText = etNoteDesc.text.toString()
            notes.dateTime = currentDate
            notes.color = selectedColor
            notes.imgPath = selectedImagePath
            notes.webLink = webLink
            context?.let {
                NotesDatabase.getDatabase(it).noteDao().insertNotes(notes)
                etNoteTitle.setText("")
                etNoteSubTit.setText("")
                etNoteDesc.setText("")
                layoutImage.visibility = View.GONE
                imgNote.visibility = View.GONE
                tvWebLink.visibility = View.GONE
                requireActivity().supportFragmentManager.popBackStack()

            }
        }


        }

    }
    /*
    Implementazione della funzione deleteNote() che elimina completamente il nostro oggetto.
     */
    private fun deleteNote(){

        launch{
            context?.let{
                //Si elimina il nostro oggetto e verrà rimosso dalla tabella attraverso il metodo popBackStack()

                NotesDatabase.getDatabase(it).noteDao().deleteSpecificNote(noteId)
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

    }

    //Funzione checkWebUrl() che esegue un check al link inserito se sia corretto o meno.

    private fun checkWebUrl(){
        if (Patterns.WEB_URL.matcher(etWebLink.text.toString()).matches()){

            layoutWebUrl.visibility = View.GONE
            etWebLink.isEnabled = false
            webLink = etWebLink.text.toString()
            tvWebLink.visibility = View.VISIBLE
            tvWebLink.text = etWebLink.text.toString()

        }else{
            //Messaggio di errore in caso l'URL sia errata.
            Toast.makeText(requireContext(), "Url non è valido", Toast.LENGTH_SHORT).show()
        }

    }


    //Si crea un oggetto ricevitore broadcast per ricevere azioni
    private val BroadcastReceiver:BroadcastReceiver = object:BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {

            var actionColor=p1!!.getStringExtra("action")

            /*
            Si decide all'interno del costrutto when, che azione bisogna essere eseguita nel momento della scelta
            dell'operazione. Permette di selezionare i vari colori, di inserire immagini, link ed eliminare l'oggetto scelto.
             */
            when(actionColor!!){

                "Blue" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Yellow" -> {

                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "White" -> {

                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Purple" -> {

                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Green" -> {

                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Orange" -> {

                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Black" -> {

                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Image" -> {

                    readStorageTask()
                    layoutWebUrl.visibility = View.GONE

                }

                "WebUrl" -> {

                    layoutWebUrl.visibility = View.VISIBLE

                }

                "DeleteNote" -> {
                    //Eliminazione
                    deleteNote()

                }


                else -> {

                    layoutImage.visibility = View.GONE
                    imgNote.visibility = View.GONE

                    layoutWebUrl.visibility = View.GONE
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))

                }



            }
        }


        }
    //Distruzione del ricevitore.
    override fun onDestroy() {

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BroadcastReceiver)
        super.onDestroy()
    }

    //Creazione di una funzione "storage" che permetterà di accedere alla galleria.
    private fun hasReadStoragePerm():Boolean{

        return EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
    }


    //Funzione readStorageTask che permette di "leggere" la nostra galleria e di accedervi.
    private fun readStorageTask(){
        if (hasReadStoragePerm()){

            pickImageFromGallery()

        }else{

            //Se l'utente decide di non dare il permesso, apparirà un messaggio di warning che bisogna a tutti i costi entrare nella galleria.

            EasyPermissions.requestPermissions(
                requireActivity(),
                getString(R.string.storage_permission_text),
                READ_STORAGE_PERM,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    //Si crea una funzione che permette di scegliere un'immagine all'interno della galleria.

    private fun pickImageFromGallery(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
         if (intent.resolveActivity(requireActivity().packageManager) != null){
             startActivityForResult(intent,REQUEST_CODE_IMAGE)
         }
    }

    //Funzione getPathFromUri che permette di ottenere il path.

    private fun getPathFromUri(contentUri: Uri): String? {
        var filePath:String? = null
        var cursor = requireActivity().contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null){
            filePath = contentUri.path

        }else{
            cursor.moveToFirst()
            var index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    /*
    Si esegue l'override della funzione onActivityResult per recuperare tutti i dati che vengono eseguiti
    dall'Activity.
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK){

            if (data != null){
                var selectedImageUrl = data.data
                if(selectedImageUrl != null){
                    try{
                        var inputStream = requireActivity().contentResolver.openInputStream(selectedImageUrl)
                        var bitmap = BitmapFactory.decodeStream(inputStream)
                        imgNote.setImageBitmap(bitmap)
                        imgNote.visibility = View.VISIBLE
                        layoutImage.visibility = View.VISIBLE

                        selectedImagePath = getPathFromUri(selectedImageUrl)!!


                    }catch(e:Exception){
                        Toast.makeText(requireContext(),e.message,Toast.LENGTH_SHORT).show()
                    }


                }
            }

        }
    }
    /*
    /Gestione dei permessi. Override della funzione onRequestPermissionsResult.
    Il sistema passa la risposta dell'utente alla finestra di dialogo dei permessi.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, requireActivity())
    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(), perms)){
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {

    }

    override fun onRationaleDenied(requestCode: Int) {

    }
}