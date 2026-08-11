package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Cost effect that requires exiling exactly N matching cards from the controller's graveyard.
 * Unlike {@link ExileXCardsFromGraveyardCost}, this requires an exact count and does not set the X value.
 * If requiredType is null, any card in the graveyard qualifies.
 * Uses exileGraveyardCardIndices (List&lt;Integer&gt;) from PlayCardRequest.
 *
 * @param count        the exact number of cards that must be exiled
 * @param requiredType the card type required (null = any)
 * @param predicate an additional card predicate (null = no additional filter)
 */
public record ExileNCardsFromGraveyardCost(int count, CardType requiredType, CardPredicate predicate) implements CostEffect {

    public ExileNCardsFromGraveyardCost(int count, CardType requiredType) {
        this(count, requiredType, null);
    }

    @Override
    public int consumedGraveyardCardCount() {
        return count;
    }

    @Override
    public CardType consumedGraveyardCardType() {
        return requiredType;
    }

    @Override
    public CardPredicate consumedGraveyardCardPredicate() {
        return predicate;
    }
}
