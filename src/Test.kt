//enum class Status(private val identity: String) {
//  UNKNOWN("?"), MINE("x"), NORMAL("0");
//
//  companion object {
//    fun get(identity: String) = when (identity) {
//      "?" -> UNKNOWN
//      "x" -> MINE
//      else -> NORMAL
//    }
//  }
//
//  override fun toString(): String {
//    return identity
//  }
//}
//
//data class Position(val row: Int, val column: Int) {
//  override fun toString(): String = "$row, $column"
//}
//
//class Cell(identity: String, row: Int, column: Int) {
//  private var status = Status.get(identity)
//  var count: Int = if (isNormal()) identity.toInt() else -1
//  var weights = 0.0
//  val position = Position(row, column)
//
//  val surroundingCells = mutableListOf<Cell>()
//  fun getUnknownCells() = surroundingCells.filter { it.isUnknown() }
//  fun getMineCells() = surroundingCells.filter { it.isMine() }
//
//  fun isMine() = status == Status.MINE
//  fun isUnknown() = status == Status.UNKNOWN
//  fun isNormal() = status == Status.NORMAL
//
//  fun toNormal() {
//    this.status = Status.NORMAL
//    this.weights = 0.0
//    val (row, column) = position
//    this.count = Game.open(row, column)
//  }
//
//  fun toMine() {
//    this.status = Status.MINE
//  }
//
//  override fun toString(): String {
//    if (!isNormal()) {
//      return this.status.toString()
//    }
//    return count.toString()
//  }
//}
//
//typealias Board = List<List<Cell>>
//
//val Board.rows: Int
//  get() = this.size
//
//val Board.columns: Int
//  get() = this[0].size
//
//val DIRECTION = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0, 1 to 1, 1 to -1, -1 to 1, -1 to -1)
//
//fun Board.around(cell: Cell, action: (Cell) -> Unit) {
//  DIRECTION.forEach { (X, Y) ->
//    val (row, column) = cell.position
//    val nextRow = row + X
//    val nextColumn = column + Y
//    if (nextColumn !in 0 until columns || nextRow !in 0 until rows) {
//      return@forEach
//    }
//    action(this[nextRow][nextColumn])
//  }
//}
//
//fun Board.walk(action: (Cell) -> Unit) {
//  this.forEach { it.forEach(action) }
//}
//
//class MineSweeper(board: String, nMines: Int) {
//  private val cells: Board
//  private var mines = nMines
//  private val normalCells = mutableListOf<Cell>()
//
//  init {
//    cells = board.lines().map { it.split(" ") }
//      .mapIndexed { row, line -> line.mapIndexed { column, item -> Cell(item, row, column) } }
//
//    cells.walk { cell ->
//      if (cell.isNormal()) {
//        normalCells.add(cell)
//      }
//      cells.around(cell) { cell.surroundingCells.add(it) }
//    }
//  }
//
//  fun success(): String {
//    return cells.joinToString("\n") { t -> t.joinToString(" ") { it.toString() } }
//  }
//
//  private fun failure(): String = "?"
//
//  private fun compute(): Boolean {
//    val removeCells = mutableListOf<Cell>()
//    val addCells = mutableListOf<Cell>()
//    normalCells.forEach {
//      val mineCells = it.getMineCells()
//      val unknownCells = it.getUnknownCells()
//
//      if (mineCells.size == it.count) {
//        unknownCells.forEach { c -> c.toNormal() }
//        addCells.addAll(unknownCells)
//        removeCells.add(it)
//        return@forEach
//      }
//
//      if (mineCells.size + unknownCells.size == it.count) {
//        mines -= unknownCells.size
//        unknownCells.forEach { c -> c.toMine() }
//        removeCells.add(it)
//      }
//    }
//    if (removeCells.size == 0) {
//      return false
//    }
//    normalCells.addAll(addCells)
//    normalCells.removeAll(removeCells)
//    return true
//  }
//
//  private fun withWeight(): Boolean {
//    var maxWeight = 0.0
//    val maxWeightCells = mutableListOf<Cell>()
//    normalCells.forEach {
//      val unknownCells = it.getUnknownCells()
//      val mineCells = it.getMineCells()
//      unknownCells.forEach { c ->
//        c.weights += (it.count - mineCells.size) / unknownCells.size.toDouble()
//        if (c.weights > maxWeight) {
//          maxWeight = c.weights
//          maxWeightCells.clear()
//          maxWeightCells.add(c)
//        } else if (c.weights == maxWeight) {
//          maxWeightCells.add(c)
//        }
//      }
//    }
//    val unknownCells = mutableListOf<Cell>()
//    cells.walk {
//      if (it.isUnknown()) {
//        unknownCells.add(it)
//      }
//    }
//    if (unknownCells.size == maxWeightCells.size && maxWeightCells.size > mines) {
//      return false
//    }
//    if (maxWeightCells.size == 1) {
//      maxWeightCells.forEach { it.toMine() }
//      mines--
//      return true
//    }
//    val _normalCells = unknownCells.filter { !maxWeightCells.contains(it) }
////    if(_normalCells.any { it.weights == 0.0 } && maxWeightCells.size < mines && maxWeightCells.size == 1){
////      println(maxWeightCells.size)
////      maxWeightCells.forEach { println(it.position.toString()) }
////      return false
////    }
//    _normalCells.forEach { it.toNormal() }
//    normalCells.addAll(_normalCells)
//    return true
//  }
//
//  fun solve(): String {
//    while (mines > 0) {
//      if (compute()) {
//        println("----")
//        println(mines)
//        println(success())
//        continue
//      }
//      if (!withWeight()) {
//        return failure()
//      }
//    }
//    if (mines < 0) {
//      return failure()
//    }
//    cells.walk {
//      if (it.isUnknown()) {
//        it.toNormal()
//      }
//    }
//    return success()
//  }
//}
//
//// 步骤1
//// 周围的无知数量等于 中心已知数量, 打开
//// 周围的无知数量大于 中心已知数量, 跳过
//// 循环以上
//// 如果 N 次 之后, 雷数已全部推出, 则胜利
//
//// 步骤2
//// 如果出现的雷数 小于 给定的数量, 则权重推测
//// 出现最大的权重数 大于 剩余的雷数 , 则失败
//
//// 否则打开最大的权重数的未知
//// 循环以上
//

