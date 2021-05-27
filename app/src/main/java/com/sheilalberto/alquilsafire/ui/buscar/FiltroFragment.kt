package com.sheilalberto.alquilsafire.ui.buscar

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.sheilalberto.alquilsafire.R
import kotlinx.android.synthetic.main.fragment_filtro.*

class FiltroFragment(
    private val fabBuscarFiltro: FloatingActionButton
) : Fragment(){

    //Lista de las ubicaciones y de los coches que van a ser filtrados
    private var listaIdUbicaciones = mutableListOf<String>()
    private var listaIdCoches = mutableListOf<String>()

    //variables de la base de datos
    private val db = FirebaseFirestore.getInstance()

    //variables strings de las distintas opciones
    private var electricoStr = ""
    private var gasolinaStr = ""
    private var dieselStr = ""
    private var automaticoStr = ""
    private var manualStr = ""
    private var asiento1Str = ""
    private var asiento2Str = ""
    private var asiento3Str = ""
    private var asiento4Str = ""
    private var asiento5Str = ""
    private var asiento5MasStr = ""
    private var motoStr = ""
    private var cocheStr = ""
    private var furgonetaStr = ""
    private var deportivoStr = ""

    //variables boolean de las distintas opciones
    private var electrico = true
    private var gasolina = true
    private var diesel = true
    private var automatica = true
    private var manual = true
    private var asiento1 = true
    private var asiento2 = true
    private var asiento3 = true
    private var asiento4 = true
    private var asiento5 = true
    private var asiento5Mas = true
    private var moto = true
    private var coche = true
    private var furgoneta = true
    private var deportivo = true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_filtro, container, false)

        //rescatamos los componentes del layout
        val btnFiltroCancelar : Button = root.findViewById(R.id.btnFiltroCancelar)
        val btnFiltroBuscar : Button = root.findViewById(R.id.btnFiltroBuscar)
        val btnFiltroElectrico : Button = root.findViewById(R.id.btnFiltroElectrico)
        val btnFiltroGasolina : Button = root.findViewById(R.id.btnFiltroGasolina)
        val btnFiltroDiesel : Button = root.findViewById(R.id.btnFiltroDiesel)
        val btnFiltroAutomatica : Button = root.findViewById(R.id.btnFiltroAutomatica)
        val btnFiltroManual : Button = root.findViewById(R.id.btnFiltroManual)
        val btnFiltroAsiento1 : Button = root.findViewById(R.id.btnFiltroAsiento1)
        val btnFiltroAsiento2 : Button = root.findViewById(R.id.btnFiltroAsiento2)
        val btnFiltroAsiento3 : Button = root.findViewById(R.id.btnFiltroAsiento3)
        val btnFiltroAsiento4 : Button = root.findViewById(R.id.btnFiltroAsiento4)
        val btnFiltroAsiento5 : Button = root.findViewById(R.id.btnFiltroAsiento5)
        val btnFiltroAsiento5Mas : Button = root.findViewById(R.id.btnFiltroAsiento5Mas)
        val btnFiltroMoto : Button = root.findViewById(R.id.btnFiltroMoto)
        val btnFiltroCoche : Button = root.findViewById(R.id.btnFiltroCoche)
        val btnFiltroFurgoneta : Button = root.findViewById(R.id.btnFiltroFurgoneta)
        val btnFiltroDeportivo : Button = root.findViewById(R.id.btnFiltroDeportivo)
        val imgFiltroReset : ImageView = root.findViewById(R.id.imgFiltroReset)


        //llama a la función reset
        imgFiltroReset.setOnClickListener{

            reset()

        }

        //vuelve al fragment buscar
        btnFiltroCancelar.setOnClickListener{

            fabBuscarFiltro.show()
            val buscar = BuscarFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.filtro_layout, buscar)
            transaction.addToBackStack(null)
            transaction.commit()

        }

        //cuando pulsamos el botón buscar
        btnFiltroBuscar.setOnClickListener{

            comprobarCampos()

            db.collection("coches")
                .get()
                .addOnSuccessListener { result ->
                    for (coche in result) {
                        if (coche.get("combustible").toString().equals(electricoStr) ||
                            coche.get("combustible").toString().equals(gasolinaStr) ||
                            coche.get("combustible").toString().equals(dieselStr) ||
                            coche.get("transmision").toString().equals(manualStr) ||
                            coche.get("transmision").toString().equals(automaticoStr) ||
                            coche.get("asientos").toString().equals(asiento1Str) ||
                            coche.get("asientos").toString().equals(asiento2Str) ||
                            coche.get("asientos").toString().equals(asiento3Str) ||
                            coche.get("asientos").toString().equals(asiento4Str) ||
                            coche.get("asientos").toString().equals(asiento5Str) ||
                            coche.get("asientos").toString().equals(asiento5MasStr) ||
                            coche.get("tipo").toString().equals(cocheStr) ||
                            coche.get("tipo").toString().equals(motoStr) ||
                            coche.get("tipo").toString().equals(furgonetaStr) ||
                            coche.get("tipo").toString().equals(deportivoStr)) {

                            val idUbicacion = coche.get("idUbicacion").toString()
                            val idCoche = coche.get("idCoche").toString()
                            Log.e("UBICACION", idUbicacion)
                            listaIdUbicaciones.add(idUbicacion)
                            listaIdCoches.add(idCoche)

                        }

                    }

                    fabBuscarFiltro.show()
                    val editarFragment = BuscarFragment(listaIdUbicaciones, listaIdCoches)
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    transaction.replace(R.id.filtro_layout, editarFragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }



        }

        //llamamos al método cambiar botones, que cambiará el valor boolean del
        //campo que representa el boton al que pulsamos y también cambiará el fondo del botón
        btnFiltroGasolina.setOnClickListener{
            gasolina = cambiarBotones(btnFiltroGasolina, gasolina)
            //Log.e("GASOLINA", gasolina.toString())
        }

        btnFiltroElectrico.setOnClickListener{
            electrico = cambiarBotones(btnFiltroElectrico, electrico)
        }

        btnFiltroDiesel.setOnClickListener{
            diesel = cambiarBotones(btnFiltroDiesel, diesel)
        }

        btnFiltroAutomatica.setOnClickListener{
            automatica = cambiarBotones(btnFiltroAutomatica, automatica)
        }

        btnFiltroManual.setOnClickListener{
            manual = cambiarBotones(btnFiltroManual, manual)
        }

        btnFiltroAsiento1.setOnClickListener{
            asiento1 = cambiarBotones(btnFiltroAsiento1, asiento1)
        }

        btnFiltroAsiento2.setOnClickListener{
            asiento2 = cambiarBotones(btnFiltroAsiento2, asiento2)
        }

        btnFiltroAsiento3.setOnClickListener{
            asiento3 = cambiarBotones(btnFiltroAsiento3, asiento3)
        }

        btnFiltroAsiento4.setOnClickListener{
            asiento4 = cambiarBotones(btnFiltroAsiento4, asiento4)
        }

        btnFiltroAsiento5.setOnClickListener{
            asiento5 = cambiarBotones(btnFiltroAsiento5, asiento5)
        }

        btnFiltroAsiento5Mas.setOnClickListener{
            asiento5Mas = cambiarBotones(btnFiltroAsiento5Mas, asiento5Mas)
        }

        btnFiltroMoto.setOnClickListener{
            moto = cambiarBotones(btnFiltroMoto, moto)
        }

        btnFiltroCoche.setOnClickListener{
            coche = cambiarBotones(btnFiltroCoche, coche)
        }

        btnFiltroFurgoneta.setOnClickListener{
            furgoneta = cambiarBotones(btnFiltroFurgoneta, furgoneta)
        }

        btnFiltroDeportivo.setOnClickListener{
            deportivo = cambiarBotones(btnFiltroDeportivo, deportivo)
        }



        return root
    }

    /**
     * Función que pone todo a true y cambia todos los fondos de los botones
     */
    private fun reset() {
        electrico = true
        gasolina = true
        diesel = true
        automatica = true
        manual = true
        asiento1 = true
        asiento2 = true
        asiento3 = true
        asiento4 = true
        asiento5 = true
        asiento5Mas = true
        moto = true
        coche = true
        furgoneta = true
        deportivo = true

        btnFiltroElectrico.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroGasolina.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroDiesel.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroAutomatica.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroManual.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroAsiento1.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroAsiento2.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroAsiento3.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroAsiento4.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroAsiento5.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroAsiento5Mas.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroCoche.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroFurgoneta.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroMoto.setBackgroundResource(R.drawable.boton_redondo)
        btnFiltroDeportivo.setBackgroundResource(R.drawable.boton_redondo)
    }

    /**
     * Se llama en el botón buscar, una vez seleccionados los valores que queremos buscar se hace
     * una comparación y si está a true, se le da al string el valor verdadero
     */
    private fun comprobarCampos() {

        if (electrico) electricoStr = "Eléctrico"
        if (gasolina) gasolinaStr = "Gasolina"
        if(diesel) dieselStr = "Diesel"
        if (automatica) automaticoStr = "Automático"
        if (manual) manualStr = "Manual"
        if (asiento1) asiento1Str = "1"
        if (asiento2) asiento2Str = "2"
        if (asiento3) asiento3Str = "3"
        if (asiento4) asiento4Str = "4"
        if (asiento5) asiento5Str = "5"
        if (asiento5Mas) asiento5MasStr = "+5"
        if (moto) motoStr = "Moto"
        if (coche) cocheStr = "Coche"
        if(furgoneta) furgonetaStr = "Furgoneta"
        if (deportivo) deportivoStr = "Deportivo"

    }

    /**
     * Recibe un botón y un booleano y lo cambia a false o a true dependiendo del valor, también
     * cambia el fondo
     */
    private fun cambiarBotones(boton: Button, bandera: Boolean): Boolean{
        if(bandera){
            boton.setBackgroundResource(R.drawable.boton_fondo_transparente)
            return false
        } else {
            boton.setBackgroundResource(R.drawable.boton_redondo)
            return true
        }
    }

}