package com.rokid.cxrmsamples.dataBeans.selfView

class ImageViewProps {
    var id: String = ""
        set(value) {
            field = if (value.matches(Regex("[a-zA-Z0-9_]+"))) {
                value
            } else {
                throw IllegalArgumentException("id must be a valid identifier")
            }
        }
    var layout_width: String = "match_parent"
        set(value) {
            field =
                if (value == "match_parent" || value == "wrap_content" || value.endsWith("dp")) {
                    value
                } else if (value.matches(Regex("\\d+"))) {
                    // If only a number is provided, automatically append the dp unit
                    "${value}dp"
                } else {
                    throw IllegalArgumentException("layout_width must be 'match_parent', 'wrap_content', or a value ending with 'dp'")
                }
        }
    var layout_height: String = "wrap_content"
        set(value) {
            field =
                if (value == "match_parent" || value == "wrap_content" || value.endsWith("dp")) {
                    value
                } else if (value.matches(Regex("\\d+"))) {
                    // If only a number is provided, automatically append the dp unit
                    "${value}dp"
                } else {
                    throw IllegalArgumentException("layout_height must be 'match_parent', 'wrap_content', or a value ending with 'dp'")
                }
        }
    var name: String = "NONE"

    var scaleType: String = "center"
        set(value) {
            field = when (value) {
                "center", "center_crop", "center_inside", "fit_center", "fit_end", "fit_start", "fit_xy", "matrix" -> value
                else -> throw IllegalArgumentException("scaleType must be one of center, center_crop, center_inside, fit_center, fit_end, fit_start, fit_xy, matrix")
            }
        }

    /**
     * Process color value: if the input is a valid color value, keep only the green channel.
     */
    private fun processColorValue(color: String): String {
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
        // If it is not a valid color format, just return the original value
        else {
            return color
        }
    }

    fun toJson(): String {
        return if (id.isEmpty()) {
            throw IllegalArgumentException("id can not be empty")
        } else {
            val sb: StringBuilder = StringBuilder("{")
                .append("\"id\":\"$id\"")
                .append(",\"layout_width\":\"$layout_width\"")
                .append(",\"layout_height\":\"$layout_height\"")
                .append(",\"name\":\"$name\"")
                .append(",\"scaleType\":\"$scaleType\"")
            sb.append("}").toString()
        }
    }
}