根据排除法

1. 每个边界节点的发言(周围可能的雷, 只能考究 剩余推理数为 1 的情况, 不为 1 就跳过,因为组合数就太多了, 无法提供帮助), 全都记录下来
2. 查看并记录严格包含的情况

2.1 存在则 差集一定是数字
3.2 如果不存在, 之间毫无交集的集合数是否等于雷数

3.2.1 如果等于, 是否存在区域以外的未知数

3.2.1.1 如果存在,则打开, 因为它们一定不是雷
3.2.1.2 如果不存在,则失败

3.2.2 如果不等于,则失败

3 - 存在差集
0 1 3 x 2 0
1 2 x x 2 0
? ? ? 3 1 0
? ? ? 1 0 0
? ? ? 1 0 0

2 - 存在差集
? ? ? ? ? ?
2 2 2 1 2 2
2 x 2 0 1 x

2 - 失败
1 2 3 x
1 x 3 2
2 3 ? ?
1 x ? ?

2 - 存在差集
0 1 2 2 1 0
1 2 x x 2 0
? ? ? x 2 0
? ? ? 2 1 0

3 - 存在差集 * 2
? ? ? ? 1 0
? ? - ? 3 1
1 1 2 x x 1
0 0 2 3 3 1

3 - 存在差集
0 0 0 1 ? ? ? ? ?
1 1 1 1 ? 1 1 1 1
1 x 2 2 ? 1 0 0 0
1 3 x 3 ? 1 0 0 0
0 2 x ? ? 1 0 0 0


3 x 3 x x 3 2 1
x 3 x 4 5 x 2 0
1 2 2 ? ? x 3 1
0 0 1 ? ? ? ? ?

3 x 3 x x 3 2 1
x 3 x 4 5 x 2 0
1 2 2 ? ? x 3 1
0 0 1 ? ? 2 ? ?

