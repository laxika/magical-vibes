package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Records whether the card returned by a preceding {@link ReturnCardFromGraveyardEffect}
 * matches a predicate as the resolving spell's event value.
 *
 * <p>The effect records {@code 1} for a matching card and {@code 0} otherwise. It is left
 * unbound so the resolver keeps the graveyard card on {@code entry.targetId}.</p>
 *
 * @param predicate the characteristic the returned card must match
 */
public record RecordReturnedGraveyardCardMatchEffect(CardPredicate predicate) implements CardEffect {
}
