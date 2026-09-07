package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the resolving effect's controller permission to play cards from opponents' graveyards
 * until end of turn, including casting spells and playing lands.
 */
public record AllowPlayFromOtherGraveyardsThisTurnEffect() implements CardEffect {
}