2 - 失败, 因为不存在任何交集的集合数 只有 1 个, 不等于假设的雷数 2
情况确实如此, 如下:
3 x 3 x x 3 2 1
x 3 x 4 5 x 2 0
1 2 2 2 * x 3 1
0 0 1 1 2 2 2 *

3 x 3 x x 3 2 1
x 3 x 4 5 x 2 0
1 2 2 * 3 x 3 1
0 0 1 1 2 2 * 1

3 - 存在差集 * 2
0 1 1 2 2
1 2 x 1 0
? ? 2 1 0
? ? 2 1 2
? - 2 x 2
? ? ? 2 2
? ? ? 1 0

5 - 存在差集
0 0 0 0
1 2 1 1
? ? ? ?
? ? 2 1
? ? 1 0
? ? 1 1
? ? ? ?
1 1 1 1
0 0 0 0

2 - 存在差集
0 1 2 2 1
1 2 x x 1
? ? 3 2 1
? ? 1 0 0
? ? 1 0 0

3 - 存在差集
0 1 ? 2 1 1
0 1 ? 2 x 1
1 2 ? 2 1 1
? ? ? 1 0 0

2 - 失败, 不存在任何交集的个数不等于雷数
? ? 1 0 0 0
? ? 3 2 2 1
1 2 x x 4 x
0 1 3 x x 2


5
3 x ? ? ? ?
3 x 3 ? ? ?
2 2 3 ? ? ?
1 x 2 ? ? ?
1 2 ? ? 2 1
0 1 ? ? 1 0

2
1 x ? ?
1 3 ? ?
1 2 ? ?
1 x 2 1
1 2 2 1

1 x x 1
1 3 3 2
1 2 x 1
1 x 2 1
1 2 2 1

2
1 2 2 1
? ? ? ?
2 3 2 1
x 2 0 0

1 2 2 1
? x x ?
2 3 2 1
x 2 0 0

6
? ? ? ? ? ? ? ? ? ? 1 0
? ? 2 1 1 2 3 ? ? ? 2 1
? ? 2 2 1 2 x 3 3 2 x 2
2 4 x 3 x 2 2 x 1 1 2 x
x 3 x 3 1 1 1 1 1 1 2 2

2
0 1 ? ?
0 1 ? ?
1 2 ? ?
4 x ? ?
x x 3 1
3 2 1 0

0 1 1 1
0 1 x 1
1 2 3 2
4 x 3 x
x x 3 1
3 2 1 0

2
1 1 2 x 1
1 x 3 3 2
1 2 ? ? ?
0 1 ? ? ?

1
2 3 3 1
2 x x 2
3 3 ? ?
2 1 ? ?

3
0 0 1 1 1 0 0 1 x ? 1 0
0 0 1 x 1 0 0 2 4 ? 3 1
0 0 1 2 2 1 0 2 x ? ? ?
0 0 0 2 x 2 0 2 x 4 x 3
0 0 1 3 x 2 0 1 1 2 2 x
0 0 1 x 2 1 1 1 1 0 1 1

0 0 1 1 1 0 0 1 x 2 1 0
0 0 1 x 1 0 0 2 4 x 3 1
0 0 1 2 2 1 0 2 x x 4 x
0 0 0 2 x 2 0 2 x 4 x 3
0 0 1 3 x 2 0 1 1 2 2 x
0 0 1 x 2 1 1 1 1 0 1 1

2
1 1 2 1
1 x 3 x
2 3 x 2
? ? 2 1
2 ? 2 0
1 ? 1 0

