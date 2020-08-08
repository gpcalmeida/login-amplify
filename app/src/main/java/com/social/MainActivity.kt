package com.social

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.*
import com.amazonaws.regions.Regions
import com.amplifyframework.auth.AuthProvider
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.Amplify
import com.social.model.User
import com.social.service.AWSService
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private var service : AWSService? = null
    private val mCurrentUser = MutableLiveData<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        service = AWSService(this)

        showFacebookHash()

        Amplify.Auth.fetchAuthSession(
            { result -> Log.i("AmplifyQuickstart", result.toString()) },
            { error -> Log.e("AmplifyQuickstart", error.toString()) }
        )


        sign_up.setOnClickListener {
            Amplify.Auth.signUp(
                username.text.toString(),
                password.text.toString(),
                AuthSignUpOptions.builder().userAttribute(AuthUserAttributeKey.email(), username.text.toString()).build(),
                { result -> Log.i("AmplifySignUp", "Result: $result") },
                { error -> Log.e("AmplifySignUp", "Sign up failed", error) }
            )
        }

        confirm_sign_up.setOnClickListener {
            Amplify.Auth.confirmSignUp(
                username.text.toString(),
                confirm_code.text.toString(),
                { result -> Log.i("AmplifyConfirmSignUp", if (result.isSignUpComplete) "Confirm signUp succeeded" else "Confirm sign up not complete") },
                { error -> Log.e("AmplifyConfirmSignUp", error.toString()) }
            )
        }

        sign_in.setOnClickListener {
            Amplify.Auth.signIn(
                username_login.text.toString(),
                password.text.toString(),
                { result -> Log.i("AmplifySignIn", if (result.isSignInComplete) "Sign in succeeded" else "Sign in not complete") },
                { error -> Log.e("AmplifySignIn", error.toString()) }
            )
        }

        web_ui.setOnClickListener {
            Amplify.Auth.signInWithWebUI(
                this,
                { result -> Log.i("AmplifyWebUi", result.toString()) },
                { error -> Log.e("AmplifyWebUi", error.toString()) }
            )
        }

        sign_out.setOnClickListener {
            Amplify.Auth.signOut(
                { Log.i("AmplifySignOut", "Logged out") },
                { error -> Log.e("AmplifySignOut", error.toString())
                })
        }

        facebook_web_ui.setOnClickListener {
            Amplify.Auth.signInWithSocialWebUI(
                AuthProvider.facebook(),
                this,
                { result -> Log.i("AuthQuickstart", result.toString()) },
                { error -> Log.e("AuthQuickstart", error.toString()) }
            )
        }

        get_user.setOnClickListener {
            Amplify.Auth.fetchAuthSession(
                { result ->
                    val cognitoAuthSession = result as AWSCognitoAuthSession
                    when (cognitoAuthSession.identityId.type) {
                        AuthSessionResult.Type.SUCCESS -> Log.i("AuthQuickStart", "IdentityId: " + cognitoAuthSession.identityId.value)
                        AuthSessionResult.Type.FAILURE -> Log.i("AuthQuickStart", "IdentityId not present because: " + cognitoAuthSession.identityId.error.toString())
                    }
                },
                { error -> Log.e("AuthQuickStart", error.toString()) }
            )
        }



    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if(intent?.scheme != null && "myapp".equals(intent.scheme)) {
            Amplify.Auth.handleWebUISignInResponse(intent)
        }
    }

    @Suppress("all")
    private fun showFacebookHash() {
        try {
            val info: PackageInfo? = this.packageManager?.getPackageInfo(
                "com.social",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info!!.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("Facebook KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
    }
}