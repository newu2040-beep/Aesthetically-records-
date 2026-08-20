package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CelestialPrimary
import com.example.ui.theme.SoftPink

enum class NavDestination(val route: String, val title: String) {
    HOME("home", "Home"),
    TEMPLATES("templates", "Templates"),
    RECORD("record", "Record"),
    STUDIO("studio", "Studio"),
    SETTINGS("settings", "Settings")
}

@Composable
fun AestheticallyBottomNav(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("bottom_nav_bar")
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home
                NavItem(
                    title = "Home",
                    icon = if (currentDestination == NavDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    isSelected = currentDestination == NavDestination.HOME,
                    onClick = { onNavigate(NavDestination.HOME) }
                )

                // 2. Templates
                NavItem(
                    title = "Templates",
                    icon = if (currentDestination == NavDestination.TEMPLATES) Icons.Filled.GridView else Icons.Outlined.GridView,
                    isSelected = currentDestination == NavDestination.TEMPLATES,
                    onClick = { onNavigate(NavDestination.TEMPLATES) }
                )

                // Space for Center FAB
                Spacer(modifier = Modifier.width(56.dp))

                // 3. Studio
                NavItem(
                    title = "Studio",
                    icon = if (currentDestination == NavDestination.STUDIO) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                    isSelected = currentDestination == NavDestination.STUDIO,
                    onClick = { onNavigate(NavDestination.STUDIO) }
                )

                // 4. Settings
                NavItem(
                    title = "Settings",
                    icon = if (currentDestination == NavDestination.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    isSelected = currentDestination == NavDestination.SETTINGS,
                    onClick = { onNavigate(NavDestination.SETTINGS) }
                )
            }
        }

        // Center Floating Record Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
                .scale(pulseScale)
                .size(62.dp)
                .shadow(12.dp, CircleShape, spotColor = CelestialPrimary)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(SoftPink, CelestialPrimary)
                    )
                )
                .clickable { onNavigate(NavDestination.RECORD) }
                .testTag("center_record_fab"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Record Voice Note",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun NavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
