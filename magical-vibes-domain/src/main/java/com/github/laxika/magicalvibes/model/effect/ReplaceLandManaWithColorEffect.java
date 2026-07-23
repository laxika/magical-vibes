package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * STATIC replacement: if a land is tapped for mana, it produces {@code color} instead of any other
 * type (amount unchanged). Infernal Darkness ({@code BLACK}). Applied via
 * {@code GameQueryService.fixedLandManaColor}.
 */
public record ReplaceLandManaWithColorEffect(ManaColor color) implements LandManaProducesFixedColorEffect {
}
