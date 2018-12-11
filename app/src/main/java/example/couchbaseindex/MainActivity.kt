package example.couchbaseindex

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import com.couchbase.lite.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val database by lazy {
        val config = DatabaseConfiguration(this)
        Database("test-index-db", config)
    }

    private val adapterListener = object : DocAdapter.Listener {

        override fun onCounterClick(id: String) {
            with(database.getDocument(id).toMutable()) {
                setInt("counter", getInt("counter") + 1)
                database.save(this)
                update()
            }
        }

        override fun onNestedValueClick(id: String) {
            with(database.getDocument(id).toMutable()) {
                with(getDictionary("nested")) {
                    setInt("value", getInt("value") + 1)
                }
                database.save(this)
                update()
            }
        }

        override fun onLongClick(id: String) {
            AlertDialog.Builder(this@MainActivity)
                .setMessage("Delete this doc?")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    database.getDocument(id)?.let { database.delete(it) }
                    update()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.layoutManager = LinearLayoutManager(this)

        buttonCreateDoc.setOnClickListener { createDoc(); update() }
        buttonCreateProblemIndex.setOnClickListener { createIndexIncludingNested(); update() }
        buttonCreateIndex.setOnClickListener { createIndexWithoutNested(); update() }
        buttonDeleteIndexes.setOnClickListener { deleteIndexes(); update() }
        buttonUpdate.setOnClickListener { update() }

        update()
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }

    fun createDoc() {
        val doc = MutableDocument()
            .setInt("counter", 1).apply {
                setDictionary(
                    "nested", MutableDictionary()
                        .setInt("value", 2)
                )
            }
        database.save(doc)
    }

    fun update() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
        val result = query.execute()
        textView.text =
                "Count: ${result.count()}, indexes: ${database.indexes.size} (${database.indexes.joinToString { it }})"

        readDocs()
    }

    fun readDocs() {
        val query = QueryBuilder.select(
            SelectResult.expression(Meta.id),
            SelectResult.expression(Expression.property("counter")),
            SelectResult.expression(Expression.property("nested"))
        )
            .from(DataSource.database(database))
            .where(
                Expression.property("counter").greaterThan(Expression.value(0)).and(
                    Expression.not(Expression.property("nested").isNullOrMissing)
                )
            )
            .also {
                Log.e("!!!", it.explain()) // <-- log query plan
            }

        textViewError.visibility = GONE

        val docs = query.execute().mapNotNull { doc: Result ->
            val nestedValue = doc.getValue("nested")
            if (nestedValue is Dictionary || nestedValue == null) {
                Sample(
                    doc.getString("id"), doc.getInt("counter"),
                    if (nestedValue != null) Nested(doc.getDictionary("nested").getInt("value")) else null
                )
            } else {
                Log.e("!!!", "'nested' is ${nestedValue::class.java.simpleName}")
                textViewError.text =
                        "Unable to fetch data: 'nested' is ${nestedValue::class.java.simpleName}"
                textViewError.visibility = VISIBLE
                null
            }
        }

        recyclerView.adapter = DocAdapter(docs).apply {
            listener = adapterListener
        }
    }

    fun createIndexWithoutNested() {
        database.createIndex(
            "sampleIndex", IndexBuilder.valueIndex(
                ValueIndexItem.property("counter")
            )
        )
    }

    fun createIndexIncludingNested() {
        database.createIndex(
            "sampleIndex", IndexBuilder.valueIndex(
                ValueIndexItem.property("counter"),
                ValueIndexItem.property("nested")
            )
        )
    }

    fun deleteIndexes() {
        database.indexes.forEach { database.deleteIndex(it) }
    }
}
