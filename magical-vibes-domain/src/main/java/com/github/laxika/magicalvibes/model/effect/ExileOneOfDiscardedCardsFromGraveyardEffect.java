package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution effect for choosing one of the nonland cards discarded in the triggering discard
 * event and exiling it from its owner's graveyard while tracking it with the source permanent.
 */
public record ExileOneOfDiscardedCardsFromGraveyardEffect() implements CardEffect {
}
