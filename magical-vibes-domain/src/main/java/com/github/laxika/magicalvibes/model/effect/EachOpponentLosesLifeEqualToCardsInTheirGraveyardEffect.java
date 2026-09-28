package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Each opponent loses life equal to the number of matching cards in that opponent's graveyard.
 * The amount is evaluated separately for each opponent.
 */
public record EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffect(CardPredicate filter)
        implements CardEffect {

    public EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffect() {
        this(null);
    }
}
