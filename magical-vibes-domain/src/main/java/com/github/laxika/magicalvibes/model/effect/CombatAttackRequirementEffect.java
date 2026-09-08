package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * A requirement that makes matching creatures attack each combat if able.
 *
 * <p>The predicate is evaluated live from the effect controller's perspective, so a requirement
 * can cover creatures that enter the battlefield after it resolves.</p>
 */
public interface CombatAttackRequirementEffect extends CardEffect {

    PermanentPredicate affectedPredicate();

    /** Whether this static requirement is currently active for its source permanent. */
    default boolean isActive(GameData gameData, Permanent sourcePermanent) {
        return true;
    }

    /**
     * The player or permanent that a matching creature must attack, or {@code null} when any
     * legal attack target satisfies the requirement.
     */
    default UUID requiredAttackTargetId(GameData gameData, Permanent sourcePermanent) {
        return null;
    }

    /** Whether a legal attack must target a player other than the effect controller, if possible. */
    default boolean requiresAttackAtOtherPlayerIfAble() {
        return false;
    }
}
