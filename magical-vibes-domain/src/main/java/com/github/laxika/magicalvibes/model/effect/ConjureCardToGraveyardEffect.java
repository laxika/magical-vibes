package com.github.laxika.magicalvibes.model.effect;

/** Conjures one full, non-token copy of a registered card printing into a player's graveyard. */
public record ConjureCardToGraveyardEffect(
        String setCode,
        String collectorNumber
) implements CardEffect {
}
