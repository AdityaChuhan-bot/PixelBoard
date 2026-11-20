package com.akshaykadam.pixelboard.patches.gboard.features.keyboard

import com.akshaykadam.pixelboard.patches.gboard.shared.GboardMethodTarget
import com.akshaykadam.pixelboard.patches.gboard.shared.findMutableMethodOrNull
import com.akshaykadam.pixelboard.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import com.akshaykadam.pixelboard.patches.gboard.shared.isMethodReference
import com.akshaykadam.pixelboard.patches.gboard.shared.runtimeabi.RuntimeAbiCatalog
import com.akshaykadam.pixelboard.patches.gboard.shared.runtimeabi.RuntimeCallEmitter
import com.akshaykadam.pixelboard.patches.gboard.shared.runtimeabi.RuntimeCallId
import com.akshaykadam.pixelboard.patches.shared.Constants.COMPATIBILITY_GBOARD
import com.akshaykadam.pixelboard.patches.shared.addInstructions
import com.akshaykadam.pixelboard.patches.shared.bytecodePatch

private val softKeyboardViewOnLayoutTarget = GboardMethodTarget(
    classType = "Lcom/google/android/libraries/inputmethod/widgets/SoftKeyboardView;",
    name = "onLayout",
    parameterTypes = listOf("Z", "I", "I", "I", "I"),
    returnType = "V",
)

internal val gboardLiquidGlassKeyboardPatch = bytecodePatch(
    description = "Apply lightweight translucent Liquid Glass surfaces to Gboard keyboard and key views.",
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    dependsOn(gboardPatchesExtensionCarrierPatch)

    execute {
        val method = findMutableMethodOrNull(softKeyboardViewOnLayoutTarget)
            ?: error("Liquid Glass target SoftKeyboardView.onLayout was not found")

        val call = RuntimeCallId.KEYBOARD_RUNTIME_APPLY_LIQUID_GLASS
        val implementation = method.implementation
            ?: error("SoftKeyboardView.onLayout has no implementation")
        if (implementation.instructions.none {
                it.isMethodReference(RuntimeAbiCatalog.abi(call).reference)
            }) {
            // p0 is the existing SoftKeyboardView root. The hook changes only
            // backgrounds; it does not replace child views or intercept input.
            method.addInstructions(
                0,
                RuntimeCallEmitter.invoke(call, "p0"),
            )
        }
    }
}
