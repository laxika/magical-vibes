package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevent the next {@code amount} damage that would be dealt to the target this turn; if the target
 * is a creature, at the beginning of the next end step put a +0/+1 counter on it for each 1 damage
 * prevented this way (Sacred Boon / Scars of the Veteran).
 * <p>
 * When {@code anyTarget} is false (Sacred Boon), only creatures are legal. When true (Scars of the
 * Veteran), any target is legal: creatures get the counter-tracking shield; players get a plain
 * next-N damage prevention shield with no counters.
 * <p>
 * Creature resolution sets the target's {@code damageToCounterPreventionShield}. As that shield
 * prevents damage in {@code DamagePreventionService}, the prevented amount is accumulated into a
 * {@code DelayedPlusZeroPlusOneCounters} keyed to the creature, which the delayed triggered ability this
 * effect makes (CR 603.7a) reads as it resolves at the next end step.
 */
public record PreventNextDamageToTargetAndAddToughnessCountersEffect(int amount, boolean anyTarget)
        implements CardEffect {

    /** Creature-only (Sacred Boon). */
    public PreventNextDamageToTargetAndAddToughnessCountersEffect(int amount) {
        this(amount, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return anyTarget
                ? TargetSpec.benign(TargetPredicates.anyTarget())
                : TargetSpec.benign(TargetPredicates.creature());
    }
}
