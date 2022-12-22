import kotlin.IllegalArgumentException

fun main() {
    part1()
    part2()
}

private fun part1() {
    val inputStream = object {}.javaClass.getResourceAsStream("rpstournament.txt")!!
    var score = 0

    inputStream.bufferedReader().forEachLine { line ->
        val plays = line.trim().split(" ")
        check(plays.size == 2)
        val opponentsPlay = plays[0].decodeOpponent()
        val myPlay = plays[1].decodeSelf()
        val outcome = myPlay.play(opponentsPlay)
        score += outcome.score
        score += myPlay.score
    }

    println("Final score part 1: $score")
}

private fun part2() {
    val inputStream = object {}.javaClass.getResourceAsStream("rpstournament.txt")!!
    var score = 0

    inputStream.bufferedReader().forEachLine { line ->
        val plays = line.trim().split(" ")
        check(plays.size == 2)
        val opponentsPlay = plays[0].decodeOpponent()
        val outcome = plays[1].decodeOutcome()
        val myPlay = when (outcome) {
            Outcome.DRAW -> opponentsPlay
            Outcome.WIN -> opponentsPlay.losesTo()
            Outcome.LOSE -> opponentsPlay.losesTo().losesTo()
        }
        score += outcome.score
        score += myPlay.score
    }

    println("Final score part 2: $score")
}

fun String.decodeOpponent(): Play {
    if (this == "A") return Play.ROCK
    if (this == "B") return Play.PAPER
    if (this == "C") return Play.SCISSORS
    throw IllegalArgumentException("Invalid key")
}

fun String.decodeSelf(): Play {
    if (this == "X") return Play.ROCK
    if (this == "Y") return Play.PAPER
    if (this == "Z") return Play.SCISSORS
    throw IllegalArgumentException("Invalid key")
}

fun String.decodeOutcome(): Outcome {
    if (this == "X") return Outcome.LOSE
    if (this == "Y") return Outcome.DRAW
    if (this == "Z") return Outcome.WIN
    throw IllegalArgumentException("Invalid key")
}

enum class Play(val score: Int) {
    ROCK(1),
    PAPER(2),
    SCISSORS(3);

    fun losesTo(): Play = when (this) {
            ROCK -> PAPER
            PAPER -> SCISSORS
            SCISSORS -> ROCK
    }

    fun play(other: Play): Outcome {
        if (other.losesTo() == this) return Outcome.WIN
        if (this.losesTo() == other) return Outcome.LOSE
        return Outcome.DRAW
    }
}

enum class Outcome(val score: Int) {
    LOSE(0),
    DRAW(3),
    WIN(6)
}