package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Non-targeted mass version of {@link MustBlockSourceEffect} (Magnetic Web): every creature on the
 * battlefield with a {@code counterType} counter on it must block the triggering attacker this turn
 * if able. The attacker is the stack entry's non-targeting {@code targetId}, baked in by
 * {@code CombatAttackService} when the {@code ON_ANY_CREATURE_ATTACKS} trigger is put on the stack.
 * <p>
 * Adds the attacker's id to each matching creature's {@code Permanent.mustBlockIds}, so enforcement
 * (and the vacuous satisfaction when a creature can't legally block that attacker) is the shared
 * {@code CombatBlockService.validatePerCreatureMustBlockRequirements} path (CR 509.1c). Cleared at
 * end of turn by {@code resetModifiers}.
 */
public record CreaturesWithCounterMustBlockTriggeringAttackerEffect(CounterType counterType) implements CardEffect {
}
