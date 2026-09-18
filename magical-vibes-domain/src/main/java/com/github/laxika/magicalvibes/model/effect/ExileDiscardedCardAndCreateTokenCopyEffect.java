package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the card selected by a preceding {@link DiscardCardThenEffect} and creates a modified
 * token copy of that card.
 */
public record ExileDiscardedCardAndCreateTokenCopyEffect(
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect) implements CardEffect {
}
