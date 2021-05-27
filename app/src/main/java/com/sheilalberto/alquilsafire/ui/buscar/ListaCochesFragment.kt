package com.sheilalberto.alquilsafire.ui.buscar

import android.graphics.Paint
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.sheilalberto.alquilsafire.CirculoTransformacion
import com.sheilalberto.alquilsafire.R
import com.sheilalberto.alquilsafire.clases.Coche
import com.sheilalberto.alquilsafire.ui.poner.CocheListAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_lista2_coches.*

class ListaCochesFragment(
    private val fabBuscarFiltro: FloatingActionButton,
    private val idUbicacion : String,
    listaIdsCoches: MutableList<String> = mutableListOf<String>()
) : Fragment() {

    private lateinit var recycler : RecyclerView
    private var listaCochesFiltro = listaIdsCoches
    private var fabBoton = fabBuscarFiltro
    private var idUbica = idUbicacion

    private lateinit var imgListaCoche : ImageView
    private lateinit var tvListaNombre : TextView
    private lateinit var tvListaEmail : TextView
    private lateinit var tvListaTelefono : TextView

    private var listaCoches = mutableListOf<Coche>() //Lista de coches
    private lateinit var cocheAdapter: CocheListAdapter //Adaptador de coches
    //private lateinit var tareaCoche: ListaCochesFragment.TareaCargarCoches // Tarea hilo para cargar coches
    private var paintSweep = Paint()

    private val db = FirebaseFirestore.getInstance()
    private lateinit var databaseUsuariosReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_lista2_coches, container, false)

        imgListaCoche = root.findViewById(R.id.ivListaCoche)
        tvListaNombre = root.findViewById(R.id.tvListaNombreUsu)
        tvListaEmail = root.findViewById(R.id.tvListaEmail)
        tvListaTelefono = root.findViewById(R.id.tvListaTelefono)

        recycler = root.findViewById(R.id.cochesListaRecycler)

        recycler.layoutManager = LinearLayoutManager(context)

        //detecta cuando pulsamos en un item
        cocheAdapter = CocheListAdapter(listaCoches) {
            eventoClicFila(it)
        }

        //dependiendo de si venimos del filtro o no, nos iremos a una opción u a otra
        if (listaCochesFiltro.size == 0){
            rellenarArrayCoche()
        } else {
            rellenarArrayFiltro()
        }


        fabBoton.hide()


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //cochesListaRecycler.layoutManager = LinearLayoutManager(context)


        //iniciarSwipeRecarga()
        //iniciarSwipeHorizontal()
        //cargarCoches()
        Log.e("CARGAR", "CARGADAAAAS")
        //visualizarListaItems()

    }



    /**
     * Función que devuelve la lista de coches que queremos mostrar en el recycler
     */
    private fun rellenarArrayCoche(){

        var idUsuario = ""
        var lista = mutableListOf<Coche>() //Lista de sitios

        db.collection("coches")
            .get()
            .addOnSuccessListener { result ->
                for (coche in result) {

                    if (coche.get("idUbicacion").toString().equals(idUbicacion)){
                        val nombre = coche.get("nombre").toString()
                        val asientos = coche.get("asientos").toString()
                        val autonomia = coche.get("autonomia").toString()
                        val combustible = coche.get("combustible").toString()
                        val disponible = coche.get("disponible").toString().toBoolean()
                        val foto = coche.get("foto").toString()
                        val idCoche = coche.get("idCoche").toString()
                        val idUbicacion = coche.get("idUbicacion").toString()
                        val idU = coche.get("idUsuario").toString()
                        val likes = coche.get("likes").toString().toInt()
                        val matricula = coche.get("matricula").toString()
                        val precio = coche.get("precio").toString().toFloat()
                        val tipo = coche.get("tipo").toString()
                        val transmision = coche.get("transmision").toString()

                        idUsuario = idU
                        val c = Coche(idCoche, matricula, idU, foto, nombre, likes, idUbicacion,
                            autonomia, combustible, transmision, asientos, tipo,
                            precio, disponible)

                        listaCoches.add(c)
                    }
                }

                db.collection("usuarios")
                    .get()
                    .addOnSuccessListener { result2 ->
                        for (usuario in result2) {

                            if (usuario.get("idUsuario").toString().equals(idUsuario)){

                                val nombre = usuario.get("nombre").toString()
                                val foto = usuario.get("foto").toString()
                                val email = usuario.get("email").toString()
                                val telefono = usuario.get("telefono").toString()

                                //Cargamos al usuario en la parte superior de la pantalla
                                tvListaNombre.text = nombre
                                tvListaEmail.text = email
                                tvListaTelefono.text = telefono

                                Picasso.get()
                                    // .load(R.drawable.user_avatar)
                                    .load(Uri.parse(foto))
                                    .transform(CirculoTransformacion())
                                    .resize(178, 178)
                                    .into(imgListaCoche)

                            }
                        }

                        recycler.adapter = cocheAdapter
                    }

            }


    }

    /**
     * Función que devuelve los coches que queremos mostrar en el recycler si viene del filtro
     */
    private fun rellenarArrayFiltro() {

        var idUsuario = ""
        var lista = mutableListOf<Coche>() //Lista de coches


        db.collection("coches")
            .get()
            .addOnSuccessListener { result ->
                for (coche in result) {
                    for (i in 0..listaCochesFiltro.size -1) {
                        if (coche.get("idUbicacion").toString().equals(idUbicacion) &&
                            listaCochesFiltro[i].toString() == coche.get("idCoche").toString()){

                            val nombre = coche.get("nombre").toString()
                            val asientos = coche.get("asientos").toString()
                            val autonomia = coche.get("autonomia").toString()
                            val combustible = coche.get("combustible").toString()
                            val disponible = coche.get("disponible").toString().toBoolean()
                            val foto = coche.get("foto").toString()
                            val idCoche = coche.get("idCoche").toString()
                            val idUbicacion = coche.get("idUbicacion").toString()
                            val idU = coche.get("idUsuario").toString()
                            val likes = coche.get("likes").toString().toInt()
                            val matricula = coche.get("matricula").toString()
                            val precio = coche.get("precio").toString().toFloat()
                            val tipo = coche.get("tipo").toString()
                            val transmision = coche.get("transmision").toString()

                            idUsuario = idU
                            val c = Coche(idCoche, matricula, idU, foto, nombre, likes, idUbicacion,
                                autonomia, combustible, transmision, asientos, tipo,
                                precio, disponible)

                            listaCoches.add(c)
                        }

                    }
                }

                db.collection("usuarios")
                    .get()
                    .addOnSuccessListener { result2 ->
                        for (usuario in result2) {

                            if (usuario.get("idUsuario").toString().equals(idUsuario)){

                                val nombre = usuario.get("nombre").toString()
                                val foto = usuario.get("foto").toString()
                                val email = usuario.get("email").toString()
                                val telefono = usuario.get("telefono").toString()

                                //Cargamos al usuario en la parte superior de la pantalla
                                tvListaNombre.text = nombre
                                tvListaEmail.text = email
                                tvListaTelefono.text = telefono

                                Picasso.get()
                                    // .load(R.drawable.user_avatar)
                                    .load(Uri.parse(foto))
                                    .transform(CirculoTransformacion())
                                    .resize(178, 178)
                                    .into(imgListaCoche)

                            }
                        }

                        recycler.adapter = cocheAdapter
                    }

            }


    }

    /**
     * Se llama cuando hacemos clic en un item
     */
    private fun eventoClicFila(coche: Coche) {
        abrirCoche(coche)
    }

    /**
     * Se llama cuando hemos pulsado un coche, abrimos el fragment detalle
     */
    private fun abrirCoche(coche : Coche){
        //Se oculta el floating button

        //Se llama al detalle fragment
        val cocheDetalle = DetalleCocheFragment(coche, listaCochesFiltro, fabBoton, idUbica)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.listaCochesBuscar, cocheDetalle)
        transaction.addToBackStack(null)
        transaction.commit()

    }


}