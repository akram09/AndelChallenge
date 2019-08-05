package com.kero.andelchallenge.utils

sealed class Failure {

    sealed class AuthFailure:Failure(){
        object UknownwFailure:AuthFailure()
    }

}