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

private val latinCandidatesHolderOnSizeChangedTarget = GboardMethodTarget(
    classType = "Lcom/google/android/apps/inputmethod/latin/keyboard/widget/LatinFixedCountCandidatesHolderView;",
    name = "onSizeChanged",
    parameterTypes = listOf("I", "I", "I", "I"),
    returnType = "V",
)

internal val gboardLiquidGlassSuggestionStripPatch = bytecodePatch(
    description = "Apply a lightweight Liquid Glass surface to Gboard's Latin suggestion strip.",
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    dependsOn(gboardPatchesExtensionCarrierPatch)

    execute {
        val method = findMutableMethodOrNull(latinCandidatesHolderOnSizeChangedTarget)
            ?: error("Liquid Glass suggestion-strip target LatinFixedCountCandidatesHolderView.onSizeChanged was not found")

        val call = RuntimeCallId.KEYBOARD_RUNTIME_APPLY_SUGGESTION_STRIP
        val implementation = method.implementation
            ?: error("LatinFixedCountCandidatesHolderView.onSizeChanged has no implementation")
        if (implementation.instructions.none {
                it.isMethodReference(RuntimeAbiCatalog.abi(call).reference)
            }) {
            // p0 is the existing LinearLayout suggestion-strip container.
            method.addInstructions(
                0,
                RuntimeCallEmitter.invoke(call, "p0"),
            )
        }
    }
}
