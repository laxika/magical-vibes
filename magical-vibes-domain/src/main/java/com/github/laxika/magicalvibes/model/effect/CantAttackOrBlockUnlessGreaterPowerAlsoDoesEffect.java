package com.github.laxika.magicalvibes.model.effect;

/**
 * Static combat restriction (Okk): this creature can't attack unless a creature with greater power
 * also attacks, and can't block unless a creature with greater power also blocks.
 * <p>
 * Both sides depend on the set of creatures declared in the same combat, not on static board state,
 * so this cannot be expressed as a {@code Condition}. The attack side is validated at
 * attacker-declaration time in {@code CombatAttackService} and the block side at blocker-declaration
 * time in {@code CombatBlockService}, each against the other creatures declared in that combat.
 * <p>
 * Per the Okk ruling, the greater-power comparison is checked only as attackers/blockers are
 * declared — a later power change does not remove the creature from combat.
 */
public record CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect() implements CardEffect {
}
