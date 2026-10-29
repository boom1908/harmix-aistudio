package com.boom.harmix.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ---- Harmix "Charcoal & Ivory Minimal Glass" Palette ----

// Core Theme Palette
val Charcoal = Color(0xFF292929)
val Ivory = Color(0xFFFFFFE3)
val SoftGrey = Color(0xFFD9D7D2)
val Slate = Color(0xFF666666)
val PureBlack = Color(0xFF111111)

// Secondary / Tonal Surfaces
val CharcoalSurface = Color(0xFF1F1F1F)
val CharcoalElevated = Color(0xFF333333)

// Highly Noticeable, Minimal Frosted Glass Tints & Brushes
val GlassFill = Color(0x24FFFFE3) // 14% Ivory tint for vibrant glass panels
val GlassFillSubtle = Color(0x14FFFFE3) // 8% Ivory tint for background cards
val GlassFillStrong = Color(0x38FFFFE3) // 22% Ivory tint for active glass elements
val GlassBorder = Color(0x3EFFFFE3) // 24% Ivory hairline specular edge
val GlassBorderSubtle = Color(0x22FFFFE3) // 13% Ivory soft border
val GlassBorderBright = Color(0x66FFFFE3) // 40% Ivory top specular reflection
val GlassCharcoal = Color(0xB8292929) // 72% Charcoal frosted surface

// Distinctive Specular Glass Brushes for Noticeable Depth
val GlassSpecularBorder: Brush = Brush.verticalGradient(
    0.0f to Color(0x6AFFFFE3), // Crisp top light catch
    0.35f to Color(0x28FFFFE3), // Subtle lateral sheen
    1.0f to Color(0x10292929)  // Deep bottom rim
)

val GlassCardBrush: Brush = Brush.verticalGradient(
    0.0f to Color(0x28FFFFE3), // Specular light wash on top
    0.12f to Color(0xC0292929), // Frosted Charcoal body
    1.0f to Color(0xD81F1F1F)  // Deep charcoal-black base
)

val GlassFloatingBarBrush: Brush = Brush.verticalGradient(
    0.0f to Color(0x32FFFFE3), // Top reflection highlight
    0.15f to Color(0xB8292929), // Translucent frosted charcoal
    1.0f to Color(0xCE181818)  // Translucent dark base
)

val GlassTabPillBrush: Brush = Brush.verticalGradient(
    0.0f to Color(0x44FFFFE3), // 27% ivory top specular sheen
    0.35f to Color(0x22FFFFE3), // 13% ivory translucent body
    1.0f to Color(0x14FFFFE3)  // 8% ivory soft bottom glow
)

val GlassTabPillBorder: Brush = Brush.verticalGradient(
    0.0f to Color(0x88FFFFE3), // 53% crisp specular light edge
    0.45f to Color(0x35FFFFE3), // 21% translucent side outline
    1.0f to Color(0x14FFFFE3)  // 8% delicate lower rim
)

val GlassGlowBrush: Brush = Brush.radialGradient(
    listOf(Color(0x30FFFFE3), Color(0x00FFFFE3))
)

// Backwards-compatible aliases mapping seamlessly to the new palette
val MidnightBlack = PureBlack
val SurfaceCoal = Charcoal
val SunsetGold = Ivory
val AmberGlow = SoftGrey
val EmberRed = Color(0xFFE57373)
val Champagne = Ivory
val Bone = Ivory
val Sand = SoftGrey
val DeepMidnight = PureBlack
val SurfaceDark = Charcoal
val ZenCyan = Ivory
val SkyBlue = SoftGrey
val SoftLavender = Ivory
val MistWhite = Ivory
val CoolGray = SoftGrey
