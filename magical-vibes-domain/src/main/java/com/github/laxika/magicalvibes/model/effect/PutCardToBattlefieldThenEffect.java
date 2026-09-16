package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets a player choose a matching card from their hand to put onto the battlefield, then resolves
 * {@code thenEffect} if the chosen card matches {@code thenCondition}. The selected card may enter
 * tapped and/or attacking.
 */
public record PutCardToBattlefieldThenEffect(
        CardPredicate predicate,
        String label,
        boolean enterTapped,
        boolean enterAttacking,
        CardPredicate thenCondition,
        CardEffect thenEffect
) implements CardEffect {

    public PutCardToBattlefieldThenEffect(CardPredicate predicate, String label,
                                          CardPredicate thenCondition, CardEffect thenEffect) {
        this(predicate, label, false, false, thenCondition, thenEffect);
    }

    public PutCardToBattlefieldThenEffect(CardPredicate predicate, String label,
                                          boolean enterTapped, CardPredicate thenCondition,
                                          CardEffect thenEffect) {
        this(predicate, label, enterTapped, false, thenCondition, thenEffect);
    }
}
