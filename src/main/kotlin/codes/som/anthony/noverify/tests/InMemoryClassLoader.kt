package codes.som.anthony.noverify.tests

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

class InMemoryClassLoader(val node: ClassNode, private val cwFlags: Int) : ClassLoader() {
    private val classData by lazy {
        val writer = ClassWriter(cwFlags)
        node.accept(writer)
        writer.toByteArray()
    }

    override fun findClass(name: String): Class<*>? {
        if (name == node.name.replace('/', '.'))
            return defineClass(name, classData, 0, classData.size)

        return null
    }

    fun load(): Class<*> = findClass(node.name.replace('/', '.'))!!
}

fun executePayload(payload: ClassNode, args: Array<String>) {
    InMemoryClassLoader(payload, ClassWriter.COMPUTE_MAXS)
            .load()
            .getDeclaredMethod("main", Array<String>::class.java)
            .invoke(null, args)
}
