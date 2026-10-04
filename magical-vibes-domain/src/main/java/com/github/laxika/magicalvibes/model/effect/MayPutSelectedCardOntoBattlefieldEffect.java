package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Offers a selected card for the battlefield, holding a revealed card out until the decision. */
public record MayPutSelectedCardOntoBattlefieldEffect(
        int manaValueAtMost,
        CardPredicate predicate,
        boolean tapped,
        boolean grantHaste,
        Card chosenCard
) implements ChosenCardAwareEffect {

    public MayPutSelectedCardOntoBattlefieldEffect(int manaValueAtMost, CardPredicate predicate,
                                                   boolean tapped, boolean grantHaste) {
        this(manaValueAtMost, predicate, tapped, grantHaste, null);
    }

    @Override
    public CardEffect withChosenCard(Card card) {
        return new MayPutSelectedCardOntoBattlefieldEffect(manaValueAtMost, predicate, tapped, grantHaste, card);
    }

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
