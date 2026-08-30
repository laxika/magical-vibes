package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * A requirement that makes matching creatures attack each combat if able.
 *
 * <p>The predicate is evaluated live from the effect controller's perspective, so a requirement
 * can cover creatures that enter the battlefield after it resolves.</p>
 */
public interface CombatAttackRequirementEffect extends CardEffect {

    PermanentPredicate affectedPredicate();

    /** Whether a legal attack must target a player other than the effect controller, if possible. */
    default boolean requiresAttackAtOtherPlayerIfAble() {
        return false;
    }
}
