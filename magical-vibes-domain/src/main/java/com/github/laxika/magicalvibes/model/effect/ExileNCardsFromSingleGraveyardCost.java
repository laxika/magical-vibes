package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Cost that requires exiling exactly {@code count} matching cards from one graveyard.
 * Unlike {@link ExileNCardsFromGraveyardCost}, the graveyard may belong to any player.
 */
public record ExileNCardsFromSingleGraveyardCost(int count, CardType requiredType,
                                                  CardPredicate predicate) implements CostEffect {

    public ExileNCardsFromSingleGraveyardCost(int count, CardType requiredType) {
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
