package com.github.laxika.magicalvibes.model.effect;

/**
 * STATIC self-only "this creature blocks each combat if able" (Watchdog): whenever its controller
 * is the defending player, the creature must be declared as a blocker of some attacker it can
 * legally block. Unlike {@link SetCombatRequirementThisTurnEffect} with
 * {@link CombatRequirement#MUST_BLOCK} (a one-shot that sets
 * {@code Permanent.mustBlockThisTurnIfAble} for the current turn only), this is a permanent
 * requirement read off the creature's own static effects every combat. Enforced together with the
 * one-shot flag in {@code CombatBlockService.validateMustBlockIfAbleRequirements}; satisfied
 * vacuously when the creature can't legally block any declared attacker.
 */
public record MustBlockEachCombatEffect() implements CardEffect {
}
