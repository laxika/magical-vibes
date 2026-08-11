package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Look at the top card of the controller's library. If it matches {@code predicate}, the
 * controller may put it onto the battlefield; otherwise, or if they decline, put it into their
 * hand.
 */
public record LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect(
        CardPredicate predicate,
        boolean tapped
) implements CardEffect {
}
