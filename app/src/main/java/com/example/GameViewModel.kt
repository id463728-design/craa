package com.example

import android.app.Application
import android.content.Context
import android.view.Choreographer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("endless_car_game_prefs", Context.MODE_PRIVATE)

    // Game states
    var gameState by mutableStateOf(GameState.MENU)
        private set

    var score by mutableStateOf(0)
        private set

    var highScore by mutableStateOf(prefs.getInt("high_score", 0))
        private set

    var coinsCollected by mutableStateOf(0)
        private set

    var totalCoins by mutableStateOf(prefs.getInt("total_coins", 0))
        private set

    var selectedCarType by mutableStateOf(CarType.SPORTS)
    var selectedCarColor by mutableStateOf(Color(0xFFFFD54F)) // Default sports car yellow

    // Garage Selection & Custom Mod Shop states
    var selectedCarColorId by mutableStateOf(prefs.getString("selected_color_id", "sports_yellow") ?: "sports_yellow")
    var unlockedColors by mutableStateOf(prefs.getString("unlocked_colors_list", "sports_yellow,police_blue,truck_orange,sedan_green")?.split(",")?.toSet() ?: setOf("sports_yellow", "police_blue", "truck_orange", "sedan_green"))
    var unlockedMods by mutableStateOf(prefs.getString("unlocked_mods_list", "none")?.split(",")?.toSet() ?: setOf("none"))

    var activeUnderglow by mutableStateOf(prefs.getString("active_underglow", "none") ?: "none")
    var activeDecal by mutableStateOf(prefs.getString("active_decal", "none") ?: "none")
    var activeRims by mutableStateOf(prefs.getString("active_rims", "standard") ?: "standard")

    // Active environment
    var currentScenery by mutableStateOf(SceneryType.CITY_NEON)
        private set

    // Player State
    var player by mutableStateOf(PlayerCar())
        private set

    // Game lists
    val obstacles = mutableStateListOf<Obstacle>()
    val powerUps = mutableStateListOf<PowerUp>()
    val particles = mutableStateListOf<Particle>()

    // Game speed and difficulty variables
    var baseScrollSpeed = 0.35f // road screen height percentage scrolled per second
    var currentSpeedKmh by mutableStateOf(100)
        private set

    private var gameLoopJob: Job? = null
    private var lastUpdateTime = 0L
    private var lastSpawnTime = 0L
    private var nextSpawnIntervalMs = 1200L
    private var idCounter = 0L

    // For road line scrolling animation
    var roadScrollOffset by mutableStateOf(0f)
        private set

    init {
        // Load correct active color from store
        equipColor(selectedCarColorId)
        resetGame()
    }

    fun selectVehicle(type: CarType, color: Color) {
        selectedCarType = type
        resetGame()
        SoundManager.playMenuClick()
    }

    // ==========================================
    // SHOP AND PERSISTENCE FUNCTIONS
    // ==========================================

    fun buyColor(colorId: String, cost: Int) {
        if (colorId in unlockedColors) return
        if (totalCoins >= cost) {
            totalCoins -= cost
            val newSet = unlockedColors + colorId
            unlockedColors = newSet
            prefs.edit()
                .putInt("total_coins", totalCoins)
                .putString("unlocked_colors_list", newSet.joinToString(","))
                .apply()
            SoundManager.playShield() // Play satisfying sound effect
        }
    }

    fun equipColor(colorId: String) {
        if (colorId in unlockedColors) {
            selectedCarColorId = colorId
            selectedCarColor = when (colorId) {
                "sports_yellow" -> Color(0xFFFFD54F)
                "police_blue" -> Color(0xFF64B5F6)
                "truck_orange" -> Color(0xFFFF8A65)
                "sedan_green" -> Color(0xFF81C784)
                "hot_pink" -> Color(0xFFFF4081)
                "neon_cyan" -> Color(0xFF00E5FF)
                "sunset_purple" -> Color(0xFF7C4DFF)
                "gold_edition" -> Color(0xFFFFD700)
                else -> Color(0xFFFFD54F)
            }
            prefs.edit().putString("selected_color_id", colorId).apply()
            resetGame()
            SoundManager.playMenuClick()
        }
    }

    fun buyModification(category: String, modId: String, cost: Int) {
        if (modId in unlockedMods) return
        if (totalCoins >= cost) {
            totalCoins -= cost
            val newSet = unlockedMods + modId
            unlockedMods = newSet
            prefs.edit()
                .putInt("total_coins", totalCoins)
                .putString("unlocked_mods_list", newSet.joinToString(","))
                .apply()
            SoundManager.playShield()
        }
    }

    fun equipModification(category: String, modId: String) {
        if (modId in unlockedMods) {
            when (category) {
                "underglow" -> {
                    activeUnderglow = modId
                    prefs.edit().putString("active_underglow", modId).apply()
                }
                "decal" -> {
                    activeDecal = modId
                    prefs.edit().putString("active_decal", modId).apply()
                }
                "rims" -> {
                    activeRims = modId
                    prefs.edit().putString("active_rims", modId).apply()
                }
            }
            resetGame()
            SoundManager.playMenuClick()
        }
    }

    // ==========================================
    // CORE GAME LOGIC
    // ==========================================

    fun startGame() {
        resetGame()
        gameState = GameState.PLAYING
        lastUpdateTime = System.currentTimeMillis()
        lastSpawnTime = System.currentTimeMillis()
        SoundManager.playMenuClick()
        runGameLoop()
    }

    fun pauseGame() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED
            gameLoopJob?.cancel()
            SoundManager.playMenuClick()
        }
    }

    fun resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING
            lastUpdateTime = System.currentTimeMillis()
            lastSpawnTime = System.currentTimeMillis()
            SoundManager.playMenuClick()
            runGameLoop()
        }
    }

    fun returnToMenu() {
        gameState = GameState.MENU
        gameLoopJob?.cancel()
        resetGame()
        SoundManager.playMenuClick()
    }

    fun resetGame() {
        score = 0
        coinsCollected = 0
        obstacles.clear()
        powerUps.clear()
        particles.clear()
        baseScrollSpeed = 0.35f
        currentSpeedKmh = 100
        roadScrollOffset = 0f
        currentScenery = SceneryType.CITY_NEON

        val initialShield = if (selectedCarType == CarType.TRUCK) 5000L else 0L // Truck starts with 5s shield

        val underglowColor = when (activeUnderglow) {
            "glow_cyan" -> Color(0xFF00E5FF)
            "glow_red" -> Color(0xFFFF1744)
            "glow_green" -> Color(0xFF00E676)
            else -> Color.Transparent
        }

        player = PlayerCar(
            lane = 3,
            xPercent = getLaneCenterX(3),
            targetXPercent = getLaneCenterX(3),
            shieldTimeLeftMs = initialShield,
            boostTimeLeftMs = 0,
            color = selectedCarColor,
            type = selectedCarType,
            underglowColor = underglowColor,
            decalType = activeDecal,
            rimType = activeRims
        )
    }

    fun triggerMoveLeft() {
        if (gameState != GameState.PLAYING) return
        if (player.lane > 0) {
            player.lane--
            player.targetXPercent = getLaneCenterX(player.lane)
            SoundManager.playMenuClick()
        }
    }

    fun triggerMoveRight() {
        if (gameState != GameState.PLAYING) return
        if (player.lane < 5) {
            player.lane++
            player.targetXPercent = getLaneCenterX(player.lane)
            SoundManager.playMenuClick()
        }
    }

    fun triggerMoveToLane(targetLane: Int) {
        if (gameState != GameState.PLAYING) return
        if (targetLane in 0..5 && targetLane != player.lane) {
            player.lane = targetLane
            player.targetXPercent = getLaneCenterX(targetLane)
            SoundManager.playMenuClick()
        }
    }

    fun dragPlayer(deltaXPercent: Float) {
        if (gameState != GameState.PLAYING) return
        val newX = (player.xPercent + deltaXPercent).coerceIn(0.05f, 0.95f)
        player.xPercent = newX
        player.targetXPercent = newX
        // Infer lane based on horizontal position
        val targetLane = (newX * 6).toInt().coerceIn(0, 5)
        if (targetLane != player.lane) {
            player.lane = targetLane
        }
    }

    private fun getLaneCenterX(lane: Int): Float {
        return (lane + 0.5f) / 6f
    }

    private fun runGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Main) {
            lastUpdateTime = System.currentTimeMillis()
            val choreographer = Choreographer.getInstance()
            val frameCallback = object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    if (gameState != GameState.PLAYING) return
                    val now = System.currentTimeMillis()
                    val deltaMs = now - lastUpdateTime
                    lastUpdateTime = now
                    updatePhysics(deltaMs)
                    if (gameState == GameState.PLAYING) {
                        choreographer.postFrameCallback(this)
                    }
                }
            }
            choreographer.postFrameCallback(frameCallback)
        }
    }

    private fun updatePhysics(deltaMs: Long) {
        val dt = deltaMs / 1000f
        if (dt <= 0 || dt > 0.5f) return

        // 1. Progress score based on speed
        val speedFactor = if (player.hasBoost) 2.5f else 1.0f
        val currentScrollSpeed = baseScrollSpeed * speedFactor
        val scoreIncrement = (dt * 100 * speedFactor).toInt()
        score += scoreIncrement

        // Update speedometer
        currentSpeedKmh = (100 + (score / 120) + (if (player.hasBoost) 150 else 0)).coerceIn(100, 320)

        // Difficulty scaling (speed increases as you go further)
        baseScrollSpeed = 0.35f + (score / 4000) * 0.03f

        // Scenery transitions based on score (traveled distance)
        currentScenery = when {
            score < 1500 -> SceneryType.CITY_NEON
            score < 3500 -> SceneryType.MOUNTAIN_HIGHWAY
            score < 6000 -> SceneryType.COUNTRYSIDE_FOREST
            score < 9000 -> SceneryType.CYBER_DESERT
            else -> SceneryType.RAINY_STORM
        }

        // Update road animation scroll
        roadScrollOffset = (roadScrollOffset + dt * currentScrollSpeed * 1.5f) % 1.0f

        // 2. Interpolate Player Car position smoothly for tapping controls
        val turnSpeed = if (player.type == CarType.SPORTS) 12f else 8f
        player = player.copy(
            xPercent = player.xPercent + (player.targetXPercent - player.xPercent) * (turnSpeed * dt).coerceIn(0f, 1f),
            shieldTimeLeftMs = (player.shieldTimeLeftMs - deltaMs).coerceAtLeast(0),
            boostTimeLeftMs = (player.boostTimeLeftMs - deltaMs).coerceAtLeast(0)
        )

        // 3. Update Particles
        val particleIterator = particles.iterator()
        while (particleIterator.hasNext()) {
            val p = particleIterator.next()
            p.xPercent += p.vxPercent * dt
            p.yPercent += p.vyPercent * dt
            p.life -= p.decay * dt
            if (p.life <= 0) {
                particleIterator.remove()
            }
        }

        // Spawn scenery environmental effects
        spawnSceneryParticles()

        // Add jet stream particles behind player if boosting or driving
        if (player.hasBoost && Random.nextFloat() < 0.6f) {
            spawnBoostParticles()
        }

        // 4. Update Obstacles
        val obstacleIterator = obstacles.iterator()
        while (obstacleIterator.hasNext()) {
            val obs = obstacleIterator.next()

            // Calculate obstacle speed relative to scrolling screen
            val obstacleRelativeSpeed = if (obs.isOncoming) {
                // Oncoming traffic flows opposite way (downwards extra fast!)
                currentScrollSpeed + obs.speedPercentPerSec
            } else if (obs.isOvertaker) {
                // Overtaker flows from bottom-to-top, catches up to player
                currentScrollSpeed - obs.speedPercentPerSec
            } else {
                // Same direction slow traffic moves downwards slower than scroll
                currentScrollSpeed - obs.speedPercentPerSec
            }

            obs.yPercent += obstacleRelativeSpeed * dt

            // Check collision with player
            if (checkCollision(player.xPercent, 0.75f, 0.08f, 0.12f, getLaneCenterX(obs.lane), obs.yPercent, 0.08f, 0.11f)) {
                if (player.hasBoost) {
                    // Instantly obliterate oncoming car!
                    spawnExplosion(getLaneCenterX(obs.lane), obs.yPercent, obs.color)
                    SoundManager.playCrash()
                    obstacleIterator.remove()
                    score += 500 // Bonus points for crushing obstacle during boost!
                    
                    // Spawn bonus score text
                    particles.add(
                        Particle(
                            xPercent = getLaneCenterX(obs.lane),
                            yPercent = obs.yPercent,
                            vxPercent = 0f,
                            vyPercent = -0.3f,
                            life = 1.0f,
                            decay = 1.0f,
                            color = Color(0xFFFFD54F),
                            sizeDp = 15f,
                            text = "+500 BOOST CRUSH!"
                        )
                    )
                    continue
                } else if (player.hasShield) {
                    // Pop shield, blow up obstacle
                    player = player.copy(shieldTimeLeftMs = 0)
                    spawnExplosion(getLaneCenterX(obs.lane), obs.yPercent, obs.color)
                    SoundManager.playCrash()
                    obstacleIterator.remove()
                    continue
                } else {
                    // CRASH! Game Over!
                    triggerGameOver()
                    return
                }
            }

            // DODGE SYSTEM: Check if player successfully passed/dodged the obstacle!
            // Obstacle is past player (yPercent > 0.75f) and hasn't been credited yet
            if (!obs.hasBeenDodged && obs.yPercent > 0.75f) {
                obs.hasBeenDodged = true
                score += 150 // Dodge bonus
                // Spawn golden "+150 DODGE!" text floating particle
                particles.add(
                    Particle(
                        xPercent = getLaneCenterX(obs.lane),
                        yPercent = 0.73f,
                        vxPercent = 0f,
                        vyPercent = -0.25f,
                        life = 1.2f,
                        decay = 0.8f,
                        color = Color(0xFFFFD54F),
                        sizeDp = 14f,
                        text = "+150 DODGE!"
                    )
                )
            }

            // Remove if off screen
            if (obs.yPercent > 1.3f || obs.yPercent < -0.3f) {
                obstacleIterator.remove()
            }
        }

        // 5. Update Power-ups
        val powerUpIterator = powerUps.iterator()
        while (powerUpIterator.hasNext()) {
            val pu = powerUpIterator.next()
            pu.yPercent += (currentScrollSpeed) * dt // Power-ups sit on road, moving down at scroll speed

            // Check collision
            if (checkCollision(player.xPercent, 0.75f, 0.08f, 0.12f, getLaneCenterX(pu.lane), pu.yPercent, 0.08f, 0.08f)) {
                activatePowerUp(pu.type)
                powerUpIterator.remove()
                continue
            }

            if (pu.yPercent > 1.3f) {
                powerUpIterator.remove()
            }
        }

        // 6. Spawn Manager
        val timeSinceLastSpawn = System.currentTimeMillis() - lastSpawnTime
        if (timeSinceLastSpawn > nextSpawnIntervalMs) {
            spawnSomething()
            lastSpawnTime = System.currentTimeMillis()
            // Randomise next spawn window based on speed (gets faster as score increases!)
            val minWait = (600 - (score / 100).coerceAtMost(300)).toLong().coerceAtLeast(350L)
            val maxWait = (1300 - (score / 100).coerceAtMost(600)).toLong().coerceAtLeast(600L)
            nextSpawnIntervalMs = Random.nextLong(minWait, maxWait)
        }
    }

    private fun spawnSceneryParticles() {
        if (Random.nextFloat() > 0.18f) return

        when (currentScenery) {
            SceneryType.CITY_NEON -> {
                // Magenta or Cyan sparkles floating up along borders
                val isLeft = Random.nextBoolean()
                val x = if (isLeft) Random.nextFloat() * 0.07f else 0.93f + Random.nextFloat() * 0.07f
                particles.add(
                    Particle(
                        xPercent = x,
                        yPercent = 1.05f,
                        vxPercent = (Random.nextFloat() - 0.5f) * 0.03f,
                        vyPercent = -0.15f - Random.nextFloat() * 0.1f,
                        life = 1.0f,
                        decay = 0.4f,
                        color = if (Random.nextBoolean()) Color(0xFF00E5FF) else Color(0xFFFF4081),
                        sizeDp = Random.nextFloat() * 4f + 2f
                    )
                )
            }
            SceneryType.MOUNTAIN_HIGHWAY -> {
                // Green leaves falling
                particles.add(
                    Particle(
                        xPercent = Random.nextFloat() * 0.9f,
                        yPercent = -0.05f,
                        vxPercent = 0.05f + Random.nextFloat() * 0.05f,
                        vyPercent = 0.15f + Random.nextFloat() * 0.1f,
                        life = 1.2f,
                        decay = 0.3f,
                        color = Color(0xFFA5D6A7),
                        sizeDp = Random.nextFloat() * 5f + 3f
                    )
                )
            }
            SceneryType.COUNTRYSIDE_FOREST -> {
                // Orange autumn leaves drifting and spinning
                particles.add(
                    Particle(
                        xPercent = Random.nextFloat(),
                        yPercent = -0.05f,
                        vxPercent = (Random.nextFloat() - 0.4f) * 0.08f,
                        vyPercent = 0.18f + Random.nextFloat() * 0.1f,
                        life = 1.2f,
                        decay = 0.25f,
                        color = if (Random.nextBoolean()) Color(0xFFFFB74D) else Color(0xFF8D6E63),
                        sizeDp = Random.nextFloat() * 6f + 3f
                    )
                )
            }
            SceneryType.CYBER_DESERT -> {
                // Sand dust blowing horizontally across the entire screen
                particles.add(
                    Particle(
                        xPercent = Random.nextFloat() * 0.3f,
                        yPercent = Random.nextFloat() * 0.9f,
                        vxPercent = 0.5f + Random.nextFloat() * 0.3f,
                        vyPercent = 0.1f + Random.nextFloat() * 0.1f,
                        life = 1.0f,
                        decay = 0.7f,
                        color = Color(0xFFFFCC80).copy(alpha = 0.4f),
                        sizeDp = Random.nextFloat() * 5f + 2f
                    )
                )
            }
            SceneryType.RAINY_STORM -> {
                // Rapid dark cyan/sky blue rain streaks slashing down
                particles.add(
                    Particle(
                        xPercent = Random.nextFloat(),
                        yPercent = -0.05f,
                        vxPercent = -0.08f,
                        vyPercent = 1.3f + Random.nextFloat() * 0.3f,
                        life = 1.0f,
                        decay = 1.0f,
                        color = Color(0xFF4DD0E1).copy(alpha = 0.5f),
                        sizeDp = Random.nextFloat() * 2f + 1f
                    )
                )
            }
        }
    }

    private fun checkCollision(
        px: Float, py: Float, pw: Float, ph: Float,
        ox: Float, oy: Float, ow: Float, oh: Float
    ): Boolean {
        // Simple bounding box with standard padding tolerances for fun gameplay
        val playerLeft = px - pw / 2
        val playerRight = px + pw / 2
        val playerTop = py - ph / 2
        val playerBottom = py + ph / 2

        val obstacleLeft = ox - ow / 2
        val obstacleRight = ox + ow / 2
        val obstacleTop = oy - oh / 2
        val obstacleBottom = oy + oh / 2

        return playerLeft < obstacleRight &&
               playerRight > obstacleLeft &&
               playerTop < obstacleBottom &&
               playerBottom > obstacleTop
    }

    private fun activatePowerUp(type: PowerUpType) {
        val px = player.xPercent
        val py = 0.75f
        when (type) {
            PowerUpType.SHIELD -> {
                player = player.copy(shieldTimeLeftMs = 8000L) // 8 seconds shield
                score += 500 // Bonus score for powerup
                SoundManager.playShield()
                spawnPowerUpParticles(PowerUpType.SHIELD)

                // Floating score particle
                particles.add(
                    Particle(
                        xPercent = px,
                        yPercent = py - 0.05f,
                        vxPercent = 0f,
                        vyPercent = -0.2f,
                        life = 1.2f,
                        decay = 0.8f,
                        color = Color(0xFF29B6F6),
                        sizeDp = 14f,
                        text = "+500 SHIELD"
                    )
                )
            }
            PowerUpType.BOOST -> {
                player = player.copy(boostTimeLeftMs = 5000L) // 5 seconds boost
                score += 500 // Bonus score for powerup
                SoundManager.playBoost()
                spawnPowerUpParticles(PowerUpType.BOOST)

                // Floating score particle
                particles.add(
                    Particle(
                        xPercent = px,
                        yPercent = py - 0.05f,
                        vxPercent = 0f,
                        vyPercent = -0.2f,
                        life = 1.2f,
                        decay = 0.8f,
                        color = Color(0xFFFF7043),
                        sizeDp = 14f,
                        text = "+500 SPEED BOOST"
                    )
                )
            }
            PowerUpType.COIN -> {
                coinsCollected++
                totalCoins++
                score += 300 // Score bonus
                // Save total coins persistently
                prefs.edit().putInt("total_coins", totalCoins).apply()
                SoundManager.playCoin()
                spawnPowerUpParticles(PowerUpType.COIN)

                // Floating score particle
                particles.add(
                    Particle(
                        xPercent = px,
                        yPercent = py - 0.05f,
                        vxPercent = 0f,
                        vyPercent = -0.2f,
                        life = 1.2f,
                        decay = 0.8f,
                        color = Color(0xFFFFD54F),
                        sizeDp = 13f,
                        text = "+300 COIN"
                    )
                )
            }
        }
    }

    private fun spawnSomething() {
        val lanesAvailable = (0..5).shuffled()
        val spawnTypeChance = Random.nextFloat()

        if (spawnTypeChance < 0.18f) {
            // Spawn PowerUp (Shield, Boost, or Coin)
            val lane = lanesAvailable.first()
            val type = when {
                Random.nextFloat() < 0.22f -> PowerUpType.BOOST
                Random.nextFloat() < 0.44f -> PowerUpType.SHIELD
                else -> PowerUpType.COIN
            }
            powerUps.add(
                PowerUp(
                    id = idCounter++,
                    lane = lane,
                    yPercent = -0.1f,
                    type = type,
                    speedPercentPerSec = baseScrollSpeed
                )
            )
        } else {
            // Spawn an Obstacle Car!
            val recentObstaclesCount = obstacles.count { it.yPercent < 0.3f }
            if (recentObstaclesCount >= 3) return // Stagger spawns to guarantee a free lane!

            val numToSpawn = if (Random.nextFloat() < 0.3f && recentObstaclesCount < 2) 2 else 1
            for (i in 0 until numToSpawn) {
                if (i >= lanesAvailable.size) break
                val lane = lanesAvailable[i]

                val isOncoming = lane in 0..2
                val isOvertaker = !isOncoming && Random.nextFloat() < 0.35f

                val startY = if (isOvertaker) 1.2f else -0.15f
                val carSpeed = if (isOncoming) {
                    Random.nextFloat() * 0.15f + 0.25f // Fast oncoming cars
                } else if (isOvertaker) {
                    Random.nextFloat() * 0.15f + 0.45f // Overtaker zooms up from bottom
                } else {
                    Random.nextFloat() * 0.08f + 0.10f // Slower same-direction cars (player overtakes them)
                }

                val obstacleColor = when (Random.nextInt(6)) {
                    0 -> Color(0xFFE57373) // Pastel Red
                    1 -> Color(0xFF64B5F6) // Pastel Blue
                    2 -> Color(0xFF81C784) // Pastel Green
                    3 -> Color(0xFFBA68C8) // Pastel Purple
                    4 -> Color(0xFFFFB74D) // Pastel Orange
                    else -> Color(0xFF90A4AE) // Sleek Silver/Gray
                }

                val carType = when (Random.nextInt(4)) {
                    0 -> CarType.SEDAN
                    1 -> CarType.SPORTS
                    2 -> CarType.TRUCK
                    else -> CarType.POLICE
                }

                obstacles.add(
                    Obstacle(
                        id = idCounter++,
                        lane = lane,
                        yPercent = startY,
                        speedPercentPerSec = carSpeed,
                        color = obstacleColor,
                        type = carType,
                        isOncoming = isOncoming,
                        isOvertaker = isOvertaker
                    )
                )
            }
        }
    }

    private fun spawnBoostParticles() {
        val playerX = player.xPercent
        val playerY = 0.75f
        // Add fire/smoke exhausting from the bottom-center of the car
        particles.add(
            Particle(
                xPercent = playerX + (Random.nextFloat() - 0.5f) * 0.03f,
                yPercent = playerY + 0.06f,
                vxPercent = (Random.nextFloat() - 0.5f) * 0.1f,
                vyPercent = Random.nextFloat() * 0.3f + 0.5f, // shoot backwards
                life = 1.0f,
                decay = 2.5f,
                color = if (Random.nextBoolean()) Color(0xFFFF5722) else Color(0xFFFFC107), // orange/yellow
                sizeDp = Random.nextFloat() * 8f + 4f
            )
        )
    }

    private fun spawnPowerUpParticles(type: PowerUpType) {
        val pColor = when (type) {
            PowerUpType.SHIELD -> Color(0xFF29B6F6) // Cyan glow
            PowerUpType.BOOST -> Color(0xFFFF7043) // Red-orange burst
            PowerUpType.COIN -> Color(0xFFFFD54F) // Gold spark
        }
        val px = player.xPercent
        val py = 0.75f

        for (i in 0 until 15) {
            val angle = Random.nextFloat() * 2 * Math.PI
            val speed = Random.nextFloat() * 0.3f + 0.1f
            particles.add(
                Particle(
                    xPercent = px,
                    yPercent = py,
                    vxPercent = (cos(angle) * speed).toFloat(),
                    vyPercent = (sin(angle) * speed).toFloat(),
                    life = 1.0f,
                    decay = 2.0f,
                    color = pColor,
                    sizeDp = Random.nextFloat() * 6f + 3f
                )
            )
        }
    }

    private fun spawnExplosion(x: Float, y: Float, color: Color) {
        // Massive burst of colorful parts and grey smoke
        for (i in 0 until 25) {
            val angle = Random.nextFloat() * 2 * Math.PI
            val speed = Random.nextFloat() * 0.4f + 0.1f
            particles.add(
                Particle(
                    xPercent = x,
                    yPercent = y,
                    vxPercent = (cos(angle) * speed).toFloat(),
                    vyPercent = (sin(angle) * speed).toFloat(),
                    life = 1.0f,
                    decay = 1.5f + Random.nextFloat(),
                    color = if (Random.nextFloat() < 0.6f) color else Color(0xFF757575), // mixture of car color and grey smoke
                    sizeDp = Random.nextFloat() * 10f + 5f
                )
            )
        }
    }

    private fun triggerGameOver() {
        gameState = GameState.GAME_OVER
        gameLoopJob?.cancel()
        SoundManager.playCrash()

        // Spawn a massive crash explosion
        spawnExplosion(player.xPercent, 0.75f, player.color)

        // Save High Score
        if (score > highScore) {
            highScore = score
            prefs.edit().putInt("high_score", highScore).apply()
        }
    }

    // Simple helpers for trig functions without using full double math overhead
    private fun cos(radians: Double): Double = Math.cos(radians)
    private fun sin(radians: Double): Double = Math.sin(radians)
}
