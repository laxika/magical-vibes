package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top card of the controller's library. If it matches {@code predicate}, the
 * controller may put it onto the battlefield; otherwise, or if they decline, put it into hand.
 */
public record RevealTopCardMayPutMatchingOntoBattlefieldElseToHandEffect(
        CardPredicate predicate
) implements CardEffect {
}
