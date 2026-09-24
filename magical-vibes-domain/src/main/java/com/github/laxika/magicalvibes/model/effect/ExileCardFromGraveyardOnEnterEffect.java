package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * As the source permanent enters, its controller must exile exactly one matching card from a
 * graveyard. The entry pipeline tracks the selected card with the entering permanent.
 */
public record ExileCardFromGraveyardOnEnterEffect(CardPredicate filter, GraveyardSearchScope graveyardScope)
        implements AsEntersGraveyardExileEffect {

    public ExileCardFromGraveyardOnEnterEffect(CardPredicate filter) {
        this(filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    @Override
    public int minimumCards() {
        return 1;
    }

    @Override
    public int maximumCards() {
        return 1;
    }
}
