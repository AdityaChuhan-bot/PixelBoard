package com.akshaykadam.pixelboard.extension.keyboard;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.widget.FrameLayout;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class GboardLiquidGlassRuntimeTest {

    @Test
    public void applyToKeyboardAddsGlassSurfaceToKeyboardRoot() {
        FrameLayout root = new FrameLayout(RuntimeEnvironment.getApplication());

        GboardLiquidGlassRuntime.applyToKeyboard(root);

        Assert.assertNotNull(root.getBackground());
        Assert.assertTrue(root.getBackground() instanceof RippleDrawable);
    }

    @Test
    public void applyingRepeatedlyDoesNotReplaceTheSameSurface() {
        FrameLayout root = new FrameLayout(RuntimeEnvironment.getApplication());

        GboardLiquidGlassRuntime.applyToKeyboard(root);
        Drawable first = root.getBackground();
        GboardLiquidGlassRuntime.applyToKeyboard(root);

        Assert.assertSame(first, root.getBackground());
    }

    @Test
    public void nullKeyboardRootIsIgnored() {
        GboardLiquidGlassRuntime.applyToKeyboard(null);
    }
}
