package com.akshaykadam.pixelboard.extension.keyboard;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
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
        Assert.assertTrue(root.getBackground() instanceof LayerDrawable);
        LayerDrawable layers = (LayerDrawable) root.getBackground();
        Assert.assertEquals(1, layers.getNumberOfLayers());
        Assert.assertTrue(layers.getDrawable(0) instanceof RippleDrawable);
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
    public void existingKeyboardBackgroundIsPreservedUnderGlassLayer() {
        FrameLayout root = new FrameLayout(RuntimeEnvironment.getApplication());
        ColorDrawable original = new ColorDrawable(0xFF123456);
        root.setBackground(original);

        GboardLiquidGlassRuntime.applyToKeyboard(root);

        LayerDrawable layers = (LayerDrawable) root.getBackground();
        Assert.assertEquals(2, layers.getNumberOfLayers());
        Assert.assertSame(original, layers.getDrawable(0));
        Assert.assertTrue(layers.getDrawable(1) instanceof RippleDrawable);
    }

    @Test
    public void applyingGlassPreservesKeyboardPadding() {
        FrameLayout root = new FrameLayout(RuntimeEnvironment.getApplication());
        root.setPadding(7, 11, 13, 17);

        GboardLiquidGlassRuntime.applyToKeyboard(root);

        Assert.assertEquals(7, root.getPaddingLeft());
        Assert.assertEquals(11, root.getPaddingTop());
        Assert.assertEquals(13, root.getPaddingRight());
        Assert.assertEquals(17, root.getPaddingBottom());
    }

    @Test
    public void suggestionStripGetsItsOwnGlassSurface() {
        FrameLayout strip = new FrameLayout(RuntimeEnvironment.getApplication());

        GboardLiquidGlassRuntime.applyToSuggestionStrip(strip);

        Assert.assertTrue(strip.getBackground() instanceof LayerDrawable);
        LayerDrawable layers = (LayerDrawable) strip.getBackground();
        Assert.assertEquals(1, layers.getNumberOfLayers());
        Assert.assertTrue(layers.getDrawable(0) instanceof RippleDrawable);
    }

    @Test
    public void nullKeyboardRootIsIgnored() {
        GboardLiquidGlassRuntime.applyToKeyboard(null);
    }
}
