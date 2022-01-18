package com.ashish.comcastpokemon

data class Resource<out T>(val status: Status, val data: T?, val message: String?, val code: Int?) {
    companion object {
        fun <T> success(data: T, code: Int): Resource<T> {
            return Resource(Status.SUCCESS, data, "success", code)
        }

        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING, null, null, null)
        }

        fun <T> error(message: String, code: Int?): Resource<T> {
            return Resource(Status.ERROR, null, message, code)
        }

        fun <T> empty(): Resource<T> {
            return Resource(Status.EMPTY, null, null, null)
        }
     }
}


enum class Status {
    LOADING,
    SUCCESS,
    ERROR,
    EMPTY
}
