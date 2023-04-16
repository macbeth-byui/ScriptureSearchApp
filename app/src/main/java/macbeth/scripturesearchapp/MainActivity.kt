package macbeth.scripturesearchapp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private val library : Library = Library()
    private lateinit var idSearchTerm : SearchView
    private lateinit var idSearchFilter : Spinner
    private lateinit var idResultCount : TextView
    private lateinit var idResults : RecyclerView
    private lateinit var adapter : ResultsAdapter
    private var workerThread : Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        idSearchTerm = findViewById(R.id.tv_search_term)
        idSearchFilter = findViewById(R.id.sp_search_filter)
        idResultCount = findViewById(R.id.tv_result_count)
        idResults = findViewById(R.id.rv_results)

        idResultCount.text = resources.getQuantityString(R.plurals.num_matches,
            0, 0)

        // Setup Results List
        adapter = ResultsAdapter()
        idResults.adapter = adapter
        idResults.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)

        // Setup Spinner
        val spAdapter = ArrayAdapter.createFromResource(this,
            R.array.volumes,
            android.R.layout.simple_spinner_item)
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        idSearchFilter.adapter = spAdapter

        idSearchFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val s = idSearchTerm.query.toString()
                val filter = idSearchFilter.selectedItem.toString()
                search(s, filter)
            }
        }

        // Register for query
        idSearchTerm.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(s: String): Boolean {
                val filter = idSearchFilter.selectedItem.toString()
                search(s, filter)
                return false
            }

            override fun onQueryTextSubmit(s: String): Boolean {
                val filter = idSearchFilter.selectedItem.toString()
                search(s, filter)
                return false
            }
        })

        // Load Library
        library.volumes["Book of Mormon"] = loadChapters(R.raw.bookofmormon)
        library.volumes["New Testament"] = loadChapters(R.raw.newtestament)
        library.volumes["Old Testament"] = loadChapters(R.raw.oldtestament)
        library.volumes["Pearl of Great Price"] = loadChapters(R.raw.pearlofgreatprice)
        library.volumes["Doctrine & Covenants"] = loadSections(R.raw.doctrineandcovenants)
    }

    fun search(s : String, filter : String) {
        // Save the thread.  If another thread gets started, then
        // this thread will exit early and not display any results.
        workerThread = Thread {
            if (s == "") {
                if (workerThread == Thread.currentThread()) {
                    runOnUiThread {
                        idResultCount.text = resources.getQuantityString(
                            R.plurals.num_matches,
                            0, 0
                        )
                        adapter.set(SearchResult())
                    }
                }
                return@Thread
            }
            val newResults = SearchResult()
            newResults.searchTerm = s
            val all = listOf(
                "Book of Mormon", "New Testament", "Old Testament",
                "Doctrine & Covenants", "Pearl of Great Price"
            )
            val volumeList = when (filter) {
                "All Volumes" -> all
                else -> listOf(filter)
            }
            for (volume in volumeList) {
                for (book in library.volumes[volume]!!.books) {
                    for (chapter in book.chapters) {
                        for (verse in chapter.verses!!) {
                            if (workerThread != Thread.currentThread())
                                return@Thread
                            if (verse.text.contains(s, true)) {
                                newResults.add(verse)
                            }
                        }
                    }
                }
            }
            if (workerThread == Thread.currentThread()) {
                runOnUiThread {
                    adapter.set(newResults)
                    idResultCount.text = resources.getQuantityString(R.plurals.num_matches,
                        newResults.data.size, newResults.data.size)

                }
            }
        }
        workerThread?.start()
    }

    private fun loadChapters(fileId : Int) : Volume {
        val stream = resources.openRawResource(fileId).bufferedReader()
        val gson = Gson()
        val volume : Volume = gson.fromJson(stream, Volume::class.java)
        stream.close()
        return volume
    }

    private fun loadSections(fileId : Int) : Volume {
        val stream = resources.openRawResource(fileId).bufferedReader()
        val gson = Gson()
        val book = gson.fromJson(stream, Book::class.java)
        stream.close()
        book.title = "D&C"

        val volume = Volume()
        volume.title = "The Doctrine and Covenants"
        volume.books.add(book)

        return volume

    }
}