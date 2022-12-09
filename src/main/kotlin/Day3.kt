fun main() {
    itemMixup()
    badgeMixup()
}

fun itemMixup() {
    val inputStream = object {}.javaClass.getResourceAsStream("rucksacks.txt")!!

    var prioritySum = 0
    inputStream.bufferedReader().forEachLine { line ->
        check(line.length % 2 == 0) { "Invalid input $line" }
        val chars = mutableSetOf<Char>()
        for (i in 0 until line.length / 2) {
            chars.add(line[i])
        }
        var priority = 0
        for (i in line.length / 2 until line.length) {
            val currChar = line[i]
            if (chars.contains(currChar)) {
                priority = currChar.toPriorityValue()
                break
            }
        }
        check(priority > 0) { "Invalid input $line" }
        prioritySum += priority
    }

    println("Rucksack priority sum $prioritySum")
}

fun badgeMixup() {
    val inputStreamReader = object {}.javaClass.getResourceAsStream("rucksacks.txt")?.bufferedReader()!!
    val lines = inputStreamReader.readLines()
    check(lines.size  % 3 == 0) { "Invalid input ${lines.size}"}

    var prioritySum = 0
    for (i in lines.indices step 3) {
        val line1 = lines[i]
        val line2 = lines[i+1]
        val line3 = lines[i+2]
        val initialCandidates = mutableSetOf<Char>()
        for (c in line1) {
            initialCandidates.add(c)
        }
        val secondaryCandidates = mutableSetOf<Char>()
        for (c in line2) {
            if (initialCandidates.contains(c)) secondaryCandidates.add(c)
        }
        val finalCandidates = mutableSetOf<Char>()
        for (c in line3) {
            if (secondaryCandidates.contains(c)) finalCandidates.add(c)
        }
        check(finalCandidates.size == 1) { "Invalid input (1) $line1 (2) $line2 (3) $line3"}
        prioritySum += finalCandidates.first().toPriorityValue()
    }

    println("Badge priority sum $prioritySum")
}

fun Char.toPriorityValue(): Int {
    check(isLetter()) { "Invalid input $this" }
    return if (isUpperCase()) {
        code - UPPERCASE_ASCII_OFFSET
    } else {
        code - LOWERCASE_ASCII_OFFSET
    }
}

const val LOWERCASE_ASCII_OFFSET = 96
const val UPPERCASE_ASCII_OFFSET = 38