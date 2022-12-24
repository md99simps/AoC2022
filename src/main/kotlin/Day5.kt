import java.util.*
import kotlin.collections.ArrayDeque

fun main() {
    rearrangeCrates()
}

private fun rearrangeCrates() = object {}.javaClass.getResourceAsStream("craterearrangement.txt")!!.use {
    // Small shortcut, we can easily know the number of piles by examining the input file. Ideally we'd parse this from the input.
    val cratePiles9000: CratePiles = Array(9) { ArrayDeque() }
    val cratePiles9001: CratePiles = Array(9) { ArrayDeque() }

    var hasProcessedCrates = false

    // Input assumptions:
    // 1. A line with '[' as its first non-whitespace character is a crate input.
    // 2. A line with '1' as its first non-whitespace character is the end of crate input.
    // 3. A line with "move" as its first non-whitespace substring is a move instruction.
    // 4. All empty lines are meaningless, and they can be discarded.
    // 5. All lines will match a predicate from assumptions 1-4.
    // 6. All crate input will proceed all move instructions.
    //
    // For inputs that violate assumptions 1-4, behavior is undefined
    // For inputs that violate assumptions 5-6, the program will exit with IllegalStateException
    it.bufferedReader().forEachLine { line ->
        if (line.trim().startsWith('[')) {
            if (hasProcessedCrates) { // All crate input must proceed all move instructions
                throw IllegalStateException("Out of order input $line")
            }
            applyCrateInput(line, cratePiles9000)
            applyCrateInput(line, cratePiles9001)
        } else if (line.trim().startsWith('1')) {
            hasProcessedCrates = true // reaching this line ought to mean we're done parsing crate input
        } else if (line.trim().isEmpty()) {
            return@forEachLine // discard empty lines
        } else if (line.trim().startsWith("move")) {
            if (!hasProcessedCrates) { // All crate input must proceed all move instructions
                throw IllegalStateException("Out of order input $line")
            }
            println(line)
            applyMoveInstruction(line, cratePiles9000)
            applyMoveInstruction(line, cratePiles9001, craneVariant = CraneVariant.MULTIMOVE)
            //cratePiles9000.printDbg()
            cratePiles9001.printDbg()
        } else {
            throw IllegalStateException("Unexpected line $line")
        }
    }

    //cratePiles9000.prettyPrintDestructive()
    cratePiles9001.prettyPrintDestructive()
}

/** Semantic name for our 2 dimensional, abstract collection of collections of characters */
private typealias CratePiles = Array<ArrayDeque<Char>>

/** 2 different cranes can be used to move crates in different ways.  */
enum class CraneVariant {
    ORIGINAL,
    MULTIMOVE,
}

/** Parse a line we've determined to be a crate input, applying the parsed input to [currPiles]. */
private fun applyCrateInput(line: String, currPiles: CratePiles) {
    for (i in 1 until line.length step 2) {
        val char = line[i]
        if (char.isLetter()) {
            val whichPile = i / 4
            currPiles.stackCrate(char, whichPile)
        }
    }
}

/** Parse a line we've determined to be a move instruction, applying the parsed input to [currPiles]. */
private fun applyMoveInstruction(
    line: String,
    currPiles: CratePiles,
    craneVariant: CraneVariant = CraneVariant.ORIGINAL
) {
    val words = line.split(' ')
    require(words.size == 6) { "Invalid move instruction $line" }
    // Don't catch NumberFormatException, we can't do anything about it.
    val instruction = CrateMove(words[1].toInt(), words[3].toInt(), words[5].toInt())
    if (craneVariant == CraneVariant.ORIGINAL) {
        currPiles.moveCrates(instruction)
    } else {
        currPiles.moveCratesMulti(instruction)
    }
}

/**
 * A representation of a [quantity] of crates to be moved from a pile of crates indicated by [fromPile] to a pile of
 * crates indicated by [toPile].
 */
data class CrateMove(
    val quantity: Int,
    val fromPile: Int,
    val toPile: Int
)

/** Stacks a crate indicated by [crate] to the pile indicated by [whichPile]. */
private fun CratePiles.stackCrate(crate: Char, whichPile: Int) {
    require(whichPile >= 0 && whichPile < this.size) { "Can't add crate to pile $whichPile that does not exist" }
    this[whichPile].addLast(crate)
}

/** Applies the move instruction represented by [move], moving crates between piles individually. */
private fun CratePiles.moveCrates(move: CrateMove) {
    require(move.fromPile > 0 && move.fromPile <= this.size) { "Can't move crates from pile ${move.fromPile - 1} that does not exist" }
    require(move.toPile > 0 && move.toPile <= this.size) { "Can't move crates to pile ${move.toPile - 1} that does not exist" }
    if (move.fromPile == move.toPile) return // no-op
    val fromPile = this[move.fromPile - 1]
    val toPile = this[move.toPile - 1]
    check(fromPile.size >= move.quantity) { "Can't move ${move.quantity} crates from pile {${move.fromPile - 1} with size ${fromPile.size}" }
    for (i in 0 until move.quantity) toPile.addFirst(fromPile.removeFirst())
}

/** Applies the move instruction represented by [move], moving the full quantity of crates between piles at once. */
private fun CratePiles.moveCratesMulti(move: CrateMove) {
    require(move.fromPile > 0 && move.fromPile <= this.size) { "Can't move crates from pile ${move.fromPile - 1} that does not exist" }
    require(move.toPile > 0 && move.toPile <= this.size) { "Can't move crates to pile ${move.toPile - 1} that does not exist" }
    if (move.fromPile == move.toPile) return // no-op
    val fromPile = this[move.fromPile - 1]
    val toPile = this[move.toPile - 1]
    check(fromPile.size >= move.quantity) { "Can't move ${move.quantity} crates from pile {${move.fromPile - 1} with size ${fromPile.size}" }
    val intermediateStack = Stack<Char>()
    for (i in 0 until move.quantity) intermediateStack.add(fromPile.removeFirst())
    for (i in 0 until move.quantity) toPile.addFirst(intermediateStack.pop())

}

private fun CratePiles.printDbg() {
    for (pile in this) {
        println(pile)
    }
}

// This doesn't have to be destructive, but I don't care to optimize that piece away
private fun CratePiles.prettyPrintDestructive() {
    var maxSize = 0
    val pileSizes = Array(this.size) { 0 }
    for (i in indices) {
        val currPile = this[i]
        if (currPile.size > maxSize) maxSize = currPile.size
        pileSizes[i] = currPile.size
    }
    for (i in (maxSize - 1) downTo 0) {
        for (j in indices) {
            val currPile = this[j]
            if (pileSizes[j] > i) print(" [${currPile.removeFirst()}] ")
            else print("     ")
        }
        print("\n")
    }
}