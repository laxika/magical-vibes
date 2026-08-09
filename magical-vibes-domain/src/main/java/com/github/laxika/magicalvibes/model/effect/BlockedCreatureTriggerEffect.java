package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * A conditional trigger in the global {@code ON_ANY_CREATURE_BECOMES_BLOCKED} slot. The combat
 * collector evaluates the predicate against each blocked attacker and queues the wrapped effect
 * once per matching attacker, rather than once per blocker.
 */
public interface BlockedCreatureTriggerEffect extends CardEffect {

    CardPredicate predicate();

    CardEffect wrapped();
}
