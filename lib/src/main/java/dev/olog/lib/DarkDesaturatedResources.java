package dev.olog.lib;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.util.SparseArray;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * This class desaturates colors.
 * See <a href="https://it.wikipedia.org/wiki/Hue_Saturation_Brightness">HSL</a> for more information.
 */
public class DarkDesaturatedResources extends Resources {

    private static final float DEFAULT_DESATURATION_AMOUNT = 0.25f;
    private static final float DEFAULT_DESATURATION_THRESHOLD = 0.75f;

    private final boolean isDarkMode;
    private final float desaturationAmount;
    private final float desaturationThreshold;
    private SparseArray<WeakReference<ColorStateList>> cache = new SparseArray<>();

    /**
     * @param isDarkMode when true, the colors can be desaturated
     * @param desaturationAmount the amount of saturation removed from the colors
     * @param desaturationThreshold colors with a saturation above this, will be desaturated. This also
     *                              represent the desaturation lower bound.
     */
    public DarkDesaturatedResources(
            boolean isDarkMode,
            @FloatRange(from = 0, to = 1) float desaturationAmount,
            @FloatRange(from = 0, to = 1) float desaturationThreshold,
            @NonNull Resources resources
    ) {
        super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
        this.isDarkMode = isDarkMode;
        this.desaturationAmount = desaturationAmount;
        this.desaturationThreshold = desaturationThreshold;
    }

    /**
     * Constructor that uses default values for desaturation amount and threshold
     *
     * @param isDarkMode when true, the colors can be desaturated
     */
    @SuppressWarnings("unused")
    public DarkDesaturatedResources(
            boolean isDarkMode,
            @NonNull Resources resources
    ) {
        this(isDarkMode, DEFAULT_DESATURATION_AMOUNT, DEFAULT_DESATURATION_THRESHOLD, resources);
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
            return ColorDesaturationUtils.desaturate(requestedColor, desaturationAmount, desaturationThreshold);
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

        final WeakReference<ColorStateList> cachedRef = cache.get(id);

        if (cachedRef != null) {
            final ColorStateList colorStateList = cachedRef.get();
            if (colorStateList != null){
                return colorStateList;
            }
        }

        final ColorStateList requestedColor = resolveColorStateList(id, theme);

        try {
            final ColorStateList desaturated = new ColorStateList(
                    getStates(requestedColor),
                    desaturateGroup(getColors(requestedColor))
            );
            cache.append(id, new WeakReference<>(desaturated));
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
            colors[i] = ColorDesaturationUtils.desaturate(colors[i], desaturationAmount, desaturationThreshold);
        }
        return colors;
    }

}
