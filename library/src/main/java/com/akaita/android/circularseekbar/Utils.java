package com.akaita.android.circularseekbar;

import android.content.res.Resources;
import android.util.DisplayMetrics;

class Utils {

    private Utils() {}

    /**
     * This method converts dp unit to equivalent pixels, depending on
     * device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we
     *           need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on
     * device density
     */
    static float convertDpToPixel(Resources r, float dp) {
        DisplayMetrics metrics = r.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }
}
