package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Until end of turn, the effect controller's lands produce the specified color instead of any
 * other type when tapped for mana. The amount produced is unchanged.
 */
public record LandManaProducesFixedColorUntilEndOfTurnEffect(ManaColor color) implements CardEffect {
}
