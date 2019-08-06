package dev.olog.lib;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;

public class ColorDesaturationUtils {

    public static int desaturate(@ColorInt int color, float amount, float minDesaturation) {
        if (color == Color.TRANSPARENT) {
            // can't desaturate transparent color
            return color;
        }
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        if (hsl[1] > minDesaturation) {
            hsl[1] = MathUtils.clamp(
                    hsl[1] - amount,
                    minDesaturation,
                    1f
            );
        }
        return ColorUtils.HSLToColor(hsl);
    }

}
