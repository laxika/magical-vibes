package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Lets each player optionally put one matching card from their hand onto the battlefield.
 * Choices are made in active-player order and the chosen cards enter simultaneously.
 */
public record EachPlayerMayPutCardFromHandToBattlefieldEffect(CardPredicate predicate, String label)
        implements CardEffect {

    /** Show and Tell's artifact, creature, enchantment, or land choice. */
    public static EachPlayerMayPutCardFromHandToBattlefieldEffect showAndTell() {
        return new EachPlayerMayPutCardFromHandToBattlefieldEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        new CardTypePredicate(CardType.LAND))),
                "artifact, creature, enchantment, or land");
    }
}
