package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Trigger marker for an attached permanent effect that only fires when its combat opponent
 * matches a permanent predicate. The combat trigger collector evaluates the predicate when the
 * combat event occurs and unwraps the effect before putting it on the stack.
 */
public interface CombatOpponentFilterEffect extends CardEffect {

    PermanentPredicate opponentFilter();

    CardEffect wrapped();
}