0 0 0 1 x 3 x 1 0 1 1 2 x 1 0 0 0 0 0 1 1 1 0 0 0 1 x 1 0
0 0 0 1 2 x 2 1 0 1 x 2 1 1 0 0 0 1 1 2 x 1 0 1 1 3 2 2 0
0 0 0 0 1 1 1 0 0 1 2 2 1 0 0 0 0 1 x 2 1 2 1 3 x 3 x 1 0
0 0 0 0 0 0 0 0 0 0 2 x 2 0 0 0 0 1 1 1 0 1 x 3 x 3 1 1 0
0 0 0 0 0 0 0 0 0 0 2 x 2 0 0 0 0 0 0 0 0 1 1 2 1 1 0 0 0
1 1 2 1 1 0 0 0 0 0 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0
1 x 3 x 2 1 1 0 0 0 0 0 0 1 x 1 1 1 1 0 0 0 0 0 0 0 0 0 0
2 3 x 2 2 x 1 0 0 0 0 1 1 2 1 2 2 x 2 2 2 1 0 0 0 0 0 1 1
1 x 2 1 1 1 1 0 0 0 0 1 x 1 0 1 x 2 2 x x 1 0 0 0 0 1 2 x
2 2 2 0 0 0 0 0 0 0 1 2 2 1 0 2 2 2 1 2 2 2 1 1 0 0 1 x 2
1 x 1 0 0 0 0 0 0 0 1 x 1 0 0 1 x 1 0 0 0 1 x 1 0 0 1 1 1

2
0 0 1 x 1 1 1 1 0 0 0 0 0 0 0 0 1 x 1 0 0 0 0 0 1 ? ?
0 0 1 1 1 1 x 1 0 0 1 1 1 1 1 1 1 1 1 1 1 1 0 1 2 ? ?
0 0 0 0 0 1 1 1 0 0 1 x 1 1 x 1 0 0 0 2 x 2 0 1 x 2 1
0 1 1 1 0 0 0 0 0 0 1 1 1 1 1 1 0 0 0 2 x 2 0 1 1 1 0
1 2 x 1 0 0 1 1 1 0 0 1 1 1 0 0 0 0 0 1 1 1 0 0 0 0 0
? ? 2 1 0 0 1 x 1 0 0 1 x 1 0 0 0 0 0 0 0 0 1 1 2 1 1
? ? 1 0 0 0 1 1 1 0 0 1 1 1 0 0 0 0 0 0 0 0 1 x 2 x 1

2
x x 1
? 3 1
? 3 1
? x 1

4
1 2 ? 1 0 0 0 0 0 0 0 0 1 x 1 0 0 0 0 0 0 1 ? 1 1 x 1
1 x ? 1 0 0 0 0 0 0 0 0 1 1 1 0 0 0 1 1 1 1 ? 1 1 1 1
1 2 2 1 0 0 1 1 1 0 0 0 0 0 0 0 0 0 1 x 2 2 1 1 0 0 0
0 1 ? 2 1 2 2 x 2 1 0 0 0 0 0 0 0 0 1 3 x 3 ? 1 0 0 0
0 1 ? 2 x 2 x 3 x 1 0 0 0 0 0 0 0 0 0 2 x 3 ? 1 0 0 0

4
1 2 ? 1 0 0 0 0 0 0 0 0 1 x 1 0 0 0 0 0 0 1 ? 1 1 x 1
1 x ? 1 0 0 0 0 0 0 0 0 1 1 1 0 0 0 1 1 1 1 ? 1 1 1 1
1 2 2 1 0 0 1 1 1 0 0 0 0 0 0 0 0 0 1 x 2 2 1 1 0 0 0
0 1 ? 2 1 2 2 x 2 1 0 0 0 0 0 0 0 0 1 3 x 3 ? 1 0 0 0
0 1 ? 2 x 2 x 3 x 1 0 0 0 0 0 0 0 0 0 2 x 3 ? 1 0 0 0

3
2 3 2 1 0
1 2 x 1 0
? ? 2 1 0
? ? 2 0 0
? ? 1 0 0
? ? 2 0 0
? ? 1 0 0

3
1 1 0 1 1 1 0 0 0
1 1 1 2 x 2 1 1 1
? ? ? ? ? ? ? ? ?
1 2 2 3 x x 3 2 1
0 1 x 2 2 3 x 2 1

4
x 2 1 0 0
2 x 1 0 0
2 2 2 1 1
? ? 1 ? ?
1 1 1 2 x
0 0 0 1 1
0 0 0 0 0
0 0 0 0 0
1 1 1 1 1
? ? 1 ? ?
x 2 1 1 1
1 1 0 0 0
0 0 0 0 0
1. 记录每个依赖推理的节点发言, 即周围可能的雷
2.2 计算孤岛数
6
6
numberOfMinefields != mines
maxWeightCells.forEach { it.toMine() }
Dead you are!! You stepped on a mine at (3,0)...

