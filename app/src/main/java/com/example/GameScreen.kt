package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun GameScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505)) // Immersive black background
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        when (viewModel.gameState) {
            GameState.MENU -> {
                MainMenuScreen(viewModel)
            }
            GameState.PLAYING, GameState.PAUSED, GameState.GAME_OVER -> {
                GamePlayScreen(viewModel)

                if (viewModel.gameState == GameState.PAUSED) {
                    PauseOverlay(viewModel)
                }

                if (viewModel.gameState == GameState.GAME_OVER) {
                    GameOverOverlay(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen(viewModel: GameViewModel) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Garage, 1 = Custom Mod Shop

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game Logo / Immersive Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF), // Cyber Cyan Glow
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ENDLESS HIGHWAY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.testTag("game_logo")
                )
            }
            Text(
                text = "IMMERSIVE CYBER TUNER",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFFFF7043),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            )
        }

        // Coins & High Score bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High Score
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111115)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFF22222A), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "HIGH SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF888899))
                        )
                        Text(
                            text = "${viewModel.highScore}m",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Coins collected / Shop Currency
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111115)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFF22222A), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "TOTAL COINS",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF888899))
                        )
                        Text(
                            text = "${viewModel.totalCoins}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }

        // Tab Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF111115))
                .border(1.dp, Color(0xFF22222A), RoundedCornerShape(12.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { activeTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 0) Color(0xFF1E1E28) else Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = if (activeTab == 0) Color(0xFF00E5FF) else Color(0xFF888899),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GARAGE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = if (activeTab == 0) Color.White else Color(0xFF888899),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Button(
                onClick = { activeTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 1) Color(0xFF1E1E28) else Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Style,
                    contentDescription = null,
                    tint = if (activeTab == 1) Color(0xFFFF4081) else Color(0xFF888899),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TUNING SHOP",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = if (activeTab == 1) Color.White else Color(0xFF888899),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeTab == 0) {
                // Garage View (Select Car Model)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CHOOSE CHASSIS SPEC",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        VehicleSelectionCard(
                            title = "Sports",
                            type = CarType.SPORTS,
                            color = Color(0xFFFFD54F),
                            icon = Icons.Default.ElectricCar,
                            description = "Drift Steering / Fast Lane Slide",
                            isSelected = viewModel.selectedCarType == CarType.SPORTS,
                            onClick = { viewModel.selectVehicle(CarType.SPORTS, Color(0xFFFFD54F)) },
                            modifier = Modifier.weight(1f)
                        )
                        VehicleSelectionCard(
                            title = "Police",
                            type = CarType.POLICE,
                            color = Color(0xFF64B5F6),
                            icon = Icons.Default.LocalPolice,
                            description = "Alternate Beacon warning lights",
                            isSelected = viewModel.selectedCarType == CarType.POLICE,
                            onClick = { viewModel.selectVehicle(CarType.POLICE, Color(0xFF64B5F6)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        VehicleSelectionCard(
                            title = "Truck",
                            type = CarType.TRUCK,
                            color = Color(0xFFFF8A65),
                            icon = Icons.Default.LocalShipping,
                            description = "Reinforced flatbed / Shield Start",
                            isSelected = viewModel.selectedCarType == CarType.TRUCK,
                            onClick = { viewModel.selectVehicle(CarType.TRUCK, Color(0xFFFF8A65)) },
                            modifier = Modifier.weight(1f)
                        )
                        VehicleSelectionCard(
                            title = "Sedan",
                            type = CarType.SEDAN,
                            color = Color(0xFF81C784),
                            icon = Icons.Default.TimeToLeave,
                            description = "Classic cruiser / Highly balanced",
                            isSelected = viewModel.selectedCarType == CarType.SEDAN,
                            onClick = { viewModel.selectVehicle(CarType.SEDAN, Color(0xFF81C784)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dynamic Vehicle Stats Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111115)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF22222A), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "CURRENT VEHICLE CONFIG",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFF7043),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Underglow", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF888899)))
                                    Text(viewModel.activeUnderglow.replace("glow_", "").uppercase(), style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                }
                                Column {
                                    Text("Decal Wrap", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF888899)))
                                    Text(viewModel.activeDecal.replace("decal_", "").uppercase(), style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                }
                                Column {
                                    Text("Rims Mod", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF888899)))
                                    Text(viewModel.activeRims.replace("rims_", "").uppercase(), style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            } else {
                // Modification Shop & Custom Tuner (Spend Coins to Unlock modifications!)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // A. Body Paint Section
                    item {
                        Text(
                            text = "CUSTOM CHASSIS PAINTS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color(0xFFFF4081),
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ShopItemCard(
                                title = "Pink Spark",
                                id = "hot_pink",
                                cost = 15,
                                isColor = true,
                                colorSample = Color(0xFFFF4081),
                                isUnlocked = "hot_pink" in viewModel.unlockedColors,
                                isEquipped = viewModel.selectedCarColorId == "hot_pink",
                                totalCoins = viewModel.totalCoins,
                                onBuy = { viewModel.buyColor("hot_pink", 15) },
                                onEquip = { viewModel.equipColor("hot_pink") },
                                modifier = Modifier.weight(1f)
                            )
                            ShopItemCard(
                                title = "Cyber Cyan",
                                id = "neon_cyan",
                                cost = 20,
                                isColor = true,
                                colorSample = Color(0xFF00E5FF),
                                isUnlocked = "neon_cyan" in viewModel.unlockedColors,
                                isEquipped = viewModel.selectedCarColorId == "neon_cyan",
                                totalCoins = viewModel.totalCoins,
                                onBuy = { viewModel.buyColor("neon_cyan", 20) },
                                onEquip = { viewModel.equipColor("neon_cyan") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ShopItemCard(
                                title = "Sunset Purple",
                                id = "sunset_purple",
                                cost = 25,
                                isColor = true,
                                colorSample = Color(0xFF7C4DFF),
                                isUnlocked = "sunset_purple" in viewModel.unlockedColors,
                                isEquipped = viewModel.selectedCarColorId == "sunset_purple",
                                totalCoins = viewModel.totalCoins,
                                onBuy = { viewModel.buyColor("sunset_purple", 25) },
                                onEquip = { viewModel.equipColor("sunset_purple") },
                                modifier = Modifier.weight(1f)
                            )
                            ShopItemCard(
                                title = "Gold Edition",
                                id = "gold_edition",
                                cost = 40,
                                isColor = true,
                                colorSample = Color(0xFFFFD700),
                                isUnlocked = "gold_edition" in viewModel.unlockedColors,
                                isEquipped = viewModel.selectedCarColorId == "gold_edition",
                                totalCoins = viewModel.totalCoins,
                                onBuy = { viewModel.buyColor("gold_edition", 40) },
                                onEquip = { viewModel.equipColor("gold_edition") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // B. Underglow Neon
                    item {
                        Text(
                            text = "NEON UNDERGLOW EFFECTS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ShopItemCard(
                                title = "Ice Cyan Glow",
                                id = "glow_cyan",
                                cost = 20,
                                isColor = false,
                                colorSample = Color(0xFF00E5FF),
                                isUnlocked = "glow_cyan" in viewModel.unlockedMods,
                                isEquipped = viewModel.activeUnderglow == "glow_cyan",
                                totalCoins = viewModel.totalCoins,
                                onBuy = { viewModel.buyModification("underglow", "glow_cyan", 20) },
                                onEquip = { viewModel.equipModification("underglow", "glow_cyan") },
                                modifier = Modifier.weight(1f)
                            )
                            ShopItemCard(
                                title = "Toxic Green",
                                id = "glow_green",
                                cost = 25,
                                isColor = false,
                                colorSample = Color(0xFF00E676),
                                isUnlocked = "glow_green" in viewModel.unlockedMods,
                                isEquipped = viewModel.activeUnderglow == "glow_green",
                                totalCoins = viewModel.totalCoins,
                                onBuy = { viewModel.buyModification("underglow", "glow_green", 25) },
                                onEquip = { viewModel.equipModification("underglow", "glow_green") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ShopItemCard(
                                title = "Fire Red Glow",
                                id = "glow_red",
                                cost = 25,
                                isColor = false,
                                colorSample = Color(0xFFFF1744),
                                isUnlocked = "glow_red" in viewModel.unlockedMods,
                                isEquipped = viewModel.activeUnderglow == "glow_red",
                                totalCoins = viewModel.totalCoins,
                                onBuy = { viewModel.buyModification("underglow", "glow_red", 25) },
                                onEquip = { viewModel.equipModification("underglow", "glow_red") },
                                modifier = Modifier.weight(1f)
                            )
                            // None underglow card to clear it
                            ShopItemCard(
                                title = "Deactivate Glow",
                                id = "none",
                                cost = 0,
                                isColor = false,
                                colorSample = Color.Gray,
                                isUnlocked = true,
                                isEquipped = viewModel.activeUnderglow == "none",
                                totalCoins = viewModel.totalCoins,
                                onBuy = {},
                                onEquip = { viewModel.equipModification("underglow", "none") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // C. Decal Pattern Wraps
                    item {
                        Text(
                            text = "AERO WRAP DECALS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ShopItemCard(
                                title = "Racing Stripes",
                                id = "decal_stripe",
                                cost = 30,
                                isColor = false,
                                colorSample = Color.White,
                                isUnlocked = "decal_stripe" in viewModel.unlockedMods,
                                isEquipped = viewModel.activeDecal == "decal_stripe",
                                totalCoins = viewModel.totalCoins,
                                onBuy = { viewModel.buyModification("decal", "decal_stripe", 30) },
                                onEquip = { viewModel.equipModification("decal", "decal_stripe") },
                                modifier = Modifier.weight(1f)
                            )
                            ShopItemCard(
                                title = "Flame Vinyl",
                                id = "decal_flames",
                                cost = 45,
                                isColor = false,
                                colorSample = Color(0xFFFF3D00),
                                isUnlocked = "decal_flames" in viewModel.unlockedMods,
                                isEquipped = viewModel.activeDecal == "decal_flames",
                                totalCoins = viewModel.totalCoins,
                                onBuy = { viewModel.buyModification("decal", "decal_flames", 45) },
                                onEquip = { viewModel.equipModification("decal", "decal_flames") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ShopItemCard(
                                title = "Standard Solid",
                                id = "none",
                                cost = 0,
                                isColor = false,
                                colorSample = Color.Gray,
                                isUnlocked = true,
                                isEquipped = viewModel.activeDecal == "none",
                                totalCoins = viewModel.totalCoins,
                                onBuy = {},
                                onEquip = { viewModel.equipModification("decal", "none") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // D. Rims
                    item {
                        Text(
                            text = "CUSTOM FORGED RIMS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ShopItemCard(
                                title = "Gold Star Rims",
                                id = "rims_gold",
                                cost = 25,
                                isColor = false,
                                colorSample = Color(0xFFFFD54F),
                                isUnlocked = "rims_gold" in viewModel.unlockedMods,
                                isEquipped = viewModel.activeRims == "rims_gold",
                                totalCoins = viewModel.totalCoins,
                                onBuy = { viewModel.buyModification("rims", "rims_gold", 25) },
                                onEquip = { viewModel.equipModification("rims", "rims_gold") },
                                modifier = Modifier.weight(1f)
                            )
                            ShopItemCard(
                                title = "Carbon Red",
                                id = "rims_red",
                                cost = 25,
                                isColor = false,
                                colorSample = Color(0xFFFF1744),
                                isUnlocked = "rims_red" in viewModel.unlockedMods,
                                isEquipped = viewModel.activeRims == "rims_red",
                                totalCoins = viewModel.totalCoins,
                                onBuy = { viewModel.buyModification("rims", "rims_red", 25) },
                                onEquip = { viewModel.equipModification("rims", "rims_red") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ShopItemCard(
                                title = "Classic Rims",
                                id = "standard",
                                cost = 0,
                                isColor = false,
                                colorSample = Color.Gray,
                                isUnlocked = true,
                                isEquipped = viewModel.activeRims == "standard",
                                totalCoins = viewModel.totalCoins,
                                onBuy = {},
                                onEquip = { viewModel.equipModification("rims", "standard") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Play / Tuned Start Button
        Button(
            onClick = { viewModel.startGame() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("play_button")
                .border(
                    width = 2.dp,
                    color = Color(0xFFFF8A80),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ENGAGE RACE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
    }
}

@Composable
fun ShopItemCard(
    title: String,
    id: String,
    cost: Int,
    isColor: Boolean,
    colorSample: Color,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    totalCoins: Int,
    onBuy: () -> Unit,
    onEquip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) Color(0xFF1E1E28) else Color(0xFF111115)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = if (isEquipped) Color(0xFF00E5FF) else Color(0xFF22222A),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                if (isUnlocked) {
                    onEquip()
                } else if (totalCoins >= cost) {
                    onBuy()
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Color sample pill
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colorSample)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Action status or pricing
            if (isEquipped) {
                Text(
                    text = "ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                )
            } else if (isUnlocked) {
                Text(
                    text = "EQUIP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$cost Coins",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (totalCoins >= cost) Color(0xFFFFD54F) else Color(0xFFE53935),
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GamePlayScreen(viewModel: GameViewModel) {
    var policeLightState by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel.gameState) {
        while (true) {
            policeLightState = !policeLightState
            delay(150)
        }
    }

    // Floating animation loop for powerups
    val transition = rememberInfiniteTransition(label = "powerups")
    val hoverOffset by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val screenWidth = size.width
                    if (screenWidth > 0) {
                        viewModel.dragPlayer(dragAmount.x / screenWidth)
                    }
                }
            }
    ) {
        // 1. CORE GRAPHICS CANVAS RENDERER
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_canvas")
        ) {
            val w = size.width
            val h = size.height

            val roadWidth = w * 0.86f
            val roadLeft = w * 0.07f
            val roadRight = w * 0.93f
            val laneWidth = roadWidth / 6f

            // DRAW SCENERY AND BACKGROUND GRAPHICS BASED ON SCENERY ENVIRONMENT STATE
            when (viewModel.currentScenery) {
                SceneryType.CITY_NEON -> {
                    // Left and right side shoulders
                    drawRect(color = Color(0xFF030303), topLeft = Offset(0f, 0f), size = Size(roadLeft, h))
                    drawRect(color = Color(0xFF030303), topLeft = Offset(roadRight, 0f), size = Size(w - roadRight, h))

                    // Draw decorative neon grids/structures representing cityscape
                    var neonY = 0f
                    while (neonY < h) {
                        // Left skyscrapers outline
                        drawRect(
                            color = Color(0xFF00E5FF).copy(alpha = 0.08f),
                            topLeft = Offset(4.dp.toPx(), neonY + 20.dp.toPx()),
                            size = Size(roadLeft - 8.dp.toPx(), 90.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        // Right skyscrapers outline
                        drawRect(
                            color = Color(0xFFFF4081).copy(alpha = 0.08f),
                            topLeft = Offset(roadRight + 4.dp.toPx(), neonY + 50.dp.toPx()),
                            size = Size(w - roadRight - 8.dp.toPx(), 70.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        neonY += 160.dp.toPx()
                    }
                }
                SceneryType.MOUNTAIN_HIGHWAY -> {
                    // Mountain forest green sides
                    drawRect(color = Color(0xFF142716), topLeft = Offset(0f, 0f), size = Size(roadLeft, h))
                    drawRect(color = Color(0xFF142716), topLeft = Offset(roadRight, 0f), size = Size(w - roadRight, h))

                    // Draw triangle pine trees
                    var treeY = 10.dp.toPx()
                    while (treeY < h) {
                        // Left pine
                        val leftCenter = roadLeft / 2
                        val pathLeft = Path().apply {
                            moveTo(leftCenter, treeY)
                            lineTo(leftCenter - 8.dp.toPx(), treeY + 24.dp.toPx())
                            lineTo(leftCenter + 8.dp.toPx(), treeY + 24.dp.toPx())
                            close()
                        }
                        drawPath(pathLeft, color = Color(0xFF2E5B35))

                        // Right pine
                        val rightCenter = roadRight + (w - roadRight) / 2
                        val pathRight = Path().apply {
                            moveTo(rightCenter, treeY + 30.dp.toPx())
                            lineTo(rightCenter - 8.dp.toPx(), treeY + 54.dp.toPx())
                            lineTo(rightCenter + 8.dp.toPx(), treeY + 54.dp.toPx())
                            close()
                        }
                        drawPath(pathRight, color = Color(0xFF2E5B35))

                        treeY += 120.dp.toPx()
                    }
                }
                SceneryType.COUNTRYSIDE_FOREST -> {
                    // Golden bright farm pastures
                    drawRect(color = Color(0xFF2E5B15), topLeft = Offset(0f, 0f), size = Size(roadLeft, h))
                    drawRect(color = Color(0xFF2E5B15), topLeft = Offset(roadRight, 0f), size = Size(w - roadRight, h))

                    // Draw golden wildflowers dots on fields
                    var flowerY = 15.dp.toPx()
                    while (flowerY < h) {
                        drawCircle(Color(0xFFFFD54F), radius = 3.dp.toPx(), center = Offset(roadLeft * 0.4f, flowerY))
                        drawCircle(Color(0xFFFFD54F), radius = 2.dp.toPx(), center = Offset(roadRight + (w - roadRight) * 0.7f, flowerY + 40.dp.toPx()))
                        flowerY += 80.dp.toPx()
                    }
                }
                SceneryType.CYBER_DESERT -> {
                    // Deep sand dunes
                    drawRect(color = Color(0xFFD88C22), topLeft = Offset(0f, 0f), size = Size(roadLeft, h))
                    drawRect(color = Color(0xFFD88C22), topLeft = Offset(roadRight, 0f), size = Size(w - roadRight, h))

                    // Draw desert cactus shapes
                    var cacY = 30.dp.toPx()
                    while (cacY < h) {
                        // Left cactus body line
                        drawLine(
                            Color(0xFF1B5E20),
                            start = Offset(roadLeft * 0.5f, cacY),
                            end = Offset(roadLeft * 0.5f, cacY + 30.dp.toPx()),
                            strokeWidth = 3.dp.toPx()
                        )
                        // Right cactus body line
                        drawLine(
                            Color(0xFF1B5E20),
                            start = Offset(roadRight + (w - roadRight) * 0.5f, cacY + 40.dp.toPx()),
                            end = Offset(roadRight + (w - roadRight) * 0.5f, cacY + 70.dp.toPx()),
                            strokeWidth = 3.dp.toPx()
                        )
                        cacY += 150.dp.toPx()
                    }
                }
                SceneryType.RAINY_STORM -> {
                    // Dark wet stormy mud banks
                    drawRect(color = Color(0xFF111C24), topLeft = Offset(0f, 0f), size = Size(roadLeft, h))
                    drawRect(color = Color(0xFF111C24), topLeft = Offset(roadRight, 0f), size = Size(w - roadRight, h))

                    // Wet puddles reflecting light
                    var puddleY = 20.dp.toPx()
                    while (puddleY < h) {
                        drawOval(
                            color = Color(0xFF4DD0E1).copy(alpha = 0.2f),
                            topLeft = Offset(roadLeft * 0.2f, puddleY),
                            size = Size(roadLeft * 0.6f, 15.dp.toPx())
                        )
                        drawOval(
                            color = Color(0xFF4DD0E1).copy(alpha = 0.2f),
                            topLeft = Offset(roadRight + (w - roadRight) * 0.2f, puddleY + 50.dp.toPx()),
                            size = Size((w - roadRight) * 0.6f, 12.dp.toPx())
                        )
                        puddleY += 110.dp.toPx()
                    }
                }
            }

            // ROAD CORE ASPHALT PAINTING
            val roadColor = if (viewModel.currentScenery == SceneryType.RAINY_STORM) Color(0xFF151417) else Color(0xFF1D1B20)
            drawRect(
                color = roadColor,
                topLeft = Offset(roadLeft, 0f),
                size = Size(roadWidth, h)
            )

            // White border shoulders
            drawRect(color = Color.White, topLeft = Offset(roadLeft - 2.dp.toPx(), 0f), size = Size(4.dp.toPx(), h))
            drawRect(color = Color.White, topLeft = Offset(roadRight - 2.dp.toPx(), 0f), size = Size(4.dp.toPx(), h))

            // Barriers scrolling indicators
            val dashHeight = 40.dp.toPx()
            val dashGap = 50.dp.toPx()
            val totalDashCycle = dashHeight + dashGap
            val scrollOffsetPx = viewModel.roadScrollOffset * totalDashCycle

            var curY = -totalDashCycle + scrollOffsetPx
            val barrierColor = when (viewModel.currentScenery) {
                SceneryType.CITY_NEON -> Color(0xFF00E5FF)
                SceneryType.RAINY_STORM -> Color(0xFF0091EA)
                else -> Color(0xFFE53935) // standard orange-red blocks
            }

            while (curY < h) {
                drawRect(
                    color = barrierColor,
                    topLeft = Offset(roadLeft - 8.dp.toPx(), curY),
                    size = Size(6.dp.toPx(), dashHeight)
                )
                drawRect(
                    color = barrierColor,
                    topLeft = Offset(roadRight + 2.dp.toPx(), curY),
                    size = Size(6.dp.toPx(), dashHeight)
                )
                curY += totalDashCycle
            }

            // Draw six lanes separating lines
            for (l in 1..5) {
                val lineX = roadLeft + l * laneWidth
                if (l == 3) {
                    // Opposing Traffic Divider
                    val lineCol = if (viewModel.currentScenery == SceneryType.CITY_NEON) Color(0xFFFF4081) else Color(0xFFFFD54F)
                    drawLine(
                        color = lineCol,
                        start = Offset(lineX - 3.dp.toPx(), 0f),
                        end = Offset(lineX - 3.dp.toPx(), h),
                        strokeWidth = 3.dp.toPx()
                    )
                    drawLine(
                        color = lineCol,
                        start = Offset(lineX + 3.dp.toPx(), 0f),
                        end = Offset(lineX + 3.dp.toPx(), h),
                        strokeWidth = 3.dp.toPx()
                    )
                } else {
                    var dotY = -totalDashCycle + scrollOffsetPx
                    while (dotY < h) {
                        drawLine(
                            color = Color(0x66FFFFFF),
                            start = Offset(lineX, dotY),
                            end = Offset(lineX, dotY + dashHeight),
                            strokeWidth = 2.dp.toPx()
                        )
                        dotY += totalDashCycle
                    }
                }
            }

            // DRAW POWER-UPS
            viewModel.powerUps.forEach { pu ->
                val puX = roadLeft + (pu.lane + 0.5f) * laneWidth
                val puY = pu.yPercent * h + hoverOffset
                drawPowerUpOnCanvas(puX, puY, pu.type)
            }

            // DRAW PARTICLES & SCORE FLOATING TEXT INDICATORS
            viewModel.particles.forEach { p ->
                if (p.text != null) {
                    // Draw score bonus floating text using native canvas
                    val paint = android.graphics.Paint().apply {
                        color = p.color.toArgb()
                        textSize = p.sizeDp.dp.toPx()
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                        alpha = (p.life.coerceIn(0f, 1f) * 255).toInt()
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        p.text,
                        p.xPercent * w,
                        p.yPercent * h,
                        paint
                    )
                } else {
                    // Standard visual sparks
                    drawCircle(
                        color = p.color.copy(alpha = p.life),
                        radius = p.sizeDp.dp.toPx(),
                        center = Offset(p.xPercent * w, p.yPercent * h)
                    )
                }
            }

            // DRAW OBSTACLE VEHICLES
            viewModel.obstacles.forEach { obs ->
                val obsX = roadLeft + (obs.lane + 0.5f) * laneWidth
                val obsY = obs.yPercent * h

                val carW = laneWidth * 0.58f
                val carH = carW * 1.5f

                drawCarOnCanvas(
                    centerX = obsX,
                    centerY = obsY,
                    carW = carW,
                    carH = carH,
                    color = obs.color,
                    type = obs.type,
                    facingDown = obs.isOncoming,
                    policeLightState = policeLightState,
                    underglowColor = Color.Transparent,
                    decalType = "none",
                    rimType = "standard"
                )
            }

            // DRAW TUNED PLAYER CAR
            val playerX = viewModel.player.xPercent * w
            val playerY = 0.75f * h
            val pCarW = laneWidth * 0.58f
            val pCarH = pCarW * 1.5f

            drawCarOnCanvas(
                centerX = playerX,
                centerY = playerY,
                carW = pCarW,
                carH = pCarH,
                color = viewModel.player.color,
                type = viewModel.player.type,
                facingDown = false,
                policeLightState = policeLightState,
                underglowColor = viewModel.player.underglowColor,
                decalType = viewModel.player.decalType,
                rimType = viewModel.player.rimType
            )

            // DRAW SHIELD SURROUNDING PLAYER
            if (viewModel.player.hasShield) {
                val shieldRadius = pCarH * 0.82f
                val isLowTime = viewModel.player.shieldTimeLeftMs < 2000
                val pulseIntensity = if (isLowTime) (sin(System.currentTimeMillis() / 80f) + 1f) * 0.15f else 0.08f

                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.22f + pulseIntensity),
                    radius = shieldRadius + (pulseIntensity * 30),
                    center = Offset(playerX, playerY)
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.7f),
                    radius = shieldRadius + (pulseIntensity * 30),
                    center = Offset(playerX, playerY),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // DRAW SPEED BOOST TRAIL AURA
            if (viewModel.player.hasBoost) {
                val boostWidth = pCarW * 1.15f
                val boostHeight = pCarH * 1.35f
                drawRoundRect(
                    color = Color(0xFFFF5722).copy(alpha = 0.25f),
                    topLeft = Offset(playerX - boostWidth / 2f, playerY - boostHeight / 2f),
                    size = Size(boostWidth, boostHeight),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }

        // 2. HUD - THEME (IMMERSIVE UI) WITH CYAN AND ORANGE GLOW
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            // Header Stats Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cyber Score Box
                Column {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = String.format("%06d", viewModel.score),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-1).sp
                        ),
                        modifier = Modifier.testTag("score_display")
                    )
                }

                // Pause Control
                IconButton(
                    onClick = { viewModel.pauseGame() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xDD111115))
                        .border(1.dp, Color(0xFF22222A), CircleShape)
                        .size(42.dp)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Cyber Speed / Powerup Box
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SPEED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFF7043).copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${viewModel.currentSpeedKmh}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = FontStyle.Italic
                            )
                        )
                        Text(
                            text = "KMH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFF7043),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub Status Details: Environmental Zone, Coin Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ZONE BADGE
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC08080C)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, Color(0xFF22222A), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val badgeColor = when (viewModel.currentScenery) {
                            SceneryType.CITY_NEON -> Color(0xFF00E5FF)
                            SceneryType.MOUNTAIN_HIGHWAY -> Color(0xFFA5D6A7)
                            SceneryType.COUNTRYSIDE_FOREST -> Color(0xFF81C784)
                            SceneryType.CYBER_DESERT -> Color(0xFFFFE082)
                            SceneryType.RAINY_STORM -> Color(0xFF29B6F6)
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = viewModel.currentScenery.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                // Coins collected in-game
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC08080C))
                        .border(1.dp, Color(0xFF22222A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${viewModel.coinsCollected}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Linear Shield / Boost duration progress bars (IMMERSIVE POWER GAUGE)
            if (viewModel.player.hasShield) {
                LinearPowerGauge(
                    label = "Shield Power",
                    color = Color(0xFF00E5FF),
                    progress = viewModel.player.shieldTimeLeftMs / 8000f
                )
            }

            if (viewModel.player.hasBoost) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearPowerGauge(
                    label = "Overclock Boost",
                    color = Color(0xFFFF7043),
                    progress = viewModel.player.boostTimeLeftMs / 5000f
                )
            }
        }

        // 3. SOVEREIGN BOTTOM BAR - TRANS-LUCENT FLUID GLASS DESIGN WITH 72dp CLICK TARGETS
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xEE050508))
                    )
                )
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xB3111115))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT & RIGHT ACCESSIBLE ARROWS
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.triggerMoveLeft() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24)),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(68.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .testTag("steer_left_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Steer Left",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerMoveRight() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E24)),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(68.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .testTag("steer_right_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Steer Right",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Decorative status center lights
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF22222A)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00E5FF)))
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF22222A)))
                }

                // SHIELD STATE DECORATIVE BADGE BUTTON
                IconButton(
                    onClick = { /* Decorative or triggers horn! */ },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFF9800))
                        .size(60.dp)
                        .border(2.dp, Color(0xFFFFCC80), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Energy Status",
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LinearPowerGauge(label: String, color: Color, progress: Float) {
    Column(modifier = Modifier.fillMaxWidth(0.5f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFF22222A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun VehicleSelectionCard(
    title: String,
    type: CarType,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E1E28) else Color(0xFF111115)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF22222A),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .testTag("vehicle_card_$title")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF888899),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF888899),
                    fontSize = 8.5.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun PauseOverlay(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD050505)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111115)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp)
                .border(1.dp, Color(0xFF22222A), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RACE PAUSED",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.testTag("pause_overlay_title")
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Traveled Zone: ${viewModel.currentScenery.name.replace("_", " ")}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF888899))
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Resume button
                Button(
                    onClick = { viewModel.resumeGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("resume_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RESUME RUN",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Exit button
                OutlinedButton(
                    onClick = { viewModel.returnToMenu() },
                    border = BorderStroke(1.dp, Color(0xFFFF1744)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF1744)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("menu_button")
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RETURN TO TUNER",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverOverlay(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE050505)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111115)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp)
                .border(2.dp, Color(0xFFFF1744), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = Color(0xFFFF1744),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "RUN TERMINATED",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.testTag("game_over_title")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Car crashed. Upgrades preserved.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF888899))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Scores display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DISTANCE",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF888899))
                        )
                        Text(
                            text = "${viewModel.score}m",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "EARNED COINS",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF888899))
                        )
                        Text(
                            text = "+${viewModel.coinsCollected}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // High score badge
                val isNewHighScore = viewModel.score >= viewModel.highScore && viewModel.score > 0
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNewHighScore) Color(0x22FFD54F) else Color(0xFF1A1A22)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isNewHighScore) Color(0xFFFFD54F) else Color(0xFF22222A),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isNewHighScore) "🎉 PERSONAL RECORD!" else "HIGH SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isNewHighScore) Color(0xFFFFD54F) else Color(0xFF888899),
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = "${viewModel.highScore}m",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Play Again Button
                Button(
                    onClick = { viewModel.startGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("restart_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENGAGE RACE AGAIN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Back to menu
                OutlinedButton(
                    onClick = { viewModel.returnToMenu() },
                    border = BorderStroke(1.dp, Color(0xFF888899)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF888899)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("menu_exit_button")
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BACK TO GARAGE",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ==========================================
// CANVAS GRAPHICS CUSTOM DRAW FUNCTIONS
// ==========================================

private fun DrawScope.drawCarOnCanvas(
    centerX: Float,
    centerY: Float,
    carW: Float,
    carH: Float,
    color: Color,
    type: CarType,
    facingDown: Boolean,
    policeLightState: Boolean,
    underglowColor: Color,
    decalType: String,
    rimType: String
) {
    withTransform({
        if (facingDown) {
            rotate(degrees = 180f, pivot = Offset(centerX, centerY))
        }
    }) {
        val carLeft = centerX - carW / 2
        val carTop = centerY - carH / 2
        val chassisRadius = when (type) {
            CarType.SPORTS -> CornerRadius(carW * 0.35f, carH * 0.25f)
            CarType.TRUCK -> CornerRadius(carW * 0.15f, carH * 0.1f)
            else -> CornerRadius(carW * 0.25f, carH * 0.2f)
        }

        // 0. Neon Underglow Effect (faded outer neon aura)
        if (underglowColor != Color.Transparent) {
            drawRoundRect(
                color = underglowColor.copy(alpha = 0.38f),
                topLeft = Offset(carLeft - 5.dp.toPx(), carTop - 3.dp.toPx()),
                size = Size(carW + 10.dp.toPx(), carH + 6.dp.toPx()),
                cornerRadius = chassisRadius,
                style = Stroke(width = 6.dp.toPx())
            )
        }

        // 1. Draw Wheels (4 corner wheels)
        val wheelW = carW * 0.18f
        val wheelH = carH * 0.22f
        val wheelColor = Color(0xFF151515)
        val wheelRadius = CornerRadius(2.dp.toPx())

        fun drawWheel(wx: Float, wy: Float) {
            drawRoundRect(
                color = wheelColor,
                topLeft = Offset(wx, wy),
                size = Size(wheelW, wheelH),
                cornerRadius = wheelRadius
            )
            // Custom Forge Rims detail
            if (rimType == "rims_gold") {
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = wheelW * 0.25f,
                    center = Offset(wx + wheelW / 2, wy + wheelH / 2)
                )
            } else if (rimType == "rims_red") {
                drawCircle(
                    color = Color(0xFFFF1744),
                    radius = wheelW * 0.25f,
                    center = Offset(wx + wheelW / 2, wy + wheelH / 2)
                )
            }
        }

        // Front-Left Wheel
        drawWheel(carLeft - wheelW * 0.4f, carTop + carH * 0.15f)
        // Front-Right Wheel
        drawWheel(carLeft + carW - wheelW * 0.6f, carTop + carH * 0.15f)
        // Rear-Left Wheel
        drawWheel(carLeft - wheelW * 0.4f, carTop + carH * 0.65f)
        // Rear-Right Wheel
        drawWheel(carLeft + carW - wheelW * 0.6f, carTop + carH * 0.65f)

        // 2. Draw Main Vehicle Chassis Base
        drawRoundRect(
            color = color,
            topLeft = Offset(carLeft, carTop),
            size = Size(carW, carH),
            cornerRadius = chassisRadius
        )

        // 2.5 Draw Custom Decals
        if (decalType == "decal_stripe") {
            // Twin white stripes down center of the car
            drawRect(
                color = Color.White.copy(alpha = 0.8f),
                topLeft = Offset(centerX - carW * 0.08f, carTop),
                size = Size(carW * 0.04f, carH)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.8f),
                topLeft = Offset(centerX + carW * 0.04f, carTop),
                size = Size(carW * 0.04f, carH)
            )
        } else if (decalType == "decal_flames") {
            // Flame vinyl lines
            drawLine(
                color = Color(0xFFFF3D00),
                start = Offset(carLeft + carW * 0.15f, carTop + carH * 0.35f),
                end = Offset(centerX - carW * 0.1f, carTop + carH * 0.15f),
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = Color(0xFFFF3D00),
                start = Offset(carLeft + carW * 0.85f, carTop + carH * 0.35f),
                end = Offset(centerX + carW * 0.1f, carTop + carH * 0.15f),
                strokeWidth = 3.dp.toPx()
            )
        }

        // 3. Draw Cabin/Windshield
        val cabinLeft = carLeft + carW * 0.12f
        val cabinW = carW * 0.76f
        val cabinH = carH * 0.35f
        val cabinTop = carTop + carH * 0.32f

        drawRoundRect(
            color = Color(0xFF101010), // Sleek black glass
            topLeft = Offset(cabinLeft, cabinTop),
            size = Size(cabinW, cabinH),
            cornerRadius = CornerRadius(carW * 0.15f)
        )

        // Reflection lines on windshield
        drawRoundRect(
            color = Color(0x33FFFFFF),
            topLeft = Offset(cabinLeft + 3.dp.toPx(), cabinTop + 2.dp.toPx()),
            size = Size(cabinW - 6.dp.toPx(), cabinH * 0.35f),
            cornerRadius = CornerRadius(carW * 0.1f)
        )

        // 4. Vehicle specific design details
        when (type) {
            CarType.SPORTS -> {
                // Carbon Rear Wing Spoiler
                val spoilerW = carW * 1.1f
                val spoilerH = carH * 0.08f
                drawRect(
                    color = Color(0xFF151515),
                    topLeft = Offset(centerX - spoilerW / 2, carTop + carH * 0.9f),
                    size = Size(spoilerW, spoilerH)
                )
                drawRect(
                    color = Color(0xFF151515),
                    topLeft = Offset(carLeft + carW * 0.15f, carTop + carH * 0.82f),
                    size = Size(carW * 0.08f, carH * 0.08f)
                )
                drawRect(
                    color = Color(0xFF151515),
                    topLeft = Offset(carLeft + carW * 0.77f, carTop + carH * 0.82f),
                    size = Size(carW * 0.08f, carH * 0.08f)
                )
            }
            CarType.TRUCK -> {
                val cargoTop = carTop + carH * 0.45f
                val cargoH = carH * 0.5f
                drawRoundRect(
                    color = Color(red = (color.red * 0.8f).coerceIn(0f, 1f), green = (color.green * 0.8f).coerceIn(0f, 1f), blue = (color.blue * 0.8f).coerceIn(0f, 1f), alpha = color.alpha),
                    topLeft = Offset(carLeft + 2.dp.toPx(), cargoTop),
                    size = Size(carW - 4.dp.toPx(), cargoH),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                for (ry in 1..4) {
                    val ribY = cargoTop + (cargoH * ry / 5f)
                    drawLine(
                        color = Color(0x22000000),
                        start = Offset(carLeft + 6.dp.toPx(), ribY),
                        end = Offset(carLeft + carW - 6.dp.toPx(), ribY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            CarType.POLICE -> {
                drawCircle(
                    color = Color.White,
                    radius = carW * 0.12f,
                    center = Offset(centerX, carTop + carH * 0.18f)
                )
                drawCircle(
                    color = Color(0xFF0D47A1),
                    radius = carW * 0.06f,
                    center = Offset(centerX, carTop + carH * 0.18f)
                )

                // Flashing beacon bar
                val barW = carW * 0.65f
                val barH = carH * 0.07f
                val barLeft = centerX - barW / 2
                val barTop = cabinTop + cabinH * 0.15f

                val redLight = Color(0xFFF44336)
                val blueLight = Color(0xFF2196F3)

                drawRect(
                    color = if (policeLightState) redLight else blueLight,
                    topLeft = Offset(barLeft, barTop),
                    size = Size(barW / 2f, barH)
                )
                drawRect(
                    color = if (policeLightState) blueLight else redLight,
                    topLeft = Offset(centerX, barTop),
                    size = Size(barW / 2f, barH)
                )
            }
            CarType.SEDAN -> {
                drawLine(
                    color = Color(0x44000000),
                    start = Offset(carLeft + carW * 0.25f, carTop + carH * 0.06f),
                    end = Offset(carLeft + carW * 0.75f, carTop + carH * 0.06f),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // 5. Headlights
        val lightRadius = carW * 0.09f
        drawCircle(
            color = Color(0xFFFFF59D),
            radius = lightRadius,
            center = Offset(carLeft + carW * 0.16f, carTop + carH * 0.04f)
        )
        drawCircle(
            color = Color(0xFFFFF59D),
            radius = lightRadius,
            center = Offset(carLeft + carW * 0.84f, carTop + carH * 0.04f)
        )

        // 6. Taillights
        val tailLightW = carW * 0.15f
        val tailLightH = carH * 0.05f
        drawRect(
            color = Color(0xFFE53935),
            topLeft = Offset(carLeft + carW * 0.10f, carTop + carH - tailLightH),
            size = Size(tailLightW, tailLightH)
        )
        drawRect(
            color = Color(0xFFE53935),
            topLeft = Offset(carLeft + carW * 0.75f, carTop + carH - tailLightH),
            size = Size(tailLightW, tailLightH)
        )
    }
}

private fun DrawScope.drawPowerUpOnCanvas(px: Float, py: Float, type: PowerUpType) {
    when (type) {
        PowerUpType.SHIELD -> {
            val radius = 15.dp.toPx()
            val time = System.currentTimeMillis()
            val pulse = (sin(time / 150f) + 1f) * 1.5f.dp.toPx()

            drawCircle(
                color = Color(0xFF29B6F6).copy(alpha = 0.25f),
                radius = radius + pulse,
                center = Offset(px, py)
            )

            drawCircle(
                color = Color(0xFF29B6F6),
                radius = radius,
                center = Offset(px, py),
                style = Stroke(width = 3.dp.toPx())
            )

            val shieldPath = Path().apply {
                moveTo(px, py - radius * 0.5f)
                quadraticBezierTo(px + radius * 0.5f, py - radius * 0.5f, px + radius * 0.5f, py)
                quadraticBezierTo(px + radius * 0.5f, py + radius * 0.4f, px, py + radius * 0.7f)
                quadraticBezierTo(px - radius * 0.5f, py + radius * 0.4f, px - radius * 0.5f, py)
                quadraticBezierTo(px - radius * 0.5f, py - radius * 0.5f, px, py - radius * 0.5f)
            }
            drawPath(
                path = shieldPath,
                color = Color(0xFFE3F2FD)
            )
        }
        PowerUpType.BOOST -> {
            val radius = 14.dp.toPx()
            val time = System.currentTimeMillis()
            val flare = (sin(time / 80f) + 1f) * 2.dp.toPx()

            drawCircle(
                color = Color(0xFFFF5722).copy(alpha = 0.3f),
                radius = radius + flare,
                center = Offset(px, py)
            )

            // Left nitrous
            drawRoundRect(
                color = Color(0xFFE64A19),
                topLeft = Offset(px - 10.dp.toPx(), py - 12.dp.toPx()),
                size = Size(7.dp.toPx(), 22.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            // Right nitrous
            drawRoundRect(
                color = Color(0xFFE64A19),
                topLeft = Offset(px + 3.dp.toPx(), py - 12.dp.toPx()),
                size = Size(7.dp.toPx(), 22.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )

            drawRect(
                color = Color(0xFFECEFF1),
                topLeft = Offset(px - 9.dp.toPx(), py - 15.dp.toPx()),
                size = Size(5.dp.toPx(), 4.dp.toPx())
            )
            drawRect(
                color = Color(0xFFECEFF1),
                topLeft = Offset(px + 4.dp.toPx(), py - 15.dp.toPx()),
                size = Size(5.dp.toPx(), 4.dp.toPx())
            )

            val firePath = Path().apply {
                moveTo(px - 8.dp.toPx(), py + 10.dp.toPx())
                lineTo(px - 5.dp.toPx(), py + 18.dp.toPx() + flare)
                lineTo(px - 2.dp.toPx(), py + 10.dp.toPx())
                moveTo(px + 2.dp.toPx(), py + 10.dp.toPx())
                lineTo(px + 5.dp.toPx(), py + 18.dp.toPx() + flare)
                lineTo(px + 8.dp.toPx(), py + 10.dp.toPx())
            }
            drawPath(
                path = firePath,
                color = Color(0xFFFF9800)
            )
        }
        PowerUpType.COIN -> {
            val radius = 13.dp.toPx()
            val angle = (System.currentTimeMillis() / 4f) % 360f

            withTransform({
                val scaleX = sin(angle * Math.PI / 180.0).toFloat()
                scale(scaleX = scaleX, scaleY = 1f, pivot = Offset(px, py))
            }) {
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = radius,
                    center = Offset(px, py)
                )

                drawCircle(
                    color = Color(0xFFF57F17),
                    radius = radius * 0.85f,
                    center = Offset(px, py),
                    style = Stroke(width = 1.5f.dp.toPx())
                )

                drawCircle(
                    color = Color(0xFFF57F17),
                    radius = radius * 0.35f,
                    center = Offset(px, py)
                )
            }
        }
    }
}
