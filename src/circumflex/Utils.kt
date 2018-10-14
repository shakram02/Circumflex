package circumflex

import java.net.URL
import java.nio.file.Paths

public fun URL.getFileName(): String {
    return Paths.get(this.path).fileName.toString()
}

fun Number.toPercentage(): Double {
    return (this.toDouble() * 100).roundToPlaces(2)
}

fun Double.roundToPlaces(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return Math.round(this * multiplier) / multiplier
}