package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Looks at the top card of the controller's library. If it matches the predicate, the controller
 * may reveal it and put one counter of the specified type on the source permanent. The card stays
 * on top of the library. The look itself is optional, so cards wrap this effect in a
 * {@link MayEffect} to offer that first choice; the reveal choice is offered only after the look.
 */
public record LookAtTopCardMayRevealMatchingAndPutCounterEffect(
        CardPredicate predicate,
        CounterType counterType
) implements CardEffect {
}
