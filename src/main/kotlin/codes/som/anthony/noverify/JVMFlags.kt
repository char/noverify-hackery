package codes.som.anthony.noverify

data class JVMFlag(val name: String, val address: Long)

fun getFlags(types: Map<String, JVMType>): List<JVMFlag> {
    val jvmFlags = mutableListOf<JVMFlag>()

    val flagType =
            types["Flag"] ?: types["JVMFlag"] ?:
            error("Could not resolve type 'Flag'")

    val flagsField =
            flagType.fields["flags"] ?:
            error("Could not resolve field 'Flag.flags'")
    val flags = unsafe.getAddress(flagsField.offset)

    val numFlagsField =
            flagType.fields["numFlags"] ?:
            error("Could not resolve field 'Flag.numFlags'")
    val numFlags = unsafe.getInt(numFlagsField.offset)

    val nameField =
            flagType.fields["_name"] ?:
            error("Could not resolve field 'Flag._name'")

    val addrField =
            flagType.fields["_addr"] ?:
            error("Could not resolve field 'Flag._addr'")

    for (i in 0 until numFlags) {
        val flagAddress = flags + (i * flagType.size)
        val flagNameAddress = unsafe.getAddress(flagAddress + nameField.offset)
        val flagValueAddress = unsafe.getAddress(flagAddress + addrField.offset)

        val flagName = unsafe.getString(flagNameAddress)
        if (flagName != null) {
            val flag = JVMFlag(flagName, flagValueAddress)
            jvmFlags.add(flag)
        }
    }

    return jvmFlags
}
