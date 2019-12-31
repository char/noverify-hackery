package codes.som.anthony.noverify.tests

import codes.som.anthony.koffee.assembleClass
import codes.som.anthony.koffee.insns.jvm.*
import codes.som.anthony.koffee.modifiers.public
import codes.som.anthony.noverify.disableBytecodeVerifier
import java.io.PrintStream

fun main(args: Array<String>) {
    disableBytecodeVerifier()

    executePayload(assembleClass(public, "Payload") {
        method(public + static, "main", void, Array<String>::class) {
            // Load System.out for printing later
            getstatic(System::class, "out", PrintStream::class)

            dconst_1 // Load 1.0 as a double
            dstore(1) // Store it in slot 1
            lload(1) // Load from slot 1, as a long

            // And print it out:
            invokevirtual(PrintStream::class, "println", void, long)

            _return
        }
    }, args)
}
