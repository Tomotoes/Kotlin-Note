fun cal1(list: List<Int>): Int {
  var res = 0
  for (item in list) {
    res *= item
    res += item
  }
  return res
}

fun cal2(list1: List<Int>): Int {
  fun recurse(list: List<Int>, res: Int): Int {
    if (list.isEmpty()) {
      return res
    }
    return recurse(list.drop(1), res * list.first() + list.first())
  }
  return recurse(list1, 0)
}

fun cal3(list: List<Int>): Int {
  return list.fold(0) { res, item -> res * item + item }
}