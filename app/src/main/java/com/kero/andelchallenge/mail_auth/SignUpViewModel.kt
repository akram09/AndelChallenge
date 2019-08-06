package com.kero.andelchallenge.mail_auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kero.andelchallenge.main.UserType
import com.kero.andelchallenge.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class None
data class User(val uid:String ,val fName: String , val mail:String  )
data class SignUpState(val state:Event<Async<UserType>> =Event(Uninitialized)): State
class SignUpViewModel :BaseViewModel<SignUpState>(SignUpState()){
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseAuth =FirebaseAuth.getInstance()
    fun signIn(fName:String ,email:String , password:String){
        setState { copy(Event(Loading())) }
        scope.launch {
            Log.e("errr", email)
            Log.e("errr", password)
            val uid= suspendCoroutine<String?> {
                continuation ->
                firebaseAuth.createUserWithEmailAndPassword(email , password).addOnCompleteListener {
                    if(it.isSuccessful){
                        continuation.resume(it.result?.user?.uid)
                    }else{
                        Log.e("errr", it?.exception?.localizedMessage)
                        firebaseAuth.signInWithEmailAndPassword(email , password).addOnSuccessListener {
                            auth ->
                            continuation.resume(auth.user.uid)
                        }
                    }
                }
            }
            if (uid==null){
                setState {
                    copy(Event(Fail(Failure.AuthFailure.UknownwFailure)))
                }
            }else{
                val userType = scope.getUserType()
                if(userType is Success){
                   val userType =  userType()
                    if(userType==UserType.ADMIN){
                        Log.e("errr", "its admin ")
                        setState {
                            copy(Event(Success(userType)))
                        }
                    }
                }
                firebaseDatabase.reference.child("users").push().setValue(User(uid , fName , email)).addOnCompleteListener {
                    if(it.isSuccessful){
                        setState {
                            copy(Event(Success(UserType.SIMPLE_USER)))
                        }
                    }else{
                        Log.e("errr", it?.exception?.localizedMessage)
                        setState {
                            copy(Event(Fail(Failure.AuthFailure.UknownwFailure)))
                        }
                    }
                }
            }


        }

    }
    private suspend fun DatabaseReference.containId(id:String):Either<Failure.AuthFailure , Boolean > = suspendCoroutine {
        addListenerForSingleValueEvent(object : ValueEventListener {
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
    private suspend  fun checkIfUser(id:String ):Either<Failure.AuthFailure , Boolean > =
        firebaseDatabase.reference.child("users").containId(id)

    private suspend fun checkIfAdmin(id:String):Either<Failure.AuthFailure , Boolean > =
        firebaseDatabase.reference.child("admin").containId(id)
}