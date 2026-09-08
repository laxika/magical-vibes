package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Offers the selected card, which is currently in hand, for the battlefield. */
public record MayPutSelectedCardOntoBattlefieldEffect(
        int manaValueAtMost,
        CardPredicate predicate,
        boolean tapped,
        boolean grantHaste
) implements CardEffect {

    /** Offers a creature with the given mana value for the battlefield with haste. */
    public MayPutSelectedCardOntoBattlefieldEffect(int manaValueAtMost) {
        this(manaValueAtMost, new CardTypePredicate(CardType.CREATURE), false, true);
    }

    /** Offers any selected card matching {@code predicate}, optionally entering tapped. */
    public static MayPutSelectedCardOntoBattlefieldEffect forCard(
            CardPredicate predicate, boolean tapped) {
        return new MayPutSelectedCardOntoBattlefieldEffect(Integer.MAX_VALUE, predicate, tapped, false);
    }
}
