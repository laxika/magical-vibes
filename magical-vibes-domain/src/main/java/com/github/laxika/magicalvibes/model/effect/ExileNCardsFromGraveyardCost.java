package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Cost effect that requires exiling exactly N cards of the specified type from the controller's graveyard.
 * If requiredType is null, any card in the graveyard qualifies.
 * Uses exileGraveyardCardIndices (List&lt;Integer&gt;) from PlayCardRequest.
 *
 * @param count        the exact number of cards that must be exiled
 * @param requiredType the card type required (null = any)
 */
public record ExileNCardsFromGraveyardCost(int count, CardType requiredType) implements CostEffect {
}
