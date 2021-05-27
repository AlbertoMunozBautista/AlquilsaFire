package com.sheilalberto.alquilsafire.ui.miUbicacion
import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sheilalberto.alquilsafire.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.sheilalberto.alquilsafire.clases.Ubicacion
import kotlinx.android.synthetic.main.fragment_mi_ubicacion.*

class MiUbicacionFragment : Fragment() {

    private lateinit var fabMiUbicacionAnadir: FloatingActionButton

    private lateinit var recycler : RecyclerView

    private var listaUbicaciones = mutableListOf<Ubicacion>() //Lista de ubicaciones
    private lateinit var ubicacionAdapter: UbicacionListAdapter //Adaptador de ubicaciones
    //private lateinit var tareaUbicacion: TareaCargarUbicacion // Tarea hilo para cargar ubicaciones
    private var paintSweep = Paint()
    private var idUsuario = ""
    private val db = FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_mi_ubicacion, container, false)

        fabMiUbicacionAnadir = root.findViewById(R.id.fabMiUbicacionAnadir)

        recycler = root.findViewById(R.id.ubicacionRecycler)


        recycler.layoutManager = LinearLayoutManager(context)

        rellenarArrayUbicacion()

        //detecta cuando pulsamos en un item
        ubicacionAdapter = UbicacionListAdapter(listaUbicaciones) {
            eventoClicFila(it)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fabMiUbicacionAnadir.setOnClickListener {

            fabMiUbicacionAnadir.hide()
            //Cargamos el fragment de añadir ubicaciones
            val anadirUbi = AnadirMiUbicacionFragment(fabMiUbicacionAnadir)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.miUbicacion_layout, anadirUbi)
            transaction.addToBackStack(null)
            transaction.commit()

        }

        //iniciarSwipeRecarga()
        iniciarSwipeHorizontal()
        //cargarUbicaciones()
        Log.e("CARGAR", "CARGADAAAAS")
        //visualizarListaItems()

    }

    /**
     * El método borrar elemento eleminará el elemento de la lista del recycler y
     * llamará a un alert dialog para pedir confirmación para evitar un borrado erroneo
     */
    private fun borrarElemento(position: Int) {
        //Cuando hemos deslizado, quitamos el elemento del swipe y lo ponemos
        //instantaneamente para que desaparezca el color del fondo
        val deleteModel: Ubicacion = listaUbicaciones[position]
        ubicacionAdapter.removeItem(position)
        ubicacionAdapter.restoreItem(deleteModel, position)


        //Alert dialog para confirmar si desea eliminar la ubicación deslizada
        Log.i("Elimar", "Eliminando...")
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.logo_negro)
            .setTitle("Eliminar lugar")
            .setMessage("¿Desea eliminar el sitio seleccionado?")
            .setPositiveButton("Sí"){dialog, which -> eliminarSitioConfirmado(position)}
            .setNegativeButton("No", null)
            .show()

    }

    /**
     * Si en el alert dialog hemos confirmado que sí queremos eliminar la ubicación
     * lo borramos de la base de datos
     */
    private fun eliminarSitioConfirmado(position:Int) {

        //SitiosController.delete(SITIOS[position])
        borrarUbicacion(listaUbicaciones[position])
        val snackbar = Snackbar.make(requireView(), "Ubicación eliminada con éxito", Snackbar.LENGTH_LONG)
        ubicacionAdapter.removeItem(position)
        snackbar.show()
    }

    /**
     * Swipe horizontal que nos servirá para eliminar ya que borramos deslizando
     * a ambos lados
     */
    private fun iniciarSwipeHorizontal() {
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or
                    ItemTouchHelper.RIGHT
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            //Según donde deslizemos
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                //izquierda -> borramos
                //derecha -> borramos
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        borrarElemento(position)
                    }
                    else -> {
                        borrarElemento(position)
                    }
                }
            }




            /**
             * Se crea el dibujo cuando deslizamos
             */
            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3

                    //En ambos casos borramos el la ubicación elegida
                    if (dX > 0) {

                        botonIzquierdo(canvas, dX, itemView, width)
                    } else {

                        botonDerecho(canvas, dX, itemView, width)
                    }
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(ubicacionRecycler)
    }

    //Cuando deslizamos hacia la izquierda aparece un fondo rojo con el botón de eliminar
    private fun botonIzquierdo(canvas: Canvas, dX: Float, itemView: View, width: Float) {

        paintSweep.setColor(Color.RED)
        val background = RectF(
            itemView.left.toFloat(), itemView.top.toFloat(), dX,
            itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_seep_eliminar)
        val iconDest = RectF(
            itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left
                .toFloat() + 2 * width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    /**
     * Cuando deslizamos hacia la izquierda aparece un fondo rojo con el botón de eliminar
     */
    private fun botonDerecho(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        // Pintamos de rojo y ponemos el icono
        paintSweep.color = Color.RED
        val background = RectF(
            itemView.right.toFloat() + dX,
            itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_seep_eliminar)
        val iconDest = RectF(
            itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right
                .toFloat() - width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    /**
     * Método que recibe la ubicación y a través del id la borra de la base de datos
     */
    private fun borrarUbicacion(u: Ubicacion){

        var id = u.idUbicacion
        db.collection("ubicaciones").document(id).delete()

    }


    /**
     * Rellenamos y devolvemos un array de ubicaciones con todas las ubicaciones que queremos
     * cargar en el recycler
     */
    private fun rellenarArrayUbicacion() {

        var lista = mutableListOf<Ubicacion>() //Lista de ubicaciones


        val prefs = requireActivity().getSharedPreferences(
            getString(R.string.prefs_file),
            Context.MODE_PRIVATE
        )

        //rescatamos de las shared preferences al usuario  que está activo
        idUsuario = prefs?.getString("idUsuario", "null").toString()
        Log.e("NOMBRE", "ID" + idUsuario.toString())


        db.collection("ubicaciones")
            .get()
            .addOnSuccessListener { result ->
                for (ubicacion in result) {
                    if (ubicacion.get("idUsuario").toString() == idUsuario){
                        val nombre = ubicacion.get("nombre").toString()
                        val latitud = ubicacion.get("latitud").toString()
                        val longitud = ubicacion.get("longitud").toString()
                        val idU = ubicacion.get("idUsuario").toString()
                        val idUbicacion = ubicacion.get("idUbicacion").toString()

                        val u = Ubicacion(
                            idUbicacion,
                            idU,
                            nombre,
                            latitud,
                            longitud
                        )

                        listaUbicaciones.add(u)
                    }


                }

                recycler.adapter = ubicacionAdapter

            }


    }

    /**
     * Se llama cuando hacemos clic en un item
     */
    private fun eventoClicFila(ubi: Ubicacion) {
        //abrirUbicacion(ubi)
    }


}