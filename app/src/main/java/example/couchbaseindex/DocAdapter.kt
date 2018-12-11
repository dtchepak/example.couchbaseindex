package example.couchbaseindex

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_doc.view.*

class DocAdapter(private val docs: List<Sample>) : RecyclerView.Adapter<DocAdapter.ViewHolder>() {

    var listener: Listener? = null

    override fun getItemCount(): Int = docs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_doc, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = docs[position]
        holder.counterView.text = doc.counter.toString()
        holder.nestedValueView.text = doc.nested?.value.toString()

        holder.counterView.setOnClickListener { listener?.onCounterClick(doc.id) }
        holder.nestedValueView.setOnClickListener { listener?.onNestedValueClick(doc.id) }
        holder.itemView.setOnLongClickListener { listener?.onLongClick(doc.id); true }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val counterView by lazy {
            itemView.textViewCounter
        }

        val nestedValueView by lazy {
            itemView.textViewNestedValue
        }
    }

    interface Listener {
        fun onCounterClick(id: String)
        fun onNestedValueClick(id: String)
        fun onLongClick(id: String)
    }
}
