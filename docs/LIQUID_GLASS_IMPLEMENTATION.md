# Liquid Glass keyboard implementation notes

## Goal

Add an Apple-inspired Liquid Glass visual treatment to the existing Gboard-based PixelBoard keyboard. This is a renderer-level modification, not a standalone keyboard screen or a separate keyboard application.

## Required visual behavior

- Keyboard background: translucent dark/light surface with a restrained blur or a visually consistent fallback.
- Keycaps: semi-transparent surfaces, subtle gradients, fine reflective edges, clear legends, and distinct action-key treatment.
- Suggestion row: one continuous floating glass surface that remains legible while suggestions update.
- Interaction: short scale/brightness feedback and native popup previews without delaying input dispatch.
- Layouts: portrait, landscape, split keyboard, and accessibility font/display scaling.

## Non-negotiable compatibility

Preserve Gboard's existing input connection, key hitboxes, gesture typing, language layouts, accessibility/TalkBack semantics, voice typing, clipboard, OCR, emoji, and AI Writing Tools. Rendering must remain GPU-light on mid-range devices; blur must have a fallback and must not be applied as an expensive independent shader to every key.

## Repository audit

The uploaded PixelBoard 1.3.5 APK contains Gboard 18.6.3.100 and exposes `SoftKeyboardView.onLayout(ZIIII)V` plus the `SoftKeyView` view hierarchy. A new bytecode patch injects `GboardLiquidGlassRuntime.applyToKeyboard(ViewGroup)` at the beginning of that existing layout method. The runtime styles the keyboard root and actual `SoftKeyView` instances using theme-aware translucent gradients, reflective strokes, and native ripple feedback. It caches applied drawables to avoid allocating a new background on every layout pass. A second bytecode hook targets `LatinFixedCountCandidatesHolderView.onSizeChanged(IIII)V`, the existing `LinearLayout` used for the Latin candidate/suggestion row, and applies a single rounded glass surface to that container.

The runtime intentionally does not apply `RenderEffect` to the key views: that would blur key labels as well as the background. Android does not expose a general-purpose backdrop blur for arbitrary child views, so this is a frosted-glass approximation. Suggestion-strip styling is now represented by a separate experimental patch hook. It is not yet device-validated and must be tested with live candidate updates, autocorrection, voice typing, and landscape layouts. The uploaded APK is already a patched PixelBoard build, not a stock Gboard base, and cannot automatically be treated as a clean input to the existing full patch pipeline.

## Implementation gate

Do not claim the Liquid Glass feature is complete until the new hook passes source-level and patch-application checks, and the resulting APK passes:

1. Source-level patch contract tests and CI build.
2. Patch application against Gboard 18.6.3.100.
3. APK resource and DEX verification.
4. Install/launch and keyboard smoke tests on Android 16.
5. Typing latency, touch geometry, rotation, and accessibility regression checks.

## Release pipeline

Tagged releases publish the built `.mpp` patch bundle. A signed APK is published only when the repository owner configures a stable base APK URL and a persistent private signing keystore through GitHub Actions secrets. Never generate a fresh signing key for every release: Android will reject an update signed with a different certificate.
