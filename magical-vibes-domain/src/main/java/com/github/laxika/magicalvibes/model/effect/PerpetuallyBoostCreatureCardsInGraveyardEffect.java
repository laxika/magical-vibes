package com.github.laxika.magicalvibes.model.effect;

/** Gives creature cards currently in the controller's graveyard a perpetual power/toughness boost. */
public record PerpetuallyBoostCreatureCardsInGraveyardEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
