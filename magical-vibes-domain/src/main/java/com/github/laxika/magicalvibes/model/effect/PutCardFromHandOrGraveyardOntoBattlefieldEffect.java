package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Puts one matching card from the controller's hand or graveyard onto the battlefield. */
public record PutCardFromHandOrGraveyardOntoBattlefieldEffect(
        CardPredicate predicate, String label, CounterType enterWithCounter) implements CardEffect {
}
