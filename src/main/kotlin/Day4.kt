fun main() {
    overlappingAssignments()
}

private fun overlappingAssignments() {
    val inputStream = object {}.javaClass.getResourceAsStream("sectionassignments.txt")!!

    var completelyOverlappingPairs = 0
    var overlappingPairs = 0

    inputStream.bufferedReader().forEachLine { line ->
        val assignments = line.split(',')
        check(assignments.size == 2) { "Invalid assignment pair $assignments" }
        val elf1Bounds =
            runCatching { assignments[0].split('-') }.mapCatching { boundsStrList -> boundsStrList.toIntPair() }
                .getOrThrow()
        val elf2Bounds =
            runCatching { assignments[1].split('-') }.mapCatching { boundsStrList -> boundsStrList.toIntPair() }
                .getOrThrow()
        if (elf1Bounds.contains(elf2Bounds) || elf2Bounds.contains(elf1Bounds)) completelyOverlappingPairs++
        if (elf1Bounds.overlaps(elf2Bounds)) overlappingPairs++
    }

    println("$completelyOverlappingPairs have completely overlapping assignments")
    println("$overlappingPairs have overlapping assignments")
}

/**
 * Returns a [Pair] of Integers given a [List] of [String]s of size 2. Throws if the list is too large or contains
 * items which cannot be converted to [Int]
 */
@Throws(NumberFormatException::class, IllegalArgumentException::class)
private fun List<String>.toIntPair(): Pair<Int, Int> {
    require(this.size == 2) { "Invalid assignment $this" }
    val (a, b) = this
    return Pair(a.toInt(), b.toInt())
}

/** Returns `true` if this [Pair] represents an [Int] range which fully contains the range represented by [other] */
private fun Pair<Int, Int>.contains(other: Pair<Int, Int>): Boolean {
    require(this.first <= this.second) { "Invalid assignment $this" }
    require(other.first <= other.second) { "Invalid assignment $other" }
    return this.first <= other.first && this.second >= other.second
}

/** Returns `true` if this [Pair] represents an [Int] range which overlaps the range represented by [other] */
private fun Pair<Int, Int>.overlaps(other: Pair<Int, Int>): Boolean {
    require(this.first <= this.second) { "Invalid assignment $this" }
    require(other.first <= other.second) { "Invalid assignment $other" }
    return  (this.first >= other.first && this.first <= other.second) || (other.first >= this.first && other.first <= this.second)
}