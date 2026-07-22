package com.github.laxika.magicalvibes.model.effect;

/**
 * Discard a card, then the source permanent gets +power/+toughness until end of turn.
 * <p>
 * Commonly wrapped in {@link MayEffect} for "you may discard a card. If you do, this creature
 * gets +X/+Y until end of turn."
 * <p>
 * Uses {@code DiscardFollowUp.boost} carried on the discard choice to apply the boost after the
 * interactive discard completes.
 */
public record DiscardCardAndBoostSelfEffect(int power, int toughness) implements CardEffect {
}
