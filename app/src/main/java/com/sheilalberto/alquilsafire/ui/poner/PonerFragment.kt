package com.sheilalberto.alquilsafire.ui.poner

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.sheilalberto.alquilsafire.R
import com.sheilalberto.alquilsafire.clases.Coche


class PonerFragment : Fragment() {

    private lateinit var fabPonerAnadir: FloatingActionButton

    private var listaCoches = mutableListOf<Coche>() //Lista de coches
    private lateinit var cocheAdapter: CocheListAdapter //Adaptador de coches
    //private lateinit var tareaCoche: PonerFragment.TareaCargarCoches // Tarea hilo para cargar coches
    private var paintSweep = Paint()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var recycler : RecyclerView


    private var idUsuario = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_poner, container, false)

        fabPonerAnadir = root.findViewById(R.id.fabPonerAnadir)

        recycler = root.findViewById(R.id.cocheRecycler)

        recycler.layoutManager = LinearLayoutManager(context)

        //detecta cuando pulsamos en un item
        cocheAdapter = CocheListAdapter(listaCoches) {
            eventoClicFila(it)
        }

        rellenarArrayCoche()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Cuando hacemos click en el fab button nos lleva al fragment de añadir coches
        fabPonerAnadir.setOnClickListener {

            fabPonerAnadir.hide()
            //Cargamos el fragment de añadir coches
            val anadirCoche = AnadirCocheFragment(fabPonerAnadir)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.poner_layout, anadirCoche)
            transaction.addToBackStack(null)
            transaction.commit()

        }

        //iniciarSwipeRecarga()
        //iniciarSwipeHorizontal()
        //cargarCoches()
        Log.e("CARGAR", "CARGADAAAAS")
        //visualizarListaItems()

    }



    /**
     * Rellenamos la lista con los coches que posteriormente cargaremos en el recycler
     */
    private fun rellenarArrayCoche(){

        var lista = mutableListOf<Coche>() //Lista de coches

        val prefs = requireActivity().getSharedPreferences(
            getString(R.string.prefs_file),
            Context.MODE_PRIVATE
        )

        //Recogemos el id del usuario qu ha iniciado sesión
        idUsuario = prefs?.getString("idUsuario", "null").toString()
        Log.e("NOMBRE", "ID" + idUsuario.toString())

        db.collection("coches")
            .get()
            .addOnSuccessListener { result ->
                for (ubicacion in result) {
                    if (ubicacion.get("idUsuario").toString() == idUsuario){
                        val nombre = ubicacion.get("nombre").toString()
                        val asientos = ubicacion.get("asientos").toString()
                        val autonomia = ubicacion.get("autonomia").toString()
                        val combustible = ubicacion.get("combustible").toString()
                        val disponible = ubicacion.get("disponible").toString().toBoolean()
                        val foto = ubicacion.get("foto").toString()
                        val idCoche = ubicacion.get("idCoche").toString()
                        val idUbicacion = ubicacion.get("idUbicacion").toString()
                        val idU = ubicacion.get("idUsuario").toString()
                        val likes = ubicacion.get("likes").toString().toInt()
                        val matricula = ubicacion.get("matricula").toString()
                        val precio = ubicacion.get("precio").toString().toFloat()
                        val tipo = ubicacion.get("tipo").toString()
                        val transmision = ubicacion.get("transmision").toString()

                        val c = Coche(idCoche, matricula, idU, foto, nombre, likes, idUbicacion,
                            autonomia, combustible, transmision, asientos, tipo,
                            precio, disponible)

                        listaCoches.add(c)
                    }


                }

                recycler.adapter = cocheAdapter

            }

    }

    /**
     * Se llama cuando hacemos clic en un item
     */
    private fun eventoClicFila(coche: Coche) {
        //abrirCoche(coche)
    }


}

