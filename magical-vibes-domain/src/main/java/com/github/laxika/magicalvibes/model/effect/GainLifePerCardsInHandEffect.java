package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect: the controller gains 1 life for each card in their hand.
 * Used by Venser's Journal (upkeep trigger).
 */
public record GainLifePerCardsInHandEffect() implements CardEffect {
}
