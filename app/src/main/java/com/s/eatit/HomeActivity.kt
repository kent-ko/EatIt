package com.s.eatit

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.s.eatit.Common.Common
import com.s.eatit.Database.CartDataSource
import com.s.eatit.Database.CartDatabase
import com.s.eatit.Database.LocalCartDataSource
import com.s.eatit.EventBus.*
import com.s.eatit.Model.CategoryModel
import com.s.eatit.Model.FoodModel
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.internal.operators.single.SingleObserveOn
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_bar_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var cartDataSource: CartDataSource

    private lateinit var navController: NavController
    private lateinit var drawer:DrawerLayout

    private lateinit var dialog: android.app.AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(this).cartDAO())

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.counter_fab)
        fab.setOnClickListener { view ->
            navController.navigate(R.id.nav_cart)
        }

        drawer= findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
         navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_detail,
                R.id.nav_cart, R.id.nav_sign_out
            ), drawer
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        var headerView = navView.getHeaderView(0)
        var txt_user = headerView.findViewById<TextView>(R.id.txt_user)
        Common.setSpanString("Hey", Common.currentUser!!.name!!, txt_user)

        navView.setNavigationItemSelectedListener (object : NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {

                item.isChecked = true
                drawer!!.closeDrawers()

                if (item.itemId == R.id.nav_sign_out)
                {
                    Toast.makeText(this@HomeActivity, "Ok TEst", Toast.LENGTH_SHORT).show()
                    signOut()
                }
                else if (item.itemId == R.id.nav_home)
                {
                    navController.navigate(R.id.nav_home)
                }
                else if (item.itemId == R.id.nav_cart)
                {
                    navController.navigate(R.id.nav_cart)
                }
                else if (item.itemId == R.id.nav_menu)
                {
                    navController.navigate(R.id.nav_menu)
                }

                return true
            }

        })

        countCartItem()
    }

    private fun signOut() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sign Out")
            .setMessage("Do you really want to exit?")
            .setNegativeButton("CANCEL", {dialogInterface, _ -> dialogInterface.dismiss() })
            .setPositiveButton("OK") {dialogInterface, _ ->

                Common.foodSelected = null
                Common.currentUser = null
                Common.categorySelected = null
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()


            }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCategorySelected(event:CategoryClick)
    {
        if(event.isSuccess){

            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_foodlist)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onFoodSelected(event: FoodItemClick)
    {
        if (event.isSucess)
        {
            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_food_detail)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCountCartEvent(event: CounterCartEvent)
    {
        if(event.isSuccess)
        {
            countCartItem()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onHideFABEvent(event: HideFABCart)
    {
        if (event.isHide)
        {
            counter_fab.hide()
        }
        else
        {
            counter_fab.show()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPopularFoodItemClick(event: PopularFoodItemClick)
    {
        if (event.popularCategoryModel != null)
        {

            dialog!!.show()


            FirebaseDatabase.getInstance()
                .getReference("Category")
                .child(event.popularCategoryModel!!.menu_id!!)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        dialog!!.dismiss()
                        Toast.makeText(this@HomeActivity, ""+p0.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        if (p0.exists())
                        {

                            Common.categorySelected = p0.getValue(CategoryModel::class.java)

                            //Load food
                            FirebaseDatabase.getInstance()
                                .getReference("Category")
                                .child(event.popularCategoryModel!!.menu_id!!)
                                .child("foods")
                                .orderByChild("id")
                                .equalTo(event.popularCategoryModel.food_id)
                                .limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onCancelled(p0: DatabaseError) {

                                        dialog!!.dismiss()
                                        Toast.makeText(this@HomeActivity, ""+p0.message, Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {

                                        if (p0.exists())
                                        {

                                            dialog!!.dismiss()
                                            for (foodSnapShot in p0.children)
                                                Common.foodSelected = foodSnapShot.getValue(FoodModel::class.java)
                                            navController!!.navigate(R.id.nav_food_detail)
                                        }
                                        else
                                        {
                                            dialog!!.dismiss()
                                            Toast.makeText(this@HomeActivity, "Item doesn't exist", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                })
                        }
                        else
                        {
                            dialog!!.dismiss()
                            Toast.makeText(this@HomeActivity, "Item doesnt exist", Toast.LENGTH_SHORT).show()
                        }
                    }

                })
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onBestDealFoodItemClick(event: BestDealItemClick)
    {
        if (event.model != null)
        {

            dialog!!.show()


            FirebaseDatabase.getInstance()
                .getReference("Category")
                .child(event.model!!.menu_id!!)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        dialog!!.dismiss()
                        Toast.makeText(this@HomeActivity, ""+p0.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        if (p0.exists())
                        {

                            Common.categorySelected = p0.getValue(CategoryModel::class.java)

                            //Load food
                            FirebaseDatabase.getInstance()
                                .getReference("Category")
                                .child(event.model!!.menu_id!!)
                                .child("foods")
                                .orderByChild("id")
                                .equalTo(event.model!!.food_id)
                                .limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onCancelled(p0: DatabaseError) {

                                        dialog!!.dismiss()
                                        Toast.makeText(this@HomeActivity, ""+p0.message, Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {

                                        if (p0.exists())
                                        {

                                            dialog!!.dismiss()
                                            for (foodSnapShot in p0.children)
                                                Common.foodSelected = foodSnapShot.getValue(FoodModel::class.java)
                                            navController!!.navigate(R.id.nav_food_detail)
                                        }
                                        else
                                        {
                                            dialog!!.dismiss()
                                            Toast.makeText(this@HomeActivity, "Item doesn't exist", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                })
                        }
                        else
                        {
                            dialog!!.dismiss()
                            Toast.makeText(this@HomeActivity, "Item doesnt exist", Toast.LENGTH_SHORT).show()
                        }
                    }

                })
        }
    }
    private fun countCartItem() {

        cartDataSource.countItemInCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int>{
                override fun onSuccess(t: Int) {
                    counter_fab.count = t
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {

                    if (!e.message!!.contains("Query returned empty")) {
                        Toast.makeText(
                            this@HomeActivity,
                            "[COUNT CART]" + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else
                    {
                        counter_fab.count = 0
                    }
                }

            })
    }

    override fun onResume() {
        super.onResume()
        countCartItem()
    }
}
