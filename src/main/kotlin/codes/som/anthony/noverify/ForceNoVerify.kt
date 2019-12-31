package codes.som.anthony.noverify

import sun.misc.Unsafe

val unsafe by lazy {
    Unsafe::class.java
            .getDeclaredField("theUnsafe")
            .also { it.isAccessible = true }
            .get(null) as Unsafe
}

private val findNativeMethod by lazy {
    ClassLoader::class.java
            .getDeclaredMethod("findNative", ClassLoader::class.java, String::class.java)
            .also { it.isAccessible = true }
}

fun findNative(name: String, classLoader: ClassLoader? = null): Long {
    return findNativeMethod.invoke(null, classLoader, name) as Long
}

fun Unsafe.getString(addr: Long): String? {
    if (addr == 0L) return null

    return buildString {
        var offset = 0

        while (true) {
            val ch = getByte(addr + offset++).toChar()
            if (ch == '\u0000') break
            append(ch)
        }
    }
}

// See JVMStructs.kt
// See JVMTypes.kt
// See JVMFlags.kt

fun disableBytecodeVerifier() {
    val flags = getFlags(getTypes(getStructs()))

    for (flag in flags) {
        if (flag.name == "BytecodeVerificationLocal"
                || flag.name == "BytecodeVerificationRemote")   {
            unsafe.putByte(flag.address, 0)
        }
    }
}