3
? ? ? ? ? x 3 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 x 1 0 0 0 0 0 0 0 0
? ? ? ? ? x x 1 0 0 0 0 1 2 2 1 0 0 0 0 0 0 1 1 1 0 0 0 0 0 0 0 0
x x x 3 3 x 3 1 0 0 0 0 1 x x 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
2 3 3 2 2 2 2 2 1 1 0 0 1 2 2 1 0 0 0 1 1 1 0 0 0 0 0 1 1 1 0 0 0
1 1 1 x 1 1 x 3 x 1 0 0 0 0 0 0 0 0 0 2 x 2 0 0 0 0 0 1 x 1 0 0 0
x 3 3 2 1 1 2 x 2 1 0 0 0 0 0 1 1 1 0 2 x 2 0 0 0 1 1 2 1 1 0 0 0
3 x x 1 0 0 1 1 1 0 0 0 0 0 0 1 x 1 0 1 1 1 0 0 0 1 x 1 0 0 0 1 1
3 x 4 1 0 0 0 0 0 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 1 1 1 0 0 0 1 x
2 x 2 0 0 0 0 0 1 2 x 1 1 x 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1
2 2 2 0 0 0 0 0 1 x 2 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
1 x 1 1 1 2 1 1 1 1 1 0 0 0 1 1 1 1 1 1 0 0 0 0 1 1 2 1 1 0 1 2 2
1 1 1 1 x 2 x 1 0 0 0 0 0 0 1 x 2 2 x 2 2 1 1 0 1 x 2 x 1 0 1 x x
0 0 0 1 1 2 1 1 1 2 2 1 0 0 1 2 x 3 3 x 4 x 3 1 2 2 3 2 1 0 1 3 3
0 0 0 1 1 1 0 0 1 x x 2 0 1 1 3 2 3 x 3 x x 3 x 1 1 x 1 0 0 0 1 x
0 1 1 2 x 1 0 0 1 3 x 2 0 1 x 2 x 2 1 2 3 3 3 2 2 2 1 1 0 0 0 1 1
0 1 x 2 1 1 0 1 1 2 2 2 2 3 3 3 1 1 0 0 1 x 1 1 x 2 1 0 0 0 0 0 0
0 1 1 1 0 1 2 3 x 1 1 x 2 x x 1 0 0 0 0 1 1 2 2 3 x 1 1 2 2 1 1 1
0 0 1 1 1 2 x x 3 3 2 2 2 2 2 1 0 0 0 0 1 1 3 x 3 1 1 1 x x 2 2 x
1 2 2 x 1 2 x 4 x 2 x 1 1 1 1 0 0 0 0 0 1 x 4 x 3 0 0 1 2 3 x 2 1
x 2 x 3 2 2 1 2 1 2 1 1 1 x 1 0 0 0 0 1 2 2 3 x 2 0 0 0 0 1 1 1 0
1 2 1 2 x 1 0 0 0 0 0 0 1 1 1 0 0 0 1 2 x 1 1 1 1 0 0 1 1 1 0 0 0
0 1 1 2 1 1 0 1 2 2 1 0 0 0 1 1 1 0 1 x 2 1 0 1 1 1 0 1 x 1 0 0 0
0 1 x 1 0 0 0 1 x x 1 0 0 0 1 x 2 1 1 1 1 0 0 1 x 2 1 2 1 1 0 0 0
0 1 1 1 0 0 0 1 2 2 1 0 0 0 1 2 x 1 0 0 0 0 0 1 1 2 x 1 0 0 0 1 1
0 0 0 0 1 1 1 0 1 1 2 1 1 1 1 2 1 1 0 0 0 0 0 0 0 1 1 1 0 0 0 1 x
1 1 1 0 1 x 1 0 1 x 2 x 1 1 x 1 0 0 0 0 1 1 1 0 0 0 0 1 1 2 1 2 1
2 x 2 0 1 1 1 0 1 1 2 1 1 1 1 1 1 1 1 0 1 x 1 0 1 1 1 2 x 3 x 1 0
2 x 2 0 0 0 0 0 0 0 1 1 1 0 0 1 2 x 1 0 1 1 1 0 1 x 1 2 x 3 1 1 0
1 1 1 0 0 0 0 0 0 0 1 x 1 0 0 1 x 2 1 0 0 0 0 0 1 1 1 1 1 1 0 0 0

