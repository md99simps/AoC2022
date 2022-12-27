import kotlin.math.max


val heightRows = mutableListOf<String>() // init'd in main
val treeGrove: TreeGrove = Pair(mutableListOf(), mutableListOf()) // init'd in parseTreeGrid

fun main() {
    // Assume input fits in memory for simplicity, because we need to (?, plan to) parse it forwards and backwards
    object {}.javaClass.getResourceAsStream("treegrid.txt")!!.bufferedReader().forEachLine { heightRows.add(it) }

    // Part 1
    parseTreeGrid()
    howManyTreesAreVisible()
}

private fun parseTreeGrid() {
    // Validate input, we expect a rectangular grid of trees
    for (i in heightRows.indices step 2) {
        if ((i + 1) < heightRows.size) check(heightRows[i].length == heightRows[i + 1].length)
    }


    // 1st pass, forwards, compute above and to the left max height for each tree
    // For each row we examine 2 representations each of the current row and the one above it, if there is a row above
    // "height row" generally refers to a representation of the heights of individual trees
    // "tree row" generally refers to a TreeRow representation, see TreeRow typealias KDoc
    for (rowIdx in heightRows.indices) {
        val currHeightRow = heightRows[rowIdx]
        val heightRowAbove = if (rowIdx == 0) null else heightRows[rowIdx - 1]
        val currTreeRow: TreeRow = Array(currHeightRow.length) { Pair(-1, -1) }
        val treeRowAbove: TreeRow? = if (rowIdx == 0) null else treeGrove.first[rowIdx - 1]
        check(treeRowAbove != null || rowIdx == 0) { "Row above $rowIdx must be treated first" }
        for (colIdx in currHeightRow.indices) {
            val maxHeightAboveTreeAbove = if (rowIdx == 0) 0 else treeRowAbove?.get(colIdx)?.first ?: 0
            val heightOfTreeAbove = if (rowIdx == 0) 0 else heightRowAbove?.get(colIdx)?.digitToInt() ?: 0
            val maxHeightLeftOfTree = if (colIdx == 0) 0 else currTreeRow[colIdx - 1].second
            val heightOfTreeToTheLeft = if (colIdx == 0) 0 else currHeightRow[colIdx - 1].digitToInt()
            val currTreeRowEntry =
                Pair(max(maxHeightAboveTreeAbove, heightOfTreeAbove), max(maxHeightLeftOfTree, heightOfTreeToTheLeft))
            currTreeRow[colIdx] = currTreeRowEntry
        }
        treeGrove.first.add(rowIdx, currTreeRow)
    }

    // 2nd pass, backwards, compute below and to the right max height for each tree
    // For each row we examine 2 representations each of the current row and the one below it, if there is a row below
    // "height row" generally refers to a representation of the heights of individual trees
    // "tree row" generally refers to a TreeRow representation, see TreeRow typealias KDoc
    treeGrove.second.apply { // Fill list with dummy values to avoid IndexOutOfBoundsException while populating list backwards
        for (i in treeGrove.first.indices) add(i, Array(1) { Pair(-1, -1) })
    }
    for (rowIdx in heightRows.indices.reversed()) {
        val currHeightRow = heightRows[rowIdx]
        val heightRowBelow = if (rowIdx == heightRows.size - 1) null else heightRows[rowIdx + 1]
        val currTreeRow: TreeRow = Array(currHeightRow.length) { Pair(-1, -1) }
        val treeRowBelow: TreeRow? = if (rowIdx == heightRows.size - 1) null else treeGrove.second[rowIdx + 1]
        check(treeRowBelow != null || rowIdx == heightRows.size - 1) { "Row below $rowIdx must be treated first" }
        for (colIdx in currHeightRow.indices.reversed()) {
            val maxHeightBelowTreeBelow =
                if (rowIdx == heightRows.size - 1) 0 else treeRowBelow?.get(colIdx)?.first ?: 0
            val heightOfTreeBelow =
                if (rowIdx == heightRows.size - 1) 0 else heightRowBelow?.get(colIdx)?.digitToInt() ?: 0
            val maxHeightRightOfTree = if (colIdx == currHeightRow.length - 1) 0 else currTreeRow[colIdx + 1].second
            val heightOfTreeToTheRight =
                if (colIdx == currHeightRow.length - 1) 0 else currHeightRow[colIdx + 1].digitToInt()
            val currTreeRowEntry =
                Pair(max(maxHeightBelowTreeBelow, heightOfTreeBelow), max(maxHeightRightOfTree, heightOfTreeToTheRight))
            currTreeRow[colIdx] = currTreeRowEntry
        }
        treeGrove.second[rowIdx] = currTreeRow
    }
}

fun howManyTreesAreVisible() {
    check(treeGrove.first.size == treeGrove.second.size) { "Invalid tree grove" }
    var visibleCounter = 0

    for (i in treeGrove.first.indices) {
        val aboveLeftTreeRow = treeGrove.first[i]
        val belowRightTreeRow = treeGrove.second[i]
        val heightTreeRow = heightRows[i]
        for (j in aboveLeftTreeRow.indices) {
            val myHeight = heightTreeRow[j].digitToInt()
            val maxHeightAbove = aboveLeftTreeRow[j].first
            val maxHeightLeft = aboveLeftTreeRow[j].second
            val maxHeightBelow = belowRightTreeRow[j].first
            val maxHeightRight = belowRightTreeRow[j].second
            // Short circuit for edge trees. These are always visible and this otherwise treats 0-height edge trees as invisible
            if (i == 0 || j == 0 || i == heightTreeRow.length-1 || j == heightTreeRow.length-1) {
                visibleCounter++
            }
            else if (maxHeightAbove < myHeight || maxHeightRight < myHeight || maxHeightBelow < myHeight || maxHeightLeft < myHeight) {
                visibleCounter++
            }
        }
    }

    println("$visibleCounter are visible from outside the grid")
}

/**
 * Semantic name for our abstract [Array] of [Int] [Pair]s.
 *
 * Each array member is a tree, and each pair corresponds to either:
 * - The maximum height of any tree above and to the left of the tree, respectively (not including this tree)
 * - The maximum height of any tree below and to the right of the tree, respectively (not including this tree)
 */
typealias TreeRow = Array<Pair<Int, Int>>

/**
 * Semantic name for our abstract [Pair] of [MutableList] of [TreeRow]
 *
 * Each pair contains 2 [TreeRow] lists corresponding to the same row of trees. The first [TreeRow] list represents
 * the same set of trees as the second, but it contains to the "above and to the left" [TreeRow] variant, while the
 * second [TreeRow] list contains the "below and to the right" [TreeRow] variant. Together, the two lists represent
 * the maximum height of any tree between each tree and the edge of the tree grove, in all directions.
 */
typealias TreeGrove = Pair<MutableList<TreeRow>, MutableList<TreeRow>>

