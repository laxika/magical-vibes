package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * As the source permanent enters, its controller must exile exactly one matching card from their
 * graveyard. The entry pipeline tracks the selected card with the entering permanent.
 */
public record ExileCardFromGraveyardOnEnterEffect(CardPredicate filter)
        implements AsEntersGraveyardExileEffect {

    @Override
    public int minimumCards() {
        return 1;
    }

    @Override
    public int maximumCards() {
        return 1;
    }
}
