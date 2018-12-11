package example.couchbaseindex

data class Sample(val id: String, val counter: Int, val nested: Nested?)

data class Nested(val value: Int)
