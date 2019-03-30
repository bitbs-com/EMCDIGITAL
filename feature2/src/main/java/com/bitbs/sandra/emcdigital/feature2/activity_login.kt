package com.bitbs.sandra.emcdigital.feature2

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView

import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.support.annotation.NonNull
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.*


import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.serialization.ImplicitReflectionSerializer
import lisa.bitbs.tools.lisadbprov
import lisa.bitbs.tools.lisawsResult
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception
import java.util.*

/**
 * A activity_login screen that offers activity_login via email/password.
 */
@ImplicitReflectionSerializer
class activity_login : AppCompatActivity(), LoaderCallbacks<Cursor>,
    GoogleApiClient.OnConnectionFailedListener {



    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    /* *//**
     * Keep track of the activity_login task to ensure we can cancel it if requested.
     */

    var user: String = ""
    var pwd: String = ""

    var mAuth: FirebaseAuth? = null
    var currentUser: FirebaseUser? = null

    var mGoogleSignInClient: GoogleSignInClient? = null

    val TAG: String = "GOOGLE_AUTH_ACTIVITY"

    private val RC_SIGN_IN: Int = 9001
    private val MAIN_ACTIVITY:Int = 9002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        //region FIREBASE
        assignFirebaseApp()
        //endregion FIREBASE

        //region GOOGLE
        setGoogleSignIn()
        //endregion GOOGLE

        //region SOLICITUD PERMISOS
        requestPermission_Contacts()
        //endregion SOLICITUD PERMISOS

        //region ASIGNA LISTENERS
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        register_button.setOnClickListener { googleSignInClick() }
        email_sign_in_button.setOnClickListener { attemptLogin() }
        //endregion ASIGNA LISTENERS


        //region LOGIN
        if(isUserLogged()) {
            loadMainActivity()
        }
        else {
            //Nothing Happens
        }
        //endregion LOGIN
    }



//region     PERMISSION REQUEST 

    private fun requestPermission_Contacts(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok,
                    { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) })
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

//endregion    PERMISSION REQUEST

//region    SHARED PREFERENCES 

    private fun getSharedPrefs() {

        val sharedPref:sharedpreferences = sharedpreferences(this)

        user = sharedPref.getValueString("USERNAME").toString()
        pwd = sharedPref.getValueString("PASSWORD").toString()

        if(user != ""
            && user != null)
        {
            if (pwd == null)
            {
                pwd= ""
            }

            email.setText(user)
            password.setText(pwd)
        }

        loaderManager.initLoader(0, null, this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSharedPrefs()
            }
        }
    }

//endregion SHARED PREFERENCES

//region      GOOGLE SIGNIN 

    private fun googleSignInClick() {
        var signInIntent: Intent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun setGoogleSignIn(){


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // Configure Google Sign In
        var gso: GoogleSignInOptions?
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun completeGoogleSignIn(task: Task<GoogleSignInAccount>){
        val sharedPref:sharedpreferences = sharedpreferences(this)
        // Google Sign In was successful, authenticate with Firebase
        var account: GoogleSignInAccount = task.getResult(ApiException::class.java)!!
        firebaseAuthWithGoogle(account)

        //Verifica si la cuenta está expirada
        if(!account.isExpired)
        {
            sharedPref.save("USERNAME", account.email.toString())
            sharedPref.save("PASSWORD", account.idToken.toString())
            sharedPref.save("GMAIL", true)

            loadMainActivity()
        }
        else
        {
            Log.w(TAG, "Account Expired.")
        }

    }
//endregion      GOOGLE SIGNIN

//region     ACTIVITY EVENTS 


   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        //region GOOGLE_SIGN_IN
        if (requestCode == RC_SIGN_IN) {
            var task: Task<GoogleSignInAccount>  = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                completeGoogleSignIn(task)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
       else if (requestCode == MAIN_ACTIVITY) {
            this.finish()
       }
        //endregion GOOGLE_SIGN_IN
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(
            this,
            // Retrieve data rows for the device user's 'profile' contact.
            Uri.withAppendedPath(
                ContactsContract.Profile.CONTENT_URI,
                ContactsContract.Contacts.Data.CONTENT_DIRECTORY
            ), ProfileQuery.PROJECTION,

            // Select only email addresses.
            ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(
                ContactsContract.CommonDataKinds.Email
                    .CONTENT_ITEM_TYPE
            ),

            // Show primary email addresses first. Note that there won't be
            // a primary email address if the user hasn't specified one.
            ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
        )
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }
//endregion     ACTIVITY EVENTS

//region FIREBASE AUTH 

    private fun assignFirebaseApp()
    {
        FirebaseApp.initializeApp(applicationContext)
        //Asigna la instancia de Firebase
        mAuth = FirebaseAuth.getInstance()
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount ) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id)
        // [START_EXCLUDE silent]
//        showProgressDialog();
        // [END_EXCLUDE]

        var credential: AuthCredential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential).addOnCompleteListener(this, OnCompleteListener<AuthResult> {

                    fun onComplete(@NonNull task: Task<AuthResult> ) {
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success")
                            var user: FirebaseUser = mAuth!!.currentUser!!
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.exception)
//                            Snackbar.make(findViewById(R.), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // [START_EXCLUDE]
//                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                })
    }

//endregion      FIREBASE AUTH

//region LOGIN 
    private fun isUserLogged() : Boolean{
        val sharedPref:sharedpreferences = sharedpreferences(this)

        var user = sharedPref.getValueString("USERNAME")
        var pwd = sharedPref.getValueString("PASSWORD")
        var google = sharedPref.getValueBoolean("GMAIL", false)
        //region googleSignIn
        if(google)
        {
            return true
        }
        //endregion googleSignIn

    return false
    }

    private fun attemptLogin() {
        /*if (mAuthTask != null) {
            return
        }*/

        // Reset errors.
        email.error = ""
        password.error = ""

        // Store values at the time of the activity_login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()


        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt activity_login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user activity_login attempt.
            showProgress(true)
            UserLoginTask(emailStr, passwordStr)
        }
    }

    private fun UserLoginTask(nickname: String, password: String){
        try {
                doAsync {
                    var json: lisawsResult = lisadbprov().sp_ERP_SEC_User_Login_Check(
                                    iCompanyId = 1031,
                                    sNickName = nickname,
                                    sPassword = password)


                    uiThread {
                        loginResult(json)
                    }
            }
        } catch (e: Exception) {
        }
    }

    private fun loginResult(json: lisawsResult){
        json.get(0,0,"A")

        val sharedPref:sharedpreferences = sharedpreferences(this)

        sharedPref.save("USERNAME", email.text.toString())
        sharedPref.save("PASSWORD", password.text.toString())

    }

    private fun loadMainActivity()
    {
        var intentMain = Intent(this, activity_main::class.java)
        startActivityForResult(intentMain, MAIN_ACTIVITY)

    }

//endregion LOGIN

//region FIELD VALIDATION 

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(
            this@activity_login,
            android.R.layout.simple_dropdown_item_1line, emailAddressCollection
        )

        email.setAdapter(adapter)
    }


//endregion FIELD VALIDATION

//region PROGRESS BAR 
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            email_login_form.visibility = if (show) View.GONE else View.VISIBLE
            email_login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        email_login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            email_login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

//endregion PROGRESS BAR

//region EXTRAS 
    object ProfileQuery {
        val PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY
        )
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }

    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private val REQUEST_READ_CONTACTS = 0

    }
//endregion EXTRAS
}
