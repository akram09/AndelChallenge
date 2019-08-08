package com.kero.andelchallenge.main

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kero.andelchallenge.utils.*
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class UserType{
    NON_AUth , ADMIN , SIMPLE_USER
}
data class ViewState(val authState : Async<UserType> = Uninitialized):State
class AuthViewModel:BaseViewModel<ViewState>(ViewState()) {

    private val firebaseAuth =FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    init {

     updateAuthState()
    }
    private fun updateAuthState(){
        scope.launch (Dispatchers.Main){
            setState {
                copy(Loading())
            }
            val userState = getUserType()
            Log.e("errr", userState.toString())
            setState {
                copy(userState)
            }
        }
    }
    fun signInCredential(credential:AuthCredential) {
        scope.launch {
            credentialSignIn(credential)
            updateAuthState()
        }
    }
    private suspend fun credentialSignIn(credential: AuthCredential) = suspendCoroutine<Unit>{
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("errr", "signInWithCredential:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("errr", "signInWithCredential:failure", task.exception)
                }
                it.resume(Unit)
            }
    }
    private suspend fun CoroutineScope.getUserType() :Async<UserType> {
        val user = firebaseAuth.currentUser
        user?.apply {
            val isAdmin = async { checkIfAdmin(user.uid)}
            val isUser = async { checkIfUser(user.uid) }
            val eitherIsAdmin  = isAdmin.await()
            val eitherIsUser = isUser.await()
            if(eitherIsAdmin.isRight && eitherIsUser.isRight){
                // we can add a condition to check that he is not a user and an admin in the same time
                if((eitherIsAdmin as Either.Right<Boolean>).b){
                    return  Success(UserType.ADMIN)
                }else if((eitherIsUser as Either.Right<Boolean>).b){
                    return  Success(UserType.SIMPLE_USER)
                }
            }
            return Fail(Failure.AuthFailure.UknownwFailure)
        }
        return Success(UserType.NON_AUth)
    }

    /**
     * this function will check if the given userId is admin or a simple user
     */
    private suspend fun checkIfAdmin(id:String):Either<Failure.AuthFailure , Boolean > =
        firebaseDatabase.reference.child("admins").containId(id)

    private suspend fun DatabaseReference.containId(id:String):Either<Failure.AuthFailure , Boolean > = suspendCoroutine {
       addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                // handle other exceptions here
                it.resume(Either.Left(Failure.AuthFailure.UknownwFailure))
            }

            override fun onDataChange(p0: DataSnapshot) {
                var found =false
                for (data in p0.children){
                    val userId = data.child("uid").getValue(String::class.java)
                    if(userId==id){
                        found = true
                        break
                    }
                }
                it.resume(Either.Right(found))
            }
        })
    }

    private suspend  fun checkIfUser(id:String ):Either<Failure.AuthFailure , Boolean > =
            firebaseDatabase.reference.child("users").containId(id)
}