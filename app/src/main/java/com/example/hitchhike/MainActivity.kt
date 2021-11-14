package com.example.hitchhike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

//    private lateinit var analytics: FirebaseAnalytics
    private val radioButtonDriver: RadioButton by lazy { findViewById(R.id.radioBtnDriver) }
    private val radioButtonRider: RadioButton by lazy { findViewById(R.id.radioBtnRider) }
    private var userType: String? = null
    private val fromLocation: EditText by lazy { findViewById(R.id.editTextFrom) }
    private val toLocation: EditText by lazy { findViewById(R.id.editTextTo) }
    private lateinit var dbReference: DatabaseReference
    private lateinit var tripRecyclerView: RecyclerView
    private lateinit var tripArrayList: ArrayList<TripsInfo>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tripRecyclerView = findViewById(R.id.tripList)
        tripRecyclerView.layoutManager = LinearLayoutManager(this)
        tripRecyclerView.setHasFixedSize(true)

        tripArrayList = arrayListOf<TripsInfo>()
        dbReference = FirebaseDatabase.getInstance().getReference("Trips")
        getTripData()

        val submitButton = findViewById<Button>(R.id.btnFilter)
        submitButton.setOnClickListener {
            if (fromLocation.text.isEmpty()){
                fromLocation.error = "Required"
                return@setOnClickListener
            } else if (toLocation.text.isEmpty()){
                toLocation.error = "Required"
                return@setOnClickListener
            } else if (userType.isNullOrEmpty()){
                Toast.makeText(this, "Please select 'Looking For'", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                getTripData()
            }
        }
        val clearFilterBtn = findViewById<Button>(R.id.btnClearFilter)
        clearFilterBtn.setOnClickListener {
            fromLocation.text = null
            toLocation.text = null
            radioButtonDriver.isChecked = false
            radioButtonRider.isChecked = false
            userType = null
            getTripData()
        }

        val addTripButton = findViewById<Button>(R.id.btnAddTrip)
        addTripButton.setOnClickListener {
            val intent = Intent(this, AddTrip::class.java).apply {
            }
            startActivity(intent)
        }
    }

    private fun getTripData() {
        dbReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for(tripSnapshot in snapshot.children){
                        val trip = tripSnapshot.getValue(TripsInfo::class.java)
                        if(fromLocation.text.isEmpty()  && toLocation.text.isEmpty() && userType.isNullOrEmpty()) {
                            tripArrayList.add(trip!!)
                        } else if (trip != null) {
                            if (trip.from.equals(fromLocation.text.toString()) && trip.to.equals(toLocation.text.toString()) && trip.userType.equals(userType)){
                                tripArrayList.clear()
                                tripArrayList.add(trip!!)
                            }
                        }
                    }
                    tripRecyclerView.adapter = MyAdapter(tripArrayList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun onRadioButtonClicked(view: View){
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                radioButtonDriver.id ->
                    if (checked) {
                        radioButtonRider.isChecked = false
                        userType = "Driver"
                    }
                radioButtonRider.id ->
                    if (checked) {
                        radioButtonDriver.isChecked = false
                        userType = "Rider"
                    }
            }
        }
    }
}