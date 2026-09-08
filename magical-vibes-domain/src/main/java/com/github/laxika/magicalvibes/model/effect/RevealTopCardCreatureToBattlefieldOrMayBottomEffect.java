package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Reveals the top card of the controller's library. If it matches {@code predicate}, put it onto
 * the battlefield, either mandatorily or optionally according to {@code mayPutMatching}. Otherwise,
 * the controller may put that card on the bottom of their library.
 *
 * <p>The no-argument form preserves Lurking Predators' mandatory creature-card behavior. The
 * predicate form also supports other matching card classes, such as permanent cards; set
 * {@code mayPutMatching} for effects such as Aid from the Cowl.
 */
public record RevealTopCardCreatureToBattlefieldOrMayBottomEffect(CardPredicate predicate,
                                                                   boolean mayPutMatching)
        implements CardEffect {

    public RevealTopCardCreatureToBattlefieldOrMayBottomEffect() {
        this(new CardTypePredicate(CardType.CREATURE), false);
    }

    public RevealTopCardCreatureToBattlefieldOrMayBottomEffect(CardPredicate predicate) {
        this(predicate, false);
    }
}
