package dev.olog.lib;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;

@SuppressWarnings("WeakerAccess")
public class ColorDesaturationUtils {

    public static int desaturate(@ColorInt int color, float amount, float minDesaturation) {
        int originalAlpha = Color.alpha(color);

        if (color == Color.TRANSPARENT || originalAlpha == 0) {
            // can't desaturate transparent color
            return color;
        }

        int colorWithFullAlpha = ColorUtils.setAlphaComponent(color, 255);

        float[] hsl = new float[3];
        ColorUtils.colorToHSL(colorWithFullAlpha, hsl);
        if (hsl[1] > minDesaturation) {
            hsl[1] = MathUtils.clamp(
                    hsl[1] - amount,
                    minDesaturation,
                    1f
            );
        }
        int desaturatedColorWithFullAlpha = ColorUtils.HSLToColor(hsl);
        //noinspection UnnecessaryLocalVariable
        int desaturatedColorWithOriginalAlpha = ColorUtils.setAlphaComponent(
                desaturatedColorWithFullAlpha,
                originalAlpha
        );
        return desaturatedColorWithOriginalAlpha;
    }

}
