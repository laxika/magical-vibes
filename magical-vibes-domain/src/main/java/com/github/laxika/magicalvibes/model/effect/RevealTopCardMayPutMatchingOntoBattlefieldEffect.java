package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top card of the relevant player's library. If it matches {@code predicate}, that
 * player may put it onto the battlefield; otherwise, or if they decline, it remains on top.
 */
public record RevealTopCardMayPutMatchingOntoBattlefieldEffect(CardPredicate predicate)
        implements CardEffect {
}
