package macbeth.scripturesearchapp

import com.google.gson.annotations.SerializedName

data class Library(
    val volumes : MutableMap<String,Volume> = mutableMapOf()
)

data class Volume(
    val books : MutableList<Book> = mutableListOf(),
    var title : String = ""
)

data class Book(
    @SerializedName(value="chapters", alternate = ["sections"])
    var chapters : MutableList<Chapter> = mutableListOf(),

    @SerializedName(value="book", alternate = ["title"])
    var title : String = ""
)

data class Chapter(
    var verses : MutableList<Verse>? = mutableListOf(),

    @SerializedName(value="chapter", alternate = ["section"])
    var chapter : Int = 0
)

data class Verse(
    var reference : String = "",
    var text : String = "",
    var verse : Int = 0
)

class SearchResult {
    var data : MutableList<Verse> = mutableListOf()
    var searchTerm : String = ""

    fun clear() {
        data.clear()
        searchTerm = ""
    }

    fun add(verse : Verse) {
        data.add(verse)
    }
}

