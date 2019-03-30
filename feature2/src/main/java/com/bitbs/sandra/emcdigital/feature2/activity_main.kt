package com.bitbs.sandra.emcdigital.feature2

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class activity_main : AppCompatActivity(), fragment_settings.OnFragmentInteractionListener, fragment_edocta.OnFragmentInteractionListener {
    private val SETTINGS = 4
    private val EDOCTA = 3

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
//                message.setText(R.string.title_payment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                setFragment(EDOCTA)
//                message.setText(R.string.title_edocta)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_student -> {
//                message.setText(R.string.title_settings)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
//                message.setText(R.string.title_settings)
                setFragment(SETTINGS)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (savedInstanceState == null) {
            val fragment = fragment_settings()
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.simpleName)
                .commit()
        }
    }

    //region Fragments
    private fun setFragment(fragmentId: Int)
    {
        val fragment: Fragment

        if(fragmentId == SETTINGS)
        {
            fragment = fragment_settings()

        }
        else if(fragmentId == SETTINGS)
        {
            fragment = fragment_edocta()

        }
        else
        {
            fragment = fragment_edocta()
        }
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.simpleName)
            .commit()
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show()
    }
    //endregion
}
