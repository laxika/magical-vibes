package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Conditional wrapper: the wrapped effect only fires if at least {@code minCount} permanents
 * matching {@code predicate} have entered the battlefield under the affected player's control
 * this turn (checked via {@code GameData.permanentsEnteredBattlefieldThisTurn}).
 */
public record PermanentEnteredThisTurnConditionalEffect(
        CardEffect wrapped,
        CardPredicate predicate,
        int minCount
) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }
}
