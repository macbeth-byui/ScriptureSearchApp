package macbeth.scripturesearchapp

import android.annotation.SuppressLint
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResultsAdapter : RecyclerView.Adapter<ResultsViewHolder>() {

    private lateinit var view : View
    private var results = SearchResult()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        view = LayoutInflater.
            from(parent.context).
            inflate(R.layout.rv_verse, parent, false)
        return ResultsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        val regex = Regex(results.searchTerm, RegexOption.IGNORE_CASE)
        val text = results.data[position].text.replace(regex)
            { x -> "<font color=\"red\">${x.value}</font>" }
        val html = "<b>" + results.data[position].reference + "</b>: " + text
        holder.verse.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    }

    override fun getItemCount(): Int {
        return results.data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun set(results : SearchResult) {
        this.results = results
        notifyDataSetChanged()
    }
}

class ResultsViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val verse: TextView = itemView.findViewById(R.id.tv_verse)
}