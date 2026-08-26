package com.github.laxika.magicalvibes.model.effect;

/**
 * Discard a card, then optionally draw cards and give the source permanent
 * +power/+toughness until end of turn.
 * <p>
 * Commonly wrapped in {@link MayEffect} for "you may discard a card. If you do, this creature
 * gets +X/+Y until end of turn."
 * <p>
 * Uses a {@code DiscardFollowUp} carried on the discard choice to apply the draw and boost after
 * the interactive discard completes.
 */
public record DiscardCardAndBoostSelfEffect(int power, int toughness, int drawCount) implements CardEffect {

    public DiscardCardAndBoostSelfEffect(int power, int toughness) {
        this(power, toughness, 0);
    }
}
