package com.kero.andelchallenge.mail_auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kero.andelchallenge.utils.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class None
data class User(val uid:String ,val fName: String , val mail:String  )
data class SignUpState(val state:Event<Async<None>> =Event(Uninitialized)): State
class SignUpViewModel :BaseViewModel<SignUpState>(SignUpState()){
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseAuth =FirebaseAuth.getInstance()
    fun signIn(fName:String ,email:String , password:String){
        setState { copy(Event(Loading())) }
        scope.launch {
            val uid= suspendCoroutine<String?> {
                continuation ->
                firebaseAuth.signInWithEmailAndPassword(email , password).addOnCompleteListener {
                    if(it.isSuccessful){
                        continuation.resume(it.result?.user?.uid)
                    }else{
                        continuation.resume(null)
                    }
                }
            }
            if (uid==null){
                setState {
                    copy(Event(Fail(Failure.AuthFailure.UknownwFailure)))
                }
            }else{
                firebaseDatabase.reference.child("users").push().setValue(User(uid , fName , email)).addOnCompleteListener {
                    if(it.isSuccessful){
                        setState {
                            copy(Event(Success(None())))
                        }
                    }else{
                        setState {
                            copy(Event(Fail(Failure.AuthFailure.UknownwFailure)))
                        }
                    }
                }
            }


        }

    }
}