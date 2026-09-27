package com.github.laxika.magicalvibes.model.effect;

/** Gives creature cards currently in the controller's library a perpetual power/toughness boost. */
public record PerpetuallyBoostCreatureCardsInLibraryEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
