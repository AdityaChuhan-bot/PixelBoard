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

The current extension hook `GboardKeyboardRuntime.sanitizeKeyLabel` is injected into `AutoCenterScaleTextView.setText`. It changes label text only. `GboardRoundedKeysSettings` toggles Gboard's built-in Belka/Material 3 rounded-key flags. Neither hook owns the keyboard background, individual key drawable creation, or suggestion-strip surface rendering.

The current repository also does not contain the base Gboard APK used by `build.sh`. The patch bundle is version-specific, so renderer class names, method signatures, resource IDs, and layout behavior cannot be safely inferred from a different Gboard build.

## Implementation gate

Do not add speculative hooks or claim the Liquid Glass feature is complete until a compatible base APK is available for bytecode/resource inspection and the resulting patch passes:

1. Source-level patch contract tests.
2. Patch application against the exact supported Gboard version.
3. APK resource and DEX verification.
4. Install/launch and keyboard smoke tests on Android 16.
5. Typing latency, touch geometry, rotation, and accessibility regression checks.

## Release pipeline

Tagged releases publish the built `.mpp` patch bundle. A signed APK is published only when the repository owner configures a stable base APK URL and a persistent private signing keystore through GitHub Actions secrets. Never generate a fresh signing key for every release: Android will reject an update signed with a different certificate.
