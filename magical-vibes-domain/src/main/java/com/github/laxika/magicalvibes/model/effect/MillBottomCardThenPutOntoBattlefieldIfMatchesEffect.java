package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Puts the bottom card of the controller's library into their graveyard, then returns it to the battlefield if it matches the filter. */
public record MillBottomCardThenPutOntoBattlefieldIfMatchesEffect(CardPredicate filter) implements CardEffect {
}
