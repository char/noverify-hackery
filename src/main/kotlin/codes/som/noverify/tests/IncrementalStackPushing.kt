package codes.som.noverify.tests

import codes.som.anthony.koffee.assembleClass
import codes.som.anthony.koffee.insns.jvm.*
import codes.som.anthony.koffee.modifiers.public
import codes.som.noverify.disableBytecodeVerifier
import java.io.PrintStream

fun main(args: Array<String>) {
    disableBytecodeVerifier()

    executePayload(assembleClass(public, "Payload") {
        method(public + static, "main", void, Array<String>::class) {
            bipush(10)
            istore_1 // locals[1] = 10, where locals[1] will be our counter
            +L["loop_start"]
            ldc("Hello, world!") // Push 'Hello, world!' to the stack,
            iinc(1, -1) // and decrement the counter.
            iload_1
            ifne(L["loop_start"]) // If the counter isn't zero yet, go back to the loop head.

            // Then, we emit 10 'println()' calls into the bytecode:
            for (i in 0 until 10) {
                getstatic(System::class, "out", PrintStream::class)
                swap
                invokevirtual(PrintStream::class, "println", void, String::class)
            }

            _return
        }
    }, args)
}