2
0 1 x 1 2 x x x x 1 0 0 1 1 2 2 x 2 x x x x 2 0 1 2 ? ? ? ?
0 1 1 1 2 x 4 3 2 1 0 0 1 x 3 x 3 3 3 5 6 x 2 0 2 x ? ? ? ?
2 2 1 0 1 2 2 1 0 0 0 0 2 3 x 5 x 3 2 x x 2 1 0 2 x 4 x x 2
x x 1 0 0 1 x 1 1 1 1 0 1 x 3 x x x 2 2 2 1 1 1 2 1 2 2 2 1
2 2 1 0 0 1 1 1 1 x 1 0 1 1 3 3 4 2 1 1 1 1 1 x 1 0 1 1 1 0
0 0 0 0 0 1 1 1 1 1 2 1 1 0 1 x 1 0 0 1 x 1 2 2 2 0 2 x 3 1
0 0 1 1 2 2 x 2 1 0 1 x 1 0 1 1 1 0 0 2 2 2 1 x 1 0 2 x 3 x
0 0 1 x 2 x 3 x 1 0 1 1 2 1 1 0 0 0 0 1 x 1 1 1 1 0 1 1 2 1
0 0 1 1 2 2 3 2 1 0 0 0 1 x 2 1 1 0 0 1 2 2 1 0 0 0 0 0 0 0
0 0 0 0 1 2 x 2 1 0 0 0 1 1 2 x 1 0 0 0 2 x 2 0 0 0 0 0 0 0
0 0 0 0 1 x 3 x 1 1 1 1 0 0 1 1 2 1 1 0 2 x 2 0 1 1 1 0 0 0
1 1 0 0 1 1 2 1 1 1 x 1 0 0 0 1 2 x 1 0 1 2 2 1 2 x 2 0 0 0
x 2 1 0 0 0 0 0 0 1 1 2 1 1 0 1 x 2 1 0 0 1 x 1 2 x 2 0 0 0
2 x 1 0 0 0 0 0 0 0 0 2 x 3 1 2 1 1 0 0 0 1 1 1 2 2 2 0 0 0
1 1 1 0 0 0 0 0 0 1 1 3 x 3 x 2 1 1 0 0 0 0 0 1 2 x 1 0 0 0
1 1 1 0 0 0 0 0 0 1 x 2 1 2 1 2 x 1 0 0 0 0 0 1 x 3 2 0 0 0
1 x 1 0 0 0 0 0 0 1 1 1 0 0 0 1 1 1 0 0 0 0 0 1 2 x 1 0 0 0
2 2 1 0 0 1 1 1 1 1 1 0 1 1 2 1 1 0 0 0 0 0 0 0 1 1 1 0 0 0
x 1 0 0 0 1 x 1 1 x 1 0 1 x 2 x 1 0 0 0 0 0 0 0 0 0 1 1 1 0
1 1 0 0 0 1 1 1 1 2 2 1 1 2 4 3 2 0 0 0 1 2 2 1 0 1 2 x 1 0
0 0 0 0 1 1 1 0 0 1 x 1 0 1 x x 1 0 0 0 1 x x 3 1 2 x 2 1 0
1 1 1 1 2 x 1 0 0 1 1 1 0 2 3 3 1 0 0 0 1 3 x 3 x 2 1 1 0 0
x 1 1 x 2 1 1 0 0 0 0 0 0 1 x 1 0 0 0 0 0 1 2 3 2 1 0 0 1 1
1 1 1 1 2 1 1 0 0 0 0 0 0 1 1 1 0 0 0 0 0 0 1 x 2 1 1 1 2 x
1 1 1 0 1 x 1 0 0 0 1 1 1 0 1 1 1 0 0 1 1 1 1 2 x 1 1 x 3 2
1 x 1 0 1 1 1 0 0 0 1 x 1 0 1 x 1 0 0 1 x 1 0 1 1 1 1 1 2 x