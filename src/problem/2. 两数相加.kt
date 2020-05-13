package problem

class ListNode(var `val`: Int) {
  var next: ListNode? = null
}

class Solution {
  fun addTwoNumbers(l1: ListNode?, l2: ListNode?): ListNode? {
    val root = ListNode(-1)
    var cursor = root
    var list1 = l1
    var list2 = l2
    var sub = 0
    while ((list1 != null || list2 != null) || sub == 1) {
      val value = (list1?.`val` ?: 0) + (list2?.`val` ?: 0) + sub
      cursor.next = ListNode(value % 10)
      sub = if (value >= 10) 1 else 0
      cursor = cursor.next!!
      list1 = list1?.next
      list2 = list2?.next
    }
    return root.next
  }
}