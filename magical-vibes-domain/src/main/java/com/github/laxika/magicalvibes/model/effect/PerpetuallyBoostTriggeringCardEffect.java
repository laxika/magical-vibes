package com.github.laxika.magicalvibes.model.effect;

/** Records a perpetual power/toughness boost for the card that caused the surrounding trigger. */
public record PerpetuallyBoostTriggeringCardEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
