package com.github.laxika.magicalvibes.model.effect;

/**
 * Static conditional attack requirement (Ekundu Cyclops): if another creature its controller
 * controls attacks, this creature also attacks if able.
 * <p>
 * The requirement only applies when at least one other creature is declared as an attacker in
 * the same combat, so it depends on the declaration itself rather than on static board state and
 * cannot be expressed through the unconditional {@code MustAttackEffect} requirement count. It is
 * validated at attacker-declaration time in {@code CombatAttackService} (CR 508.1d, which covers
 * requirements that say a creature "attacks if some condition is met").
 */
public record MustAttackIfAnotherCreatureAttacksEffect() implements CardEffect {
}
