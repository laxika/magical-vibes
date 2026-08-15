package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Looks at the top card of the controller's library. If it matches the predicate, the controller
 * may reveal it and put one counter of the specified type on the source permanent. The card stays
 * on top of the library.
 */
public record LookAtTopCardMayRevealMatchingAndPutCounterEffect(
        CardPredicate predicate,
        CounterType counterType
) implements CardEffect {
}
