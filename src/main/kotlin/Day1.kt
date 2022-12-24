fun main() = object {}.javaClass.getResourceAsStream("elfcalories.txt")!!.use {
    var currCalories = 0
    val calories = IntArray(3)

    it.bufferedReader().forEachLine { line ->
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            val first = calories[0]
            val second = calories[1]
            val third = calories[2]
            if (currCalories > first) {
                calories[0] = currCalories
                calories[1] = first
                calories[2] = second
            } else if (currCalories > second) {
                calories[1] = currCalories
                calories[2] = second
            } else if (currCalories > third) {
                calories[2] = currCalories
            }
            currCalories = 0
        } else {
            currCalories += trimmed.toInt()
        }
    }

    println("Max calories ${calories.sum()}")
}