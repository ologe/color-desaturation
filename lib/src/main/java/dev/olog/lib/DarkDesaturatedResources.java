package dev.olog.lib;

import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
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

    @SuppressWarnings("WeakerAccess")
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

    // COLOR

    @Override
    public int getColor(int id) throws NotFoundException {
        return getColorInternal(id, null);
    }

    @Override
    public int getColor(int id, @Nullable Theme theme) throws NotFoundException {
        return getColorInternal(id, theme);
    }

    private int getColorInternal(int id, @Nullable Theme theme) throws NotFoundException {
        if (isDarkMode) {
            int requestedColor = resolveColor(id, theme);
            return ColorDesaturationUtils.desaturate(requestedColor, desaturationAmount, minDesaturation);
        }
        return resolveColor(id, theme);
    }

    @SuppressWarnings("deprecation")
    private int resolveColor(int id, @Nullable Theme theme) {
        if (Build.VERSION.SDK_INT >= 23) {
            return super.getColor(id, theme);
        }
        return super.getColor(id);
    }

    // COLOR STATE LIST

    @NonNull
    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        return getColorStateListInternal(id, null);
    }

    @NonNull
    @Override
    public ColorStateList getColorStateList(int id, @Nullable Theme theme) throws NotFoundException {
        return getColorStateListInternal(id, theme);
    }

    private ColorStateList getColorStateListInternal(int id, @Nullable Theme theme) throws NotFoundException {
        if (!isDarkMode) {
            return resolveColorStateList(id, theme);
        }
        final ColorStateList requestedColor = resolveColorStateList(id, theme);

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

    @SuppressWarnings("deprecation")
    private ColorStateList resolveColorStateList(int id, @Nullable Theme theme) {
        if (Build.VERSION.SDK_INT >= 23) {
            return super.getColorStateList(id, theme);
        }
        return super.getColorStateList(id);
    }

    // REFLECTION

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
