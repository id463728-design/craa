package com.example

import androidx.compose.ui.graphics.Color

enum class GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class PowerUpType {
    SHIELD,
    BOOST,
    COIN
}

enum class CarType {
    SEDAN,
    SPORTS,
    TRUCK,
    POLICE
}

enum class SceneryType {
    CITY_NEON,
    MOUNTAIN_HIGHWAY,
    COUNTRYSIDE_FOREST,
    CYBER_DESERT,
    RAINY_STORM
}

data class PlayerCar(
    var lane: Int = 3, // Current lane (0 to 5)
    var xPercent: Float = 0.5f, // Current horizontal visual percent (0.0 to 1.0)
    var targetXPercent: Float = 0.5f, // Target horizontal visual percent (for smooth sliding)
    var shieldTimeLeftMs: Long = 0, // In milliseconds
    var boostTimeLeftMs: Long = 0, // In milliseconds
    var color: Color = Color(0xFFFFD54F), // Vibrant yellow sports car
    var type: CarType = CarType.SPORTS,
    var underglowColor: Color = Color.Transparent, // Active neon underglow
    var decalType: String = "none", // none, stripe, flame, carbon
    var rimType: String = "standard" // standard, gold, red_line, chrome
) {
    val hasShield: Boolean get() = shieldTimeLeftMs > 0
    val hasBoost: Boolean get() = boostTimeLeftMs > 0
}

data class Obstacle(
    val id: Long,
    var lane: Int,
    var yPercent: Float, // Visual vertical percent (-0.2f to 1.2f)
    val speedPercentPerSec: Float, // Vertical speed
    val color: Color,
    val type: CarType,
    val isOncoming: Boolean, // True if moving downward in lanes 0-2
    val isOvertaker: Boolean, // True if moving upward from bottom in lanes 3-5
    var hasBeenDodged: Boolean = false // Track if passed the player to award score points
)

data class PowerUp(
    val id: Long,
    val lane: Int,
    var yPercent: Float, // Visual vertical percent (-0.2f to 1.2f)
    val type: PowerUpType,
    val speedPercentPerSec: Float // Moves down with the scrolling road
)

data class Particle(
    var xPercent: Float,
    var yPercent: Float,
    val vxPercent: Float,
    val vyPercent: Float,
    var life: Float, // 1.0f down to 0.0f
    val decay: Float,
    val color: Color,
    val sizeDp: Float,
    val text: String? = null // Supporting floating bonus text particles (e.g. "+150 DODGE!")
)

