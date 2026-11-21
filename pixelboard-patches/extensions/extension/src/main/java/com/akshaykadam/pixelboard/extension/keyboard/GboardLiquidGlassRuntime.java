package com.akshaykadam.pixelboard.extension.keyboard;

import android.content.res.Configuration;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Applies a lightweight Liquid Glass treatment to Gboard's existing keyboard
 * surfaces. It deliberately keeps Gboard's view hierarchy, key hitboxes,
 * touch listeners, labels and input dispatch untouched.
 *
 * Android does not expose a general-purpose backdrop blur for arbitrary views.
 * The implementation therefore uses translucent gradients, fine reflective
 * strokes and native ripple feedback instead of blurring key contents.
 */
public final class GboardLiquidGlassRuntime {
    private static final String SOFT_KEY_VIEW =
            "com.google.android.libraries.inputmethod.widgets.SoftKeyView";
    private static final Map<View, AppliedSurface> APPLIED =
            Collections.synchronizedMap(new WeakHashMap<View, AppliedSurface>());

    private GboardLiquidGlassRuntime() {
    }

    /** Called from SoftKeyboardView.onLayout after Gboard has created its keys. */
    public static void applyToKeyboard(ViewGroup keyboardRoot) {
        if (keyboardRoot == null) {
            return;
        }
        boolean dark = isDarkMode(keyboardRoot);
        applySurface(keyboardRoot, dark, false, false);
        applyDescendants(keyboardRoot, dark);
    }

    /** Styles Gboard's existing Latin candidate/suggestion strip container. */
    public static void applyToSuggestionStrip(ViewGroup suggestionStrip) {
        if (suggestionStrip == null) {
            return;
        }
        applySurface(suggestionStrip, isDarkMode(suggestionStrip), false, true);
    }

    private static void applyDescendants(ViewGroup parent, boolean dark) {
        for (int index = 0; index < parent.getChildCount(); index++) {
            View child = parent.getChildAt(index);
            if (isSoftKeyView(child)) {
                applySurface(child, dark, isActionKey(child), false);
            }
            if (child instanceof ViewGroup) {
                applyDescendants((ViewGroup) child, dark);
            }
        }
    }

    private static boolean isSoftKeyView(View view) {
        Class<?> type = view.getClass();
        while (type != null) {
            if (SOFT_KEY_VIEW.equals(type.getName())) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }

    private static boolean isActionKey(View view) {
        CharSequence description = view.getContentDescription();
        if (description == null) {
            return false;
        }
        String value = description.toString().toLowerCase(java.util.Locale.ROOT);
        return value.contains("enter")
                || value.contains("return")
                || value.contains("shift")
                || value.contains("delete")
                || value.contains("backspace")
                || value.contains("send")
                || value.contains("search");
    }

    private static boolean isDarkMode(View view) {
        int mode = view.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    private static void applySurface(View view, boolean dark, boolean action, boolean strip) {
        AppliedSurface previous = APPLIED.get(view);
        if (previous != null
                && previous.drawable.get() == view.getBackground()
                && previous.dark == dark
                && previous.action == action
                && previous.strip == strip) {
            return;
        }

        // Rebuild from the first unmodified drawable when the system theme
        // changes. Otherwise each onLayout would stack another glass layer.
        Drawable current = view.getBackground();
        Drawable original = previous != null && previous.drawable.get() == current
                ? previous.original.get()
                : current;
        Drawable glass = createSurface(view, dark, action, strip);
        LayerDrawable composed = original == null
                ? new LayerDrawable(new Drawable[] { glass })
                : new LayerDrawable(new Drawable[] { original, glass });
        int paddingLeft = view.getPaddingLeft();
        int paddingTop = view.getPaddingTop();
        int paddingRight = view.getPaddingRight();
        int paddingBottom = view.getPaddingBottom();
        view.setBackground(composed);
        // setBackground() may adopt drawable padding. Keep Gboard's measured
        // content insets and key geometry exactly as they were before styling.
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        APPLIED.put(view, new AppliedSurface(original, composed, dark, action, strip));
    }

    private static Drawable createSurface(View view, boolean dark, boolean action, boolean strip) {
        int[] colors;
        int stroke;
        int ripple;
        float radius;
        if (strip) {
            colors = dark
                    ? new int[] { 0x443B4658, 0x30303A49, 0x3A47566B }
                    : new int[] { 0xDDEAF1FA, 0xD1DDE8F4, 0xE6F5F8FC };
            stroke = dark ? 0x668EAACB : 0x8895AFCB;
            ripple = dark ? 0x44FFFFFF : 0x334A78B8;
            radius = 16f;
        } else if (dark) {
            colors = action
                    ? new int[] { 0x665E9FE8, 0x3B3D75B7, 0x454D83C2 }
                    : new int[] { 0x48FFFFFF, 0x22FFFFFF, 0x30DCE8F5 };
            stroke = action ? 0x887EBBFA : 0x66FFFFFF;
            ripple = 0x55FFFFFF;
            radius = action ? 13f : 10f;
        } else {
            colors = action
                    ? new int[] { 0xF0DCEBFF, 0xD9C5DDF7, 0xE6D4E5FA }
                    : new int[] { 0xF2FFFFFF, 0xDCEAF1FA, 0xE8FFFFFF };
            stroke = action ? 0xB27DADE5 : 0x99FFFFFF;
            ripple = 0x334A78B8;
            radius = action ? 13f : 10f;
        }

        GradientDrawable shape = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, colors);
        shape.setCornerRadius(dp(view, radius));
        shape.setStroke(Math.max(1, dp(view, 0.8f)), stroke);

        if (Build.VERSION.SDK_INT >= 21) {
            GradientDrawable mask = new GradientDrawable();
            mask.setColor(0xFFFFFFFF);
            mask.setCornerRadius(dp(view, radius));
            return new RippleDrawable(
                    ColorStateList.valueOf(ripple), shape, mask);
        }
        return shape;
    }

    private static int dp(View view, float value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }

    private static final class AppliedSurface {
        final WeakReference<Drawable> original;
        final WeakReference<Drawable> drawable;
        final boolean dark;
        final boolean action;
        final boolean strip;

        AppliedSurface(Drawable original, Drawable drawable, boolean dark, boolean action, boolean strip) {
            this.original = new WeakReference<>(original);
            this.drawable = new WeakReference<>(drawable);
            this.dark = dark;
            this.action = action;
            this.strip = strip;
        }
    }
}
