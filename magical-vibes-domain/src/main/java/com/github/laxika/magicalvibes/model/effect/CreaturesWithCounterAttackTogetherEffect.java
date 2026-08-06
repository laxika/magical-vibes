package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Static conditional attack requirement (Magnetic Web): if a creature with a {@code counterType}
 * counter on it attacks, all creatures with such counters on them attack if able.
 * <p>
 * Like {@link MustAttackIfAnotherCreatureAttacksEffect} this depends on the declared attacker set
 * rather than on static board state, so it is validated at attacker-declaration time in
 * {@code CombatAttackService} (CR 508.1d, which covers requirements saying a creature "attacks if
 * some condition is met") instead of contributing to the static must-attack requirement count.
 * Unlike that effect it lives on a separate permanent (an artifact) and applies to every creature
 * carrying the counter, so the validator scans all battlefields for the source permanent.
 */
public record CreaturesWithCounterAttackTogetherEffect(CounterType counterType) implements CardEffect {
}
