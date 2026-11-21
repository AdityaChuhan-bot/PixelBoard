# Liquid Glass keyboard implementation notes

## Goal

Add an Apple-inspired Liquid Glass visual treatment to the existing Gboard-based PixelBoard keyboard. This is a renderer-level modification, not a standalone keyboard screen or a separate keyboard application.

## Current implementation

The repository contains two bytecode hooks and one Android runtime:

- `SoftKeyboardView.onLayout(ZIIII)V` invokes `GboardLiquidGlassRuntime.applyToKeyboard(ViewGroup)` after Gboard lays out its keyboard children.
- `LatinFixedCountCandidatesHolderView.onSizeChanged(IIII)V` invokes `GboardLiquidGlassRuntime.applyToSuggestionStrip(ViewGroup)` after the suggestion container changes size.
- The runtime applies theme-aware translucent gradients, rounded corners, reflective strokes, and native ripple feedback to the keyboard root, detected `SoftKeyView` descendants, and suggestion strip.
- Existing backgrounds and view padding are retained, and applied drawables are cached to avoid stacking layers on repeated layout callbacks.

The public patch registry now exposes these two hooks together as one selectable **Liquid Glass Keyboard** patch. It is enabled by default and declares the repository's Gboard compatibility target. A registry contract test verifies that the public patch includes both hooks and is included in the generated published inventory.

## Visual limitations

This is a lightweight frosted-glass approximation, not a complete reproduction of Apple's Liquid Glass. It does not implement true backdrop blur, dynamic refraction, or animated light reflections. Android does not expose a general-purpose backdrop blur for arbitrary child views; applying a blur effect directly to key views would also blur their legends and reduce readability.

## Compatibility and regression requirements

Preserve Gboard's existing input connection, key hitboxes, gesture typing, language layouts, accessibility/TalkBack semantics, voice typing, clipboard, OCR, emoji, and AI Writing Tools. Rendering must remain GPU-light on mid-range devices.

## Validation status

Registry wiring and its source-level contract test have been added. The feature must not be considered release-ready until the following checks pass:

1. Patch-module compilation and all patch tests.
2. Generated patch inventory includes **Liquid Glass Keyboard** with both dependencies.
3. Patch application succeeds against the exact supported Gboard APK version.
4. APK resource and DEX verification.
5. Install/launch and keyboard smoke tests on Android 16.
6. Typing latency, touch geometry, rotation, accessibility, suggestion updates, autocorrection, and voice-typing regression checks.

A green source build does not replace patch-application validation or testing on a physical device.

## Release pipeline

Tagged releases publish the built `.mpp` patch bundle. A signed APK is published only when the repository owner configures a stable base APK URL and a persistent private signing keystore through GitHub Actions secrets. Never generate a fresh signing key for every release: Android will reject an update signed with a different certificate.
