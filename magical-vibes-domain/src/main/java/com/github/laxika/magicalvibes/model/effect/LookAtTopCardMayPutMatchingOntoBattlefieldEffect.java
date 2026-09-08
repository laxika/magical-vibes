package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Look at the top card of the controller's library. If it matches {@code predicate}, the controller
 * may put that card onto the battlefield. Otherwise (or if declined), it stays on top of the library.
 *
 * <p>Used by Into the Wilds with {@code CardTypePredicate(LAND)}.
 */
public record LookAtTopCardMayPutMatchingOntoBattlefieldEffect(CardPredicate predicate,
                                                                 boolean enterTapped)
        implements CardEffect {

    public LookAtTopCardMayPutMatchingOntoBattlefieldEffect(CardPredicate predicate) {
        this(predicate, false);
    }
}
