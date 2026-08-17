package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Lets each player, or each player other than the effect's controller, optionally put one matching
 * card from their hand onto the battlefield. Choices are made in active-player order and the
 * chosen cards enter simultaneously.
 */
public record EachPlayerMayPutCardFromHandToBattlefieldEffect(CardPredicate predicate, String label,
                                                              boolean opponentsOnly)
        implements CardEffect {

    public EachPlayerMayPutCardFromHandToBattlefieldEffect(CardPredicate predicate, String label) {
        this(predicate, label, false);
    }

    /** Show and Tell's artifact, creature, enchantment, or land choice. */
    public static EachPlayerMayPutCardFromHandToBattlefieldEffect showAndTell() {
        return new EachPlayerMayPutCardFromHandToBattlefieldEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        new CardTypePredicate(CardType.LAND))),
                "artifact, creature, enchantment, or land", false);
    }

    public static EachPlayerMayPutCardFromHandToBattlefieldEffect eachOpponent(CardPredicate predicate,
                                                                                 String label) {
        return new EachPlayerMayPutCardFromHandToBattlefieldEffect(predicate, label, true);
    }
}
