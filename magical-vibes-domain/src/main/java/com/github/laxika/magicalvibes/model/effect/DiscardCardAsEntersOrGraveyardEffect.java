package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "If this permanent would enter, you may discard a matching card instead. If you do, put it onto
 * the battlefield. If you don't, put it into its owner's graveyard."
 */
public record DiscardCardAsEntersOrGraveyardEffect(CardPredicate filter, String description)
        implements EntryCostReplacementEffect {

    @Override
    public Kind kind() {
        return Kind.DISCARD_CARD;
    }

    @Override
    public int count() {
        return 1;
    }

    @Override
    public CardPredicate cardFilter() {
        return filter;
    }
}
