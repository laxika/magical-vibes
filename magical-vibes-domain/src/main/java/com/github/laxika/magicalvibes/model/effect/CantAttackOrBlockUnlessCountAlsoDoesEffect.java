package com.github.laxika.magicalvibes.model.effect;

/**
 * Static combat restriction (Orcish Conscripts): this creature can't attack unless at least
 * {@code otherCount} other creatures also attack, and can't block unless at least
 * {@code otherCount} other creatures also block.
 * <p>
 * Both sides depend on the set of creatures declared in the same combat, not on static board
 * state, so this cannot be expressed as a {@code Condition}. The attack side is validated at
 * attacker-declaration time in {@code CombatAttackService} and the block side at
 * blocker-declaration time in {@code CombatBlockService}, each against the other creatures
 * declared in that combat.
 */
public record CantAttackOrBlockUnlessCountAlsoDoesEffect(int otherCount) implements CardEffect {
}
