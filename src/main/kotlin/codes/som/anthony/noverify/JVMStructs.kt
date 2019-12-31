package codes.som.anthony.noverify

data class JVMStruct(val name: String) {
    val fields = mutableMapOf<String, Field>()
    operator fun get(f: String) = fields.getValue(f)
    operator fun set(f: String, value: Field) { fields[f] = value }

    data class Field(
            val name: String, val type: String?,
            val offset: Long, val static: Boolean
    )
}

fun getStructs(): Map<String, JVMStruct> {
    val structs = mutableMapOf<String, JVMStruct>()

    fun symbol(name: String) = unsafe.getLong(findNative(name))
    fun offsetSymbol(name: String) = symbol("gHotSpotVMStructEntry${name}Offset")
    fun derefReadString(addr: Long) = unsafe.getString(unsafe.getLong(addr))

    var currentEntry = symbol("gHotSpotVMStructs")
    val arrayStride = symbol("gHotSpotVMStructEntryArrayStride")

    while (true) {
        val typeName = derefReadString(currentEntry + offsetSymbol("TypeName"))
        val fieldName = derefReadString(currentEntry + offsetSymbol("FieldName"))
        if (typeName == null || fieldName == null)
            break

        val typeString = derefReadString(currentEntry + offsetSymbol("TypeString"))
        val static = unsafe.getInt(currentEntry + offsetSymbol("IsStatic")) != 0

        val offsetOffset = if (static) offsetSymbol("Address") else offsetSymbol("Offset")
        val offset = unsafe.getLong(currentEntry + offsetOffset)

        val struct = structs.getOrPut(typeName, { JVMStruct(typeName) })
        struct[fieldName] = JVMStruct.Field(fieldName, typeString, offset, static)

        currentEntry += arrayStride
    }

    return structs
}
