package com.github.laxika.magicalvibes.model.effect;

/** Adds two equal-sized batches of mana, each in a different chosen color. */
public record AwardTwoDifferentColorManaEffect(int amount, ManaSpendRestriction restriction)
        implements ManaProducingEffect {
}
