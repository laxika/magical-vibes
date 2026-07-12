package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals {@code damage} to the attacking creature that caused this attack trigger (the permanent
 * referenced by the stack entry's non-targeting {@code targetId}, populated by the engine for
 * {@code ON_CREATURE_ATTACKS_YOU} triggers).
 *
 * <p>The {@code attackerCondition} restricts <em>which</em> attackers cause the trigger to fire:
 * {@code CombatAttackService} evaluates it against the attacker when attackers are declared and
 * only pushes the trigger for matching attackers. The condition is therefore checked at
 * declaration time (rules-correct), and this effect deals the damage unconditionally on
 * resolution.
 *
 * <p>Used by Raking Canopy ("Whenever a creature with flying attacks you, this enchantment deals
 * 4 damage to it.") with a {@code PermanentHasKeywordPredicate(FLYING)} condition.
 */
public record DealDamageToTriggeringAttackerEffect(int damage, PermanentPredicate attackerCondition)
        implements CardEffect {
}
