fun <A, B> Array<A>.corresponds(that: Array<B>, p: (A, B) -> Boolean): Boolean {
  val i = this.iterator()
  val j = that.iterator()

  while (i.hasNext() && j.hasNext()) {
    if (!p(i.next(), j.next())) {
      return false
    }
  }
  return !i.hasNext() && !j.hasNext()
}

fun main() {
  val a = arrayOf(1, 2, 3)
  val b = arrayOf(2, 3, 4)
  val res = a.corresponds(b) { a, b -> a + 1 == b }
  println(res)

  val c = if (a.corresponds(b) { a, b -> a + 1 == b }) "xxx" else 0

}