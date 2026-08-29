package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Lets each player, or each player other than the effect's controller, optionally put one matching
 * card from their hand onto the battlefield. Choices are made in active-player order and the
 * chosen cards normally enter simultaneously. The repeating mode puts each chosen card onto the
 * battlefield immediately and starts another round beginning with the effect's controller after
 * any card enters.
 */
public record EachPlayerMayPutCardFromHandToBattlefieldEffect(CardPredicate predicate, String label,
                                                              boolean opponentsOnly,
                                                              boolean repeatUntilNoOne,
                                                              boolean startsWithController)
        implements CardEffect {

    public EachPlayerMayPutCardFromHandToBattlefieldEffect(CardPredicate predicate, String label) {
        this(predicate, label, false, false, false);
    }

    public EachPlayerMayPutCardFromHandToBattlefieldEffect(CardPredicate predicate, String label,
                                                            boolean opponentsOnly) {
        this(predicate, label, opponentsOnly, false, false);
    }

    /** Show and Tell's artifact, creature, enchantment, or land choice. */
    public static EachPlayerMayPutCardFromHandToBattlefieldEffect showAndTell() {
        return new EachPlayerMayPutCardFromHandToBattlefieldEffect(permanentCardPredicate(),
                "artifact, creature, enchantment, or land");
    }

    /** Hypergenesis's repeating, sequential artifact, creature, enchantment, or land choice. */
    public static EachPlayerMayPutCardFromHandToBattlefieldEffect hypergenesis() {
        return new EachPlayerMayPutCardFromHandToBattlefieldEffect(permanentCardPredicate(),
                "artifact, creature, enchantment, or land", false, true, true);
    }

    public static EachPlayerMayPutCardFromHandToBattlefieldEffect eachOpponent(CardPredicate predicate,
                                                                                 String label) {
        return new EachPlayerMayPutCardFromHandToBattlefieldEffect(predicate, label, true);
    }

    private static CardPredicate permanentCardPredicate() {
        return new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardTypePredicate(CardType.LAND)));
    }
}
