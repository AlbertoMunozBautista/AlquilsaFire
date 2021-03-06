package com.sheilalberto.alquilsafire.ui.miUbicacion


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.sheilalberto.alquilsafire.R
import com.sheilalberto.alquilsafire.clases.Ubicacion
import java.util.*

class AnadirMiUbicacionFragment(
    private val fabMiUbicacionAnadir: FloatingActionButton
) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var idUbicacion = ""
    private var idUsuario = ""
    //private var latitud = ""
    //private var longitud = ""
    private var nombre = ""

    //Variables Mapa
    private lateinit var mMap: GoogleMap
    private var mPosicion: FusedLocationProviderClient? = null
    private var marcadorTouch: Marker? = null
    private var localizacion: Location? = null
    private var posicion: LatLng? = null
    private var PERMISOS: Boolean = true

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_anadir_mi_ubicacion, container, false)

        //rescatamos la id del usuario que ha iniciado sesi??n y generamos una id para la ubicaci??n
        val prefs = activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        idUsuario = prefs?.getString("idUsuario", "null").toString()
        idUbicacion = UUID.randomUUID().toString()

        //almacenamos la referencia al tabla ubicaciones

        val btnAnadirMiUbicacionAnadir : Button = root.findViewById(R.id.btnAnadirMiUbicacionAnadir)
        val btnAnadirMiUbicacionCancelar : Button = root.findViewById(R.id.btnAnadirMiUbicacionCancelar)
        val etAnadirMiUbicacionNombre : EditText = root.findViewById(R.id.etAnadirMiUbicacionNombre)

        //cuando pulsamos en el bot??n a??adir
        btnAnadirMiUbicacionAnadir.setOnClickListener{

            //si el nombre de la ubicaci??n no est?? vac??o
            nombre = etAnadirMiUbicacionNombre.text.toString()
            if(nombre.isEmpty()){
                Toast.makeText(requireActivity(), "El nombre de la ubicaci??n no puede estar vac??o", Toast.LENGTH_SHORT).show()
            }else {
                //se guarda la ubicaci??n en la base de datos
                val u = Ubicacion(
                    idUbicacion,
                    idUsuario,
                    nombre,
                    posicion?.latitude.toString(),
                    posicion?.longitude.toString()
                )
                db.collection("ubicaciones").document(idUbicacion).set(u)

            }



        }

        //Cuando pulsamos en cancelar
        btnAnadirMiUbicacionCancelar.setOnClickListener{

            //volvemos a la lista de ubicaciones
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.anadirMiUbicacion_fragment, MiUbicacionFragment())
            transaction.addToBackStack(null)
            transaction.commit()
            Log.e("FRAGMENT", "PULSO ")

        }

        leerPoscionGPSActual()
        initMapa()

        return root
    }

    /**
     * Leemos la posici??n actual del GPS
     */
    private fun leerPoscionGPSActual() {
        mPosicion = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    //Inicialiazamos el mapa
    private fun initMapa() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapaAnadirMiUbicacion) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarIUMapa()
        cargarMapa()
    }

    /**
     * Configuraci??n del mapa
     */
    private fun configurarIUMapa() {
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val uiSettings: UiSettings = mMap.uiSettings
        //gestos
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        //br??jula
        uiSettings.isCompassEnabled = true
        //zoom
        uiSettings.isZoomControlsEnabled = true
        //barra de herramientas
        uiSettings.isMapToolbarEnabled = true
        //zoom por defecto
        mMap.setMinZoomPreference(14.0f)
        mMap.setOnMarkerClickListener(this)
    }

    /**
     * Cuando pulsemos en mapa se nos crea un marcador
     */
    private fun activarEventosMarcadores() {
        mMap.setOnMapClickListener { point -> // Creamos el marcador
            // Borramos el marcador Touch si est?? puesto
            marcadorTouch?.remove()
            marcadorTouch = mMap.addMarker(
                MarkerOptions() // Posici??n
                    .position(point) // T??tulo
                    .title("Posici??n Actual") // t??tulo
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
            posicion = point
        }
    }

    /**
     * Cargamos el mapa
     */
    @SuppressLint("MissingPermission")
    private fun cargarMapa() {
        if (this.PERMISOS) {
            mMap.isMyLocationEnabled = true
        }
        activarEventosMarcadores()
        obtenerPosicion()
    }



    /**
     * Obtiene la posici??n actual para pasarsela al mapa y que se cargue en nuestra posici??n
     */
    private fun obtenerPosicion() {
        try {
            //Si tenemos permiso cogemos la localizaci??n
            if (this.PERMISOS) {
                val local: Task<Location> = mPosicion!!.lastLocation
                local.addOnCompleteListener(
                    requireActivity()
                ) { task ->
                    if (task.isSuccessful) {
                        localizacion = task.result
                        posicion = LatLng(
                            localizacion!!.latitude,
                            localizacion!!.longitude
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
                    } else {
                        Log.i("GPS", "No se encuetra la ??ltima posici??n.")
                    }
                }
            }
        } catch (e: SecurityException) {
            Snackbar.make(
                requireView(),
                "No se ha encontrado su posoci??n actual o el GPS est?? desactivado",
                Snackbar.LENGTH_LONG
            ).show();
            Log.e("Exception: %s", e.message.toString())
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        return false
    }



}