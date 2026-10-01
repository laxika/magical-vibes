package com.github.laxika.magicalvibes.model.effect;

/** Static land-play permission for players whose last choice on this plane matches {@code mode}. */
public record PlayersWithChosenPlanarModePlayAdditionalLandEffect(String mode) implements CardEffect {
}
