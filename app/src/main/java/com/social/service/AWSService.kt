package com.social.service

import android.content.Context
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider
import com.amazonaws.mobile.config.AWSConfiguration

class AWSService(context: Context) {
    val configuration = AWSConfiguration(context)
    val identityManager : IdentityManager
    val credentialsProvider : AWSCredentialsProvider

    init {
        identityManager = IdentityManager(context, configuration)
        identityManager.addSignInProvider(CognitoUserPoolsSignInProvider::class.java)
        IdentityManager.setDefaultIdentityManager(identityManager)
        credentialsProvider = identityManager.credentialsProvider
    }
}