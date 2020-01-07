package com.s.eatit.ui.cart

import android.app.AlertDialog
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andremion.counterfab.CounterFab
import com.google.android.gms.location.*
import com.s.eatit.Adapters.MyCartAdapter
import com.s.eatit.Callback.IMyButtonCallBack
import com.s.eatit.Common.Common
import com.s.eatit.Common.MySwipehelper
import com.s.eatit.Database.CartDataSource
import com.s.eatit.Database.CartDatabase
import com.s.eatit.Database.LocalCartDataSource
import com.s.eatit.EventBus.CounterCartEvent
import com.s.eatit.EventBus.HideFABCart
import com.s.eatit.EventBus.UpdateItemCart
import com.s.eatit.R
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_cart.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.lang.StringBuilder
import java.util.*

class CartFragment : Fragment() {

    private lateinit var cartViewModel: CartViewModel
    private var cartDataSource:CartDataSource?=null
    private var compositeDisposable:CompositeDisposable = CompositeDisposable()
    private var recyclerViewState:Parcelable?=null

    var txt_empty_cart : TextView?= null
    var txt_total_price: TextView?= null
    var group_place_holder: CardView?= null
    var recycler_cart:RecyclerView?= null
    var adapter:MyCartAdapter?=null
    var btn_place_order: Button?=null

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallBack: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        EventBus.getDefault().postSticky(HideFABCart(true))


        cartViewModel =
            ViewModelProviders.of(this).get(CartViewModel::class.java)

        //After create cartViewModel , init data source
        cartViewModel.initCartDataSource(context!!)
        val root = inflater.inflate(R.layout.fragment_cart, container, false)

        initViews(root)

        iniLocation()

        cartViewModel.getMutableLiveDataCartItem().observe(this, Observer {

            if (it == null || it.isEmpty())
            {
                recycler_cart!!.visibility = View.GONE
                group_place_holder!!.visibility = View.GONE
                txt_empty_cart!!.visibility = View.VISIBLE
            }
            else
            {
                recycler_cart!!.visibility = View.VISIBLE
                group_place_holder!!.visibility = View.VISIBLE
                txt_empty_cart!!.visibility = View.GONE

                adapter = MyCartAdapter(context!!, it)
                recycler_cart!!.adapter = adapter

            }
        })
//        val textView: TextView = root.findViewById(R.id.text_tools)
//        toolsViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
        return root
    }

    private fun iniLocation() {
        buildLocationRequest()
        buildLocationCallback()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest, locationCallBack,
            Looper.getMainLooper())
    }

    private fun buildLocationCallback() {
        locationCallBack = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)

                currentLocation = p0!!.lastLocation
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(5000)
        locationRequest.setFastestInterval(3000)
        locationRequest.setSmallestDisplacement(10f)

    }

    private fun initViews(root: View?) {

        //important to add menu, if not it will never inflate
        setHasOptionsMenu(true)

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context!!).cartDAO())

        recycler_cart = root!!.findViewById(R.id.recycler_cart) as RecyclerView
        recycler_cart!!.setHasFixedSize(true)

        val layout_manager = LinearLayoutManager(context)
        recycler_cart!!.layoutManager = layout_manager
        recycler_cart!!.addItemDecoration(DividerItemDecoration(context, layout_manager.orientation))


        val swipe = object :MySwipehelper(context!!, recycler_cart!!, 200)
        {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(MyButton(context!!,
                    "Delete",
                    30,
                    0,
                    Color.parseColor("#FF3c30"),
                    object : IMyButtonCallBack{
                        override fun onClick(pos: Int) {
                            val deleteItem = adapter!!.getItemAtPosition(pos)
                            cartDataSource!!.deleteCart(deleteItem)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object :SingleObserver<Int>{
                                    override fun onSuccess(t: Int) {
                                        adapter!!.notifyItemRemoved(pos)
                                        sumCart()
                                        EventBus.getDefault().postSticky(CounterCartEvent(true))
                                        Toast.makeText(context, "Delete item success", Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(context, ""+e.message, Toast.LENGTH_SHORT).show()
                                    }

                                })
                        }

                    }))
            }

        }

        txt_empty_cart = root.findViewById(R.id.txt_empty_cart) as TextView
        txt_total_price = root.findViewById(R.id.txt_total_price) as TextView
        group_place_holder = root.findViewById(R.id.group_place_holder) as CardView
        btn_place_order = root.findViewById(R.id.btn_place_order) as Button

        btn_place_order!!.setOnClickListener {

            val builder = AlertDialog.Builder(context!!)
            builder.setTitle("One More Step!")


            val view = LayoutInflater.from(context).inflate(R.layout.layoout_place_order, null)

            val edt_comment = view.findViewById<View>(R.id.edt_comment) as EditText
            val txt_address = view.findViewById<View>(R.id.txt_address_detail) as TextView
            val edt_Address = view.findViewById<View>(R.id.edt_address) as EditText
            val rdi_home = view.findViewById<View>(R.id.rdi_home_address) as RadioButton
            val rdi_other_address = view.findViewById<View>(R.id.rdi_other_address) as RadioButton
            val rdi_ship_to_other_address = view.findViewById<View>(R.id.rdi_other_address) as RadioButton
            val rdi_cod = view.findViewById<View>(R.id.rdi_cod) as RadioButton
            val brainTree = view.findViewById<View>(R.id.rdi_brantree) as RadioButton

            //Data
            edt_Address.setText(Common.currentUser!!.address!!) //default, we checked rdi_hometherefore we will displayuser's address

            //Event

            rdi_home.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked)
                {
                    edt_Address.setText(Common.currentUser!!.address!!)
                    txt_address.visibility = View.GONE
                }
            }

            rdi_other_address.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked)
                {
                    edt_Address.setText("")
                    edt_Address.setHint("Enter your Address")
                    txt_address.visibility = View.GONE
                }
            }

            rdi_ship_to_other_address.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked)
                {
                    fusedLocationProviderClient!!.lastLocation
                        .addOnFailureListener{ e ->
                            txt_address.visibility = View.VISIBLE
                                Toast.makeText(context, "" + e.message,
                                    Toast.LENGTH_SHORT).show() }
                        .addOnCompleteListener{ task ->
                            val coordinates = StringBuilder()
                                .append(task.result!!.latitude)
                                .append("/")
                                .append(task.result!!.longitude)
                                .toString()

                            val singleAddress = Single.just(getAddressFromLatLong(task.result!!.latitude, task.result!!.longitude))

                            val disposable = singleAddress.subscribeWith(object :DisposableSingleObserver<String>(){
                                override fun onSuccess(t: String) {

                                    edt_Address.setText(coordinates)
                                    txt_address.visibility = View.VISIBLE
                                    txt_address.setText(t)
                                }

                                override fun onError(e: Throwable) {
                                    edt_Address.setText(coordinates)
                                    txt_address.visibility = View.VISIBLE
                                    txt_address.setText(e.message)
                                }

                            })


                        }
                }
            }

            builder.setView(view)
            builder.setNegativeButton("NO", {dialogInterface, _ -> dialogInterface.dismiss()})
                    .setPositiveButton("YES", {dialogInterface, _ -> Toast.makeText(context!!, "Implement later", Toast.LENGTH_SHORT).show()})

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun getAddressFromLatLong(latitude: Double, longitude: Double): String {

        val geoCoder = Geocoder(context!!, Locale.getDefault())
        var result:String?=null

        try {
            val addressList = geoCoder.getFromLocation(latitude, longitude, 1)

            if (addressList != null && addressList.size > 0)
            {
                val address = addressList[0]
                val sb = StringBuilder(address.getAddressLine(0))

                result = sb.toString()
            }
            else
            {

                result = "Address Not Found"
            }

            return result
        } catch (e:IOException)
        {
            return e.message!!
        }

    }

    private fun sumCart() {
        cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :SingleObserver<Double>{
                override fun onSuccess(t: Double) {
                    txt_total_price!!.text = StringBuilder("Total: ")
                        .append(t)
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(context, ""+e.message!!, Toast.LENGTH_SHORT).show()
                }

            })
    }

    override fun onStart() {
        super.onStart()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {


        cartViewModel.onStop()
        compositeDisposable.clear()

        EventBus.getDefault().postSticky(HideFABCart(false))
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

        if (fusedLocationProviderClient != null)
        {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
        }

        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        calculateTotalPrice()
        if (fusedLocationProviderClient != null){
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack,
                Looper.getMainLooper())
        }
    }

    @Subscribe(sticky = true , threadMode = ThreadMode.MAIN)
    fun onUpdateItemInCart(event: UpdateItemCart){

        if (event.cartItem != null)
        {
            recyclerViewState = recycler_cart!!.layoutManager!!.onSaveInstanceState()
            cartDataSource!!.updateCart(event.cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object:SingleObserver<Int>{
                    override fun onSuccess(t: Int) {
                        calculateTotalPrice()
                        recycler_cart!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)

                    }

                    override fun onSubscribe(d: Disposable) {}

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "[UPDATE CART]"+e.message, Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }

    private fun calculateTotalPrice() {
        cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double>{
                override fun onSuccess(price: Double) {

                    txt_total_price!!.text = StringBuilder("Total: ")
                        .append(Common.formatPrice(price))
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                    Toast.makeText(context,"[SUM CART"+e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu!!.findItem(R.id.action_settings).setVisible(false) //Hide setting menu when in cart
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater!!.inflate(R.menu.cart_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item!!.itemId == R.id.action_clear_cart){
            cartDataSource!!.cleanCart(Common.currentUser!!.uid!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object:SingleObserver<Int>{
                    override fun onSuccess(t: Int) {

                        Toast.makeText(context, "Clear Cart Success", Toast.LENGTH_SHORT).show()
                        EventBus.getDefault().postSticky(CounterCartEvent(true))
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, ""+e.message, Toast.LENGTH_SHORT).show()
                    }

                })
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}