package com.rokid.cxrmsamples.dataBeans.selfView

/**
 * Process color value: if the input is a valid color value, keep only the green channel.
 */
fun processColorValue(color: String): String {
    // Check whether it is a valid ARGB format (#AARRGGBB)
    if (color.matches(Regex("#[0-9a-fA-F]{8}"))) {
        // Parse ARGB channel values
        val alpha = color.substring(1, 3)
        val red = color.substring(3, 5)
        val green = color.substring(5, 7)
        val blue = color.substring(7, 9)

        // Return a color that keeps only the green channel, with red and blue set to 00
        return "#$alpha${"00"}$green${"00"}"
    }
    // Check whether it is a valid RGB format (#RRGGBB)
    else if (color.matches(Regex("#[0-9a-fA-F]{6}"))) {
        // Parse RGB channel values
        val red = color.substring(1, 3)
        val green = color.substring(3, 5)
        val blue = color.substring(5, 7)

        // Return a color that keeps only the green channel, with red and blue set to 00
        return "#FF${"00"}$green${"00"}"
    }
    // If it is not a valid color format, throw an exception
    else {
        throw IllegalArgumentException("Invalid color format: $color")
    }
}
