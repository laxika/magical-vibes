package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the bottom card of a target player's graveyard. The bottom card is the first card in the
 * append-ordered graveyard list; an empty graveyard is a no-op.
 */
public record ExileBottomCardOfTargetPlayerGraveyardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
