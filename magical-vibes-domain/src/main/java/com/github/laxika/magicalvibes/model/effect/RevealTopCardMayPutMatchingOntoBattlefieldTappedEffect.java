package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Offers to reveal the top card of the controller's library. If it matches the predicate, it is
 * put onto the battlefield tapped; otherwise it remains on top.
 */
public record RevealTopCardMayPutMatchingOntoBattlefieldTappedEffect(CardPredicate predicate)
        implements CardEffect {
}
