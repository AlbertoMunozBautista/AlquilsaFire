package com.sheilalberto.alquilsafire.ui.chat

import android.content.Context
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.sheilalberto.alquilsafire.R
import com.sheilalberto.alquilsafire.clases.Usuario
import kotlinx.android.synthetic.main.fragment_chat.*


class ChatFragment : Fragment() {


    //Creacion de variables
    private var listaUsuarios = mutableListOf<Usuario>() //Lista de sitios
    private lateinit var usuarioAdapter: UsuarioListAdapter //Adaptador de sitios
    private lateinit var tareaUsuario: ChatFragment.TareaCargarUsuarios // Tarea hilo para cargar sitios
    private var paintSweep = Paint()
    private val db = FirebaseFirestore.getInstance()
    private var idUsuario = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_chat, container, false)


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatRecycler.layoutManager = LinearLayoutManager(context)

        //Recargamos el fragment
        iniciarSwipeRecarga()
        //Cargamos los usuarios
        cargarUsuarios()
        Log.e("CARGAR", "CARGADAAAAS")
        //Visualizamos la lista que hayamos recogido de usuarios
        visualizarListaItems()

    }

    /**
     * Se llama cuando hemos pulsado un usuario, abrimos el fragment de chat
     */
    private fun abrirUsuario(usuario : Usuario){
        val lugarDetalle = ConverFragment(usuario)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.chatLayout, lugarDetalle)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Swipe recarga de usuarios
     */
    private fun iniciarSwipeRecarga() {

        usuarioSwipeRefresh.setColorSchemeResources(R.color.principal_oscuro)
        usuarioSwipeRefresh.setProgressBackgroundColorSchemeResource(R.color.secundario)
        usuarioSwipeRefresh.setOnRefreshListener {

            //cargamos los usuarios al refrescar
            cargarUsuarios()
        }
    }

    /**
     * LLamamos al hilo para cargar los usuarios
     */
    private fun cargarUsuarios() {
        tareaUsuario = TareaCargarUsuarios()
        tareaUsuario.execute()
    }


    /*
    Metodo que nos va a rellenar un array con Usuarios
     */

    private fun rellenarArrayUsuarios() : MutableList<Usuario> {

        //Creacion de lista de usuarios
        var lista = mutableListOf<Usuario>() //Lista de sitios

        //Instanciamos la bbdd

        val prefs = requireActivity().getSharedPreferences(
            getString(R.string.prefs_file),
            Context.MODE_PRIVATE
        )

        //Guardamos en las SharedPrferences nuestro idUsuario (con el que nos hemos logueado)
        idUsuario = prefs?.getString("idUsuario", "null").toString()
        Log.e("NOMBRE", "ID" + idUsuario.toString())


        db.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                for(usuario in result){

                    var idU = usuario.get("idUsuario").toString()
                    var nombre = usuario.get("nombre").toString()
                    var email = usuario.get("email").toString()
                    var contra = usuario.get("contra").toString()
                    var foto = usuario.get("foto").toString()
                    var dni = usuario.get("dni").toString()
                    var diaFecha = usuario.get("diaFecha").toString()
                    var mesFecha = usuario.get("mesFecha").toString()
                    var anoFecha = usuario.get("anoFecha").toString()
                    var telefono = usuario.get("telefono").toString()
                    var google = usuario.get("google").toString()

                    val u = Usuario(idU, nombre, email, contra, foto, dni, diaFecha.toInt(), mesFecha.toInt(),
                        anoFecha.toInt(), telefono, google.toString().toBoolean())

                    lista.add(u)

                }

                chatRecycler.adapter = usuarioAdapter


            }


        return lista

    }

    /**
     * Se llama cuando hacemos clic en un item
     */
    private fun eventoClicFila(usuario: Usuario) {
        abrirUsuario(usuario)
    }

    /**
     * Tarea asíncrona para la carga de usuarios
     */
    inner class TareaCargarUsuarios : AsyncTask<Void?, Void?, Void?>() {

        // antes de ejecutar
        override fun onPreExecute() {
            //comprobamos el swipe
            if (usuarioSwipeRefresh.isRefreshing) {
                usuarioSwipeRefresh.isRefreshing = false

            }
        }
        // Tarea
        override fun doInBackground(vararg args: Void?): Void? {
            try {
                listaUsuarios = rellenarArrayUsuarios()

            } catch (e: Exception) {
            }
            return null
        }
        //después de ejecutar
        override fun onPostExecute(args: Void?) {

            //detecta cuando pulsamos en un item
            usuarioAdapter = UsuarioListAdapter(listaUsuarios) {
                eventoClicFila(it)
            }

            chatRecycler.adapter = usuarioAdapter
            // Avismos que ha cambiado
            usuarioAdapter.notifyDataSetChanged()
            chatRecycler.setHasFixedSize(true)

            Toast.makeText(context, "sitios cargados", Toast.LENGTH_LONG).show()
        }

    }

    /**
     * Si paramos cancelamos la tarea
     */
    override fun onStop() {
        super.onStop()
        tareaUsuario.cancel(true)
    }




    private fun visualizarListaItems() {
        usuarioAdapter = UsuarioListAdapter(listaUsuarios) {
            eventoClicFila(it)
        }

        chatRecycler.adapter = usuarioAdapter

    }

}


