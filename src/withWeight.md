```kt
enum class Status(private val identity: String) {
  UNKNOWN("?"), MINE("x"), NORMAL("0");

  companion object {
    fun get(identity: String) = when (identity) {
      "?" -> UNKNOWN
      "x" -> MINE
      else -> NORMAL
    }
  }

  override fun toString(): String {
    return identity
  }
}

data class Position(val row: Int, val column: Int) {
  override fun toString(): String = "$row, $column"
}

class Cell(identity: String, row: Int, column: Int) {
  private var status = Status.get(identity)
  var count: Int = if (isNormal()) identity.toInt() else -1
  var weight = 0.0
  val position = Position(row, column)

  val surroundingCells = mutableListOf<Cell>()
  fun getUnknownCells() = surroundingCells.filter { it.isUnknown() }
  fun getMineCells() = surroundingCells.filter { it.isMine() }

  fun isMine() = status == Status.MINE
  fun isUnknown() = status == Status.UNKNOWN
  fun isNormal() = status == Status.NORMAL

  fun toNormal() {
    this.status = Status.NORMAL
    this.weight = 0.0
    val (row, column) = position
    this.count = Game.open(row, column)
  }

  fun toMine() {
    this.status = Status.MINE
  }

  override fun toString(): String {
    if (!isNormal()) {
      return this.status.toString()
    }
    return count.toString()
  }
}

typealias Board = List<List<Cell>>

val Board.rows: Int
  get() = this.size

val Board.columns: Int
  get() = this[0].size

val DIRECTION = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0, 1 to 1, 1 to -1, -1 to 1, -1 to -1)

fun Board.around(cell: Cell, action: (Cell) -> Unit) {
  DIRECTION.forEach { (X, Y) ->
    val (row, column) = cell.position
    val nextRow = row + X
    val nextColumn = column + Y
    if (nextColumn !in 0 until columns || nextRow !in 0 until rows) {
      return@forEach
    }
    action(this[nextRow][nextColumn])
  }
}

fun Board.walk(action: (Cell) -> Unit) {
  this.forEach { it.forEach(action) }
}

class MineSweeper(board: String, nMines: Int) {
  private val cells: Board
  private var mines = nMines
  private val normalCells = mutableListOf<Cell>()

  init {
    cells = board.lines().map { it.split(" ") }
      .mapIndexed { row, line -> line.mapIndexed { column, item -> Cell(item, row, column) } }

    cells.walk { cell ->
      if (cell.isNormal()) {
        normalCells.add(cell)
      }
      cells.around(cell) { cell.surroundingCells.add(it) }
    }
  }

  private fun success(): String {
    return cells.joinToString("\n") { t -> t.joinToString(" ") { it.toString() } }
  }

  private fun failure(): String = "?"

  private fun getUnknownCells(): List<Cell> {
    val unknownCells = mutableListOf<Cell>()
    cells.walk {
      if (it.isUnknown()) {
        unknownCells.add(it)
      }
    }
    return unknownCells
  }

  private fun compute(): Boolean {
    val removeCells = mutableListOf<Cell>()
    val addCells = mutableListOf<Cell>()
    normalCells.forEach {
      val mineCells = it.getMineCells()
      val unknownCells = it.getUnknownCells()

      if (mineCells.size == it.count) {
        unknownCells.forEach { c -> c.toNormal() }
        addCells.addAll(unknownCells)
        removeCells.add(it)
      } else if (mineCells.size + unknownCells.size == it.count) {
        mines -= unknownCells.size
        unknownCells.forEach { c -> c.toMine() }
        removeCells.add(it)
      }
    }
    if (removeCells.size == 0) {
      return false
    }
    normalCells.addAll(addCells)
    normalCells.removeAll(removeCells)
    return true
  }

  private fun withWeight(): Boolean {
    var maxWeight = 0.0
    normalCells.forEach {
      val unknownCells = it.getUnknownCells()
      val mineCells = it.getMineCells()
      unknownCells.forEach { c ->
        c.weight += (it.count - mineCells.size) / unknownCells.size.toDouble()
        if (c.weight > maxWeight) maxWeight = c.weight
      }
    }

    val unknownCells = getUnknownCells()
    val unknowns = unknownCells.size

    val weightCells = unknownCells.filter { it.weight != 0.0 }
    val weights = weightCells.size
    val maxWeightCells = unknownCells.filter { it.weight == maxWeight }
    val maxWeights = maxWeightCells.size
    val zeroWeightCells = unknownCells.filter { it.weight == 0.0 }
    val zeroWeights = zeroWeightCells.size

    // 所有未定义的cell 权重都相等, 并且大于雷数
    if (unknowns == maxWeights && maxWeights > mines) {
      return false
    }
    if (maxWeights + zeroWeights == mines && maxWeightCells.sumByDouble { it.weight } * mines == weightCells.sumByDouble { it.weight }) {
      return false
    }
    if (weights >= mines && zeroWeights > 0 && zeroWeights <= mines) {
      val a = zeroWeightCells.random()
      a.toNormal()
      normalCells.add(a)
      return true
    }
    if (maxWeights + zeroWeights <= mines) {
      maxWeightCells.forEach { it.toMine() }
      mines -= maxWeights
      return true
    }

    // 可能是雷的 cell 个数 小于等于雷数, 则一定是雷
    if (weights <= mines && zeroWeights == 0) {
      weightCells.forEach { it.toMine() }
      mines -= weights
      return true
    }
//    if (zeroWeights + maxWeights != 0) {
//      return false
//    }

//    val _normalCells = unknownCells.filter { !maxWeightCells.contains(it) }
//
//    _normalCells.forEach { it.toNormal() }
//    normalCells.addAll(_normalCells)
    return true
  }

  fun solve(): String {
    while (mines > 0) {
      if (compute()) {
        println("----")
        println(mines)
        println(success())
        continue
      }
      if (!withWeight()) {
        return failure()
      }
    }
    if (mines < 0) {
      return failure()
    }
    getUnknownCells().forEach { it.toNormal() }
    return success()
  }
}
```
