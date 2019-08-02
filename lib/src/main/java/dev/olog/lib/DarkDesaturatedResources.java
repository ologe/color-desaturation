package dev.olog.lib;

import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;

public class DarkDesaturatedResources extends Resources {

    private final boolean isDarkMode;
    private final float desaturationAmount;
    private final float minDesaturation;
    private SparseArray<ColorStateList> cache = new SparseArray<>();

    public DarkDesaturatedResources(
            boolean isDarkMode,
            float desaturationAmount,
            float minDesaturation,
            AssetManager assets,
            DisplayMetrics metrics,
            Configuration config
    ) {
        super(assets, metrics, config);
        this.isDarkMode = isDarkMode;
        this.desaturationAmount = desaturationAmount;
        this.minDesaturation = minDesaturation;
    }

    @SuppressWarnings("unused")
    public DarkDesaturatedResources(
            boolean isDarkMode,
            AssetManager assets,
            DisplayMetrics metrics,
            Configuration config
    ) {
        this(isDarkMode, 0.25f, 0.75f, assets, metrics, config);
    }

    @Override
    public int getColor(int id, @Nullable Theme theme) throws NotFoundException {
        if (isDarkMode) {
            System.out.println("get color " + id);
            int requestedColor = super.getColor(id, theme);
            return ColorDesaturationUtils.desaturate(requestedColor, desaturationAmount, minDesaturation);
        }
        return super.getColor(id, theme);
    }

    @NonNull
    @Override
    public ColorStateList getColorStateList(int id, @Nullable Theme theme) throws NotFoundException {
        if (!isDarkMode) {
            return super.getColorStateList(id, theme);
        }
        final ColorStateList requestedColor = super.getColorStateList(id, theme);

        System.out.println("get color state list " + id);
        System.out.println("opaque " + requestedColor.isOpaque());
        System.out.println("stateful" + requestedColor.isStateful());

        final ColorStateList cached = cache.get(id);
        if (cached != null) {
            return cached;
        }
        try {
            ColorStateList desaturated = new ColorStateList(
                    getStates(requestedColor),
                    desaturateGroup(getColors(requestedColor))
            );
            cache.append(id, desaturated);
            return desaturated;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return requestedColor;
        }
    }

    private int[][] getStates(ColorStateList color) throws Exception {
        //noinspection JavaReflectionMemberAccess
        Method method = ColorStateList.class.getMethod("getStates");
        method.setAccessible(true);
        return (int[][]) method.invoke(color);
    }

    private int[] getColors(ColorStateList color) throws Exception {
        //noinspection JavaReflectionMemberAccess
        Method method = ColorStateList.class.getMethod("getColors");
        method.setAccessible(true);
        return (int[]) method.invoke(color);
    }

    private int[] desaturateGroup(int[] colors) {
        for (int i = 0; i < colors.length; i++) {
            colors[i] = ColorDesaturationUtils.desaturate(colors[i], desaturationAmount, minDesaturation);
        }
        return colors;
    }

}
