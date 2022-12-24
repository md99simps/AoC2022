import java.util.Queue

fun main() {
    parseStartOfPacket()
    parseStartOfMessage()
}

/** Start of packet requires a buffer size of 4, so naive array-based solution is viable. */
private fun parseStartOfPacket() {
    val inputStreamReader = object {}.javaClass.getResourceAsStream("elfsignal.txt")!!.bufferedReader()

    val buffer = CharArray(4)
    var position = 0
    var next: Char

    while (inputStreamReader.read().also { next = it.toChar() } != -1) {
        if (position < 4) { // fill buffer
            buffer[position] = next
        } else {
            val cZero = buffer[0]
            val cOne = buffer[1]
            val cTwo = buffer[2]
            val cThree = buffer[3]
            if (cZero != cOne && cZero != cTwo && cZero != cThree && cOne != cTwo && cOne != cThree && cTwo != cThree) {
                println("First start of packet marker found at $position")
                println("$cZero$cOne$cTwo$cThree")
                return
            } else {
                buffer[0] = cOne
                buffer[1] = cTwo
                buffer[2] = cThree
                buffer[3] = next
            }
        }
        position++
    }

    println("No start of packet marker found")
}

/** Start of message requires a buffer size of 14, so we use a more elegant solution. */
private fun parseStartOfMessage() {
    val inputStreamReader = object {}.javaClass.getResourceAsStream("elfsignal.txt")!!.bufferedReader()

    val buffer: Queue<Char> = java.util.ArrayDeque()
    var position = 0
    var next: Char

    while (inputStreamReader.read().also { next = it.toChar() } != -1) {
        if (position < 14) { // fill buffer
            buffer.add(next)
        } else {
            val bufferSet = mutableSetOf<Char>()
            for (char in buffer) {
                bufferSet.add(char)
            }
            if (bufferSet.size == 14) {
                println("First start of message marker found at $position")
                println("$buffer")
                return
            }
            buffer.add(next)
            buffer.remove()
        }
        position++
    }

    println("No start of message marker found")
}