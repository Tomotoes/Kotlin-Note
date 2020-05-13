// codewars 上未完成的题目, 扫雷
// 自己真菜a ...
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
  var inside = true
  val position = Position(row, column)
  var weight = 0
  val surroundingCells = mutableListOf<Cell>()
  fun getUnknownCells() = surroundingCells.filter { it.isUnknown() }
  fun getMineCells() = surroundingCells.filter { it.isMine() }

  fun isMine() = status == Status.MINE
  fun isUnknown() = status == Status.UNKNOWN
  fun isNormal() = status == Status.NORMAL

  fun toNormal() {
    this.status = Status.NORMAL
    weight = 0
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

fun List<Cell>.toPosition(): List<String> {
  return this.map { it.position.toString() }
}

infix fun List<Cell>.sub(that: List<Cell>): List<Cell> {
  val result = mutableListOf<Cell>()
  val p = that.toPosition()
  forEach {
    if (!p.contains(it.position.toString())) {
      result.add(it)
    }
  }
  return result
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

  private fun reason(): Boolean {
    val removeCells = mutableListOf<Cell>()
    val addCells = mutableListOf<Cell>()
    normalCells.forEach {
      val unknownCells = it.getUnknownCells()
      if (unknownCells.isEmpty()) {
        return@forEach
      }
      val mineCells = it.getMineCells()

      // 雷已满, 周围还存在未知, 则未知一定是数
      // 比如 0,周围一定全是数
      if (mineCells.size == it.count) {
        unknownCells.forEach { c -> c.toNormal() }
        addCells.addAll(unknownCells)
        removeCells.add(it)
      }
      // 雷未满, 周围的雷数加未知的数 等于雷数, 则未知一定是雷
      // 比如 2, 周围有一个雷, 还有一个未知, 那么这个未知是雷
      if (mineCells.size + unknownCells.size == it.count) {
        mines -= unknownCells.size
        unknownCells.forEach { c -> c.toMine() }
        removeCells.add(it)
      }
    }
    // 依赖推理的数据未更新, 则表明无法通过雷数继续扫描了
    if (removeCells.isEmpty()) {
      return false
    }
    normalCells.addAll(addCells)
    normalCells.removeAll(removeCells)
    return true
  }

  // 头都大了.... 边界条件太多了
  // 计算模型, 调整调整再调整...
  // 我死了
  private fun exclude(): Boolean {
    println("1. 记录每个依赖推理的节点发言, 即周围可能的雷")
    val unknown1CellsList = mutableListOf<List<Cell>>()
    val unknown2CellsList = mutableListOf<List<Cell>>()
    normalCells.forEach { cell ->
      // println("只针对剩余推理数为 1 的情况, 因为大于 1 的话, 组合数太多 无法提供帮助")
      val number = cell.count - cell.getMineCells().size
      val unknownCells = cell.getUnknownCells()
      unknownCells.forEach { it.inside = false }
      if (number != 1) {
        if (unknownCells.size - number == 1) {
          unknown2CellsList.add(unknownCells)
        }
        return@forEach
      }

      // 1.2 println("查找之前是否已经有相同项")
      val ps = unknownCells.toPosition()
      val containCells = unknown1CellsList.firstOrNull {
        val p = it.toPosition()
        ps.containsAll(p) || p.containsAll(ps)
      }
      // 1.3 println("没有就添加")
      if (containCells.isNullOrEmpty()) {
        unknown1CellsList.add(unknownCells)
        return@forEach
      }
      // 1.4 println("过滤相同项")
      val contains = containCells.size
      val unknowns = unknownCells.size
      if (contains == unknowns) {
        return@forEach
      }
      println("1.5 一定存在严格包含关系, 找到差集, 差集一定是数字")
      val excludedCells = when {
        contains > unknowns -> containCells sub unknownCells
        else -> unknownCells sub containCells
      }
      excludedCells.forEach { it.toNormal() }
      normalCells.addAll(excludedCells)
      return true
    }
    unknown2CellsList.forEach {
      for (list in unknown1CellsList) {
        if (it.size > list.size) {
          if (it.toPosition().containsAll(list.toPosition())) {
            val cells = it sub list
            cells.forEach { c -> c.toMine() }
            mines -= cells.size
            return true
          }
        }
      }
    }

    println("2.2 计算孤岛数")
    println(unknown1CellsList.size)
    val cellss = mutableListOf<List<Cell>>()
    val ed = mutableListOf<String>()

    for (list1 in unknown1CellsList) {
      for (list2 in unknown1CellsList) {
        if (list1 == list2) {
          continue
        }
        val p1 = list1.toPosition()
        val p2 = list2.toPosition()
        if (p1.all { !p2.contains(it) }) {
          if(!ed.contains(p1.joinToString(""))){
            ed.add(p1.joinToString(""))
            cellss.add(list1)
          }
          if(!ed.contains(p2.joinToString(""))){
            ed.add(p2.joinToString(""))
            cellss.add(list2)
          }
        }
      }
    }
    val numberOfMinefields = cellss.size
    println(numberOfMinefields)
    if (numberOfMinefields != mines) {
      println("numberOfMinefields != mines")
      var maxWeight = 0
      val maxWeightCells = mutableListOf<Cell>()
      unknown1CellsList.forEach { list ->
        list.forEach {
          it.weight += 1
          if (it.weight > maxWeight) {
            maxWeight = it.weight
            maxWeightCells.clear()
          }
          if (it.weight == maxWeight) {
            maxWeightCells.add(it)
          }
        }
      }
      if (maxWeightCells.size != mines) {
        println("maxWeightCells.size != mines")
        return false
      }
      println("maxWeightCells.forEach { it.toMine() }")
      maxWeightCells.forEach { it.toMine() }
      mines = 0
      return true
    }
    println("2.2.1 如果等于, 是否存在区域以外的未知数")
    val insideCells = mutableListOf<Cell>()
    cells.walk {
      if (it.isUnknown() && it.inside) {
        insideCells.add(it)
      }
    }
    if (insideCells.isEmpty()) {
      if (unknown2CellsList.isEmpty()) {
        println("insideCells.isEmpty()")
        return false
      }
      return false
    }
    println("2.2.1.1 如果存在,则打开, 因为它们一定不是雷")
    insideCells.forEach { it.toNormal() }
    normalCells.addAll(insideCells)
    return true
  }

  fun solve(): String {
    while (mines > 0) {
      if (reason()) {
        println("----")
        println(mines)
        println(success())
        continue
      }
      if (!exclude()) {
        return failure()
      }
    }
    getUnknownCells().forEach { it.toNormal() }
    return success()
  }
}
