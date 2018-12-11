## Index issue with CBL 2.1

Creating an index over a nested property can cause an existing query to break, as a selected property will now return a blob instead of a dictionary.

### Steps to reproduce

* Run app
* Create some documents
* Tap "Create ok index". Note that query still runs successfully (can push "Update" to re-query).
* Tap "Delete indexes".
* Tap "Create problem index". Note that same query breaks immediately as it now returns a blob.
* Deleting index restores working app.

### Explanation

The documents being stored are of the form:

```
{ "counter": 1,
  "nested": { "value": 2 }
}
```

The query remains the same, selecting document id, `counter`, and `nested` properties.

Creating an index over `counter` property works fine. Creating an index that includes `nested` changes the form of the result returned via the query which breaks the previously working code.

```
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
```

### Credit

Thanks to Valery Miskevich for quickly coding up this sample.
