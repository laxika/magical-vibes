package com.github.laxika.magicalvibes.model.effect;

/** Records a perpetual power/toughness boost for creature cards currently in the controller's graveyard. */
public record PerpetuallyBoostCreatureCardsInGraveyardEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
