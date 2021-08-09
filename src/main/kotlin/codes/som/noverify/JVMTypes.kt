package codes.som.noverify

data class JVMType(
        val type: String, val superClass: String?, val size: Int,
        val oop: Boolean, val int: Boolean, val unsigned: Boolean) {
    val fields = mutableMapOf<String, JVMStruct.Field>()
}

fun getTypes(structs: Map<String, JVMStruct>): Map<String, JVMType> {
    fun symbol(name: String) = unsafe.getLong(findNative(name))
    fun offsetSymbol(name: String) = symbol("gHotSpotVMTypeEntry${name}Offset")
    fun derefReadString(addr: Long) = unsafe.getString(unsafe.getLong(addr))

    var entry = symbol("gHotSpotVMTypes")
    val arrayStride = symbol("gHotSpotVMTypeEntryArrayStride")

    val types = mutableMapOf<String, JVMType>()

    while (true) {
        val typeName = derefReadString(entry + offsetSymbol("TypeName"))
        if (typeName == null) break

        val superClassName = derefReadString(entry + offsetSymbol("SuperclassName"))

        val size = unsafe.getInt(entry + offsetSymbol("Size"))
        val oop = unsafe.getInt(entry + offsetSymbol("IsOopType")) != 0
        val int = unsafe.getInt(entry + offsetSymbol("IsIntegerType")) != 0
        val unsigned = unsafe.getInt(entry + offsetSymbol("IsUnsigned")) != 0

        val structFields = structs[typeName]?.fields
        types[typeName] = JVMType(
                typeName, superClassName, size,
                oop, int, unsigned
        ).apply {
            if (structFields != null)
                this.fields.putAll(structFields)
        }

        entry += arrayStride
    }

    return types
}
