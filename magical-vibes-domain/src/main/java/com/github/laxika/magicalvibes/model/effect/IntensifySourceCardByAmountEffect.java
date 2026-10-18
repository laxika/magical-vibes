package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Increases the persistent intensity of the physical card that supplies the effect by a
 * {@link DynamicAmount} resolved at resolution time (e.g. {@code ChosenPermanentPower} for
 * Quickbeast Amulet's "intensifies by X, where X is that creature's power").
 */
public record IntensifySourceCardByAmountEffect(DynamicAmount amount) implements CardEffect {
}
