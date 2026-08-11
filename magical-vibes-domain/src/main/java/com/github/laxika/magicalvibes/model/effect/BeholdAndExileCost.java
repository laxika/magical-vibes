package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Additional cast cost to choose a permanent of {@code subtype} you control or a matching card
 * from your hand, and exile the chosen object.
 */
public record BeholdAndExileCost(CardSubtype subtype) implements CostEffect {
}
