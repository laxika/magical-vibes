package com.github.laxika.magicalvibes.model.effect;

/** Records a perpetual boost for creature cards currently in the controller's graveyard. */
public record PerpetuallyBoostCreatureCardsInGraveyardEffect(
        int powerBoost, int toughnessBoost, boolean usePermanentCardCount) implements CardEffect {

    public PerpetuallyBoostCreatureCardsInGraveyardEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, false);
    }

    public PerpetuallyBoostCreatureCardsInGraveyardEffect() {
        this(0, 0, true);
    }
}
