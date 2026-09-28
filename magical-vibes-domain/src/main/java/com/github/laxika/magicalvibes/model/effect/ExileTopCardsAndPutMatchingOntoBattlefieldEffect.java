package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exiles the top cards of the controller's library, then puts every matching card onto the battlefield. */
public record ExileTopCardsAndPutMatchingOntoBattlefieldEffect(int count, CardPredicate filter)
        implements CardEffect {
}
