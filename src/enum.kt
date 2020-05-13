enum class Day {
  MON,
  TUE,
  WEN
}

enum class DayWithVal(val value: Int) {
  MON(1),
  THE(2);

  val nam: String = "xx"
  fun getDay(): Int {
    return value
  }
}

fun main() {
  val day = DayWithVal.THE
  day.getDay()
  day.nam
}
