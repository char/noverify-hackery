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
	val os = System.getProperty("os.name");
	if (os.toLowerCase().startsWith("win"))
		return findNativeMethod.invoke(null, ClassLoader.getSystemClassLoader(), name) as Long
	else
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
    var l = findNativeMethod.invoke(null, ClassLoader.getSystemClassLoader(), "gHotSpotVMStructs") as Long

	if (l == 0L) {
		val os = System.getProperty("os.name");
		System.out.println("jvm.dll/libjvm.dylib is not loaded.Try load it automatically...Your Os is " + os)
		try{
			
		if (os.toLowerCase().startsWith("win")) {

			System.load(System.getProperty("java.home") + "\\bin\\server\\" + "jvm.dll")

		} else if (os.toLowerCase().startsWith("mac")) {
			//lib/server/libjvm.dylib
			//System.load(System.getProperty("java.home") + "//lib//server//" + "libjvm.dylib")
			System.out.println("See issues on github.")
            
		}else {
			System.out.println("Operate System not Supported.")
		}
			System.out.println("Loaded")
		}
		catch(x : Throwable){
			x.printStackTrace()
		}
		
	}
    
    val flags = getFlags(getTypes(getStructs()))

    for (flag in flags) {
        if (flag.name == "BytecodeVerificationLocal"
                || flag.name == "BytecodeVerificationRemote")   {
            unsafe.putByte(flag.address, 0)
        }
    }
}
