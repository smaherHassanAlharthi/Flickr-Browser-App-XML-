package com.example.flickrbrowserappxml

data class Photo(    var id: String? = null,
                      var owner: String? = null,
                      var secret: String? = null,
                      var server: String? = null,
                      var farm: String? = null,
                      var title: String? = null,
                      var checked: Boolean?=null) {
    override fun toString(): String = title!!
}
