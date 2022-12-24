import FileSystem.Entity
import FileSystem.Entity.*
import FileSystem.Entity.Root.parent
import java.util.*

fun main() {
    parseFileSystem()
    FileSystem.print()

    // Ridiculous function name for a ridiculous problem statement
    sumOfDirectorySizesUnder100kWithDoubleCountingOfChildren()
    findSmallestDirectoryToDelete()
}

private fun parseFileSystem() = object {}.javaClass.getResourceAsStream("terminaloutput.txt")!!.bufferedReader().use {
    var currentDirectory: Directory? = null
    var listMode = false

    // Crude state machine for parsing command line output into FileSystem
    it.forEachLine { line ->
        if (currentDirectory == null) { // Initial command
            check(line == "$ cd /") { "First line of input *must* be cd /" }
            currentDirectory = Root
        } else if (line == "$ cd ..") { // Navigate back
            check(currentDirectory !is Root) { "Can't back out of root" }
            currentDirectory = currentDirectory!!.parent
            listMode = false
        } else if (line.startsWith("$ cd")) { // Navigate down
            val words = line.split(' ')
            check(words.size == 3) { "Unrecognized cd command $line" }
            val navigateTo = currentDirectory?.getChildByName(words[2])
            check(navigateTo is Directory) { "Can't navigate to ${words[2]}, options available ${currentDirectory!!.children}" }
            currentDirectory = navigateTo
            listMode = false
        } else if (line == "$ ls") { // list
            listMode = true
        } else if (listMode) {
            check(currentDirectory != null) { "Zoinks, we're lost!" }
            val words = line.split(' ')
            check(words.size == 2) { "Unrecognized dir $line" }
            val dirOrSize = words[0]
            val name = words[1]
            if (dirOrSize == "dir") { // Add directory as child
                currentDirectory!!.addChild(
                    Directory(
                        name,
                        currentDirectory!!,
                        currentDirectory!!.level + 1
                    )
                )
            } else { // Add file as child
                // Don't catch NumberFormatException, nothing to be done
                currentDirectory!!.addChild(File(name, dirOrSize.toInt()))
            }
        }
    }
}

private fun sumOfDirectorySizesUnder100kWithDoubleCountingOfChildren() {
    var sum = 0
    val toVisit: Queue<Entity> = ArrayDeque()
    toVisit.addAll(Root.children)

    // BFS
    while (!toVisit.isEmpty()) {
        val visiting = toVisit.remove()
        if (visiting is Directory) {
            if (visiting.size <= 100_000) {
                sum += visiting.size
            }
            toVisit.addAll(visiting.children)
        }
    }

    println("The sum of all directory sizes under 100,000 including double counting of children is $sum")
}

private fun findSmallestDirectoryToDelete() {
    val spaceAvailable = 70_000_000 - Root.size
    check(spaceAvailable > 0) { "We're hosed, there's no space at all." }
    val spaceToFree = 30_000_000 - spaceAvailable
    check(spaceToFree > 0) { "No need to delete anything, there's already enough space." }

    val toVisit: Queue<Entity> = ArrayDeque()
    toVisit.addAll(Root.children)
    var smallestDirThatsBigEnough: Directory? = null

    // BFS
    while (!toVisit.isEmpty()) {
        val visiting = toVisit.remove()
        if (visiting is Directory) {
            if (visiting.size >= spaceToFree) {
                if (visiting.size <= (smallestDirThatsBigEnough?.size ?: Int.MAX_VALUE)) {
                    smallestDirThatsBigEnough = visiting
                }
            }
            if (visiting.size > spaceToFree) { // optimization -- don't visit children if parent isn't big enough
                toVisit.addAll(visiting.children)
            }
        }
    }

    check(smallestDirThatsBigEnough != null) { "No one directory can be deleted to free enough space." }
    println("Smallest directory that will free enough space when deleted $smallestDirThatsBigEnough with size ${smallestDirThatsBigEnough.size}")
}


/** Singleton representation of a file system, starting from singleton [Entity.Root] */
object FileSystem {

    fun print() {
        val stack = Stack<Directory>().apply { add(Root) }

        // DFS
        while (stack.size > 0) {
            val current = stack.pop()
            val files = current.children.filterIsInstance(File::class.java)
            val directories = current.children.filterIsInstance(Directory::class.java)
            stack.addAll(directories)
            for (i in 0..current.level * 2) print(" ")
            print(current)
            println()
            for (file in files) {
                for (i in 0..(current.level + 1) * 2) print(" ")
                print(file)
                println()
            }
        }
    }

    /**
     * Abstract representation of a file system entity with a [name] and [size].
     *
     * Only 3 concrete implementations exist: [Root], [Directory], and [File].
     */
    sealed class Entity {
        abstract val name: String
        abstract val size: Int

        /**
         * Simple directory representation with [name], a mutable set of [Entity] as its children, and a [parent],
         * which is null only in the special case of [Root].
         */
        open class Directory(
            override val name: String,
            val parent: Directory?,
            val level: Int = parent?.level?.plus(1)
                ?: throw IllegalArgumentException("Must provide level with null parent")
        ) : Entity() {
            private val _children = mutableSetOf<Entity>()

            val children: Set<Entity>
                get() = _children

            override val size: Int
                get() = _children.sumOf { it.size }

            fun addChild(toAdd: Entity) = _children.add(toAdd)

            fun getChildByName(name: String): Entity? = _children.firstOrNull { it.name == name }

            override fun toString() = "- $name (dir)"
        }

        /** Special case singleton [Directory] with null [parent]. */
        object Root : Directory("/", parent = null, level = 0)

        /** Simple representation of a file with [name] and [size]. */
        class File(override val name: String, override val size: Int) : Entity() {
            override fun toString() = "- $name (file, size $size)"
        }
    }
}