package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevent the next {@code amount} damage that would be dealt to target creature this turn; at the
 * beginning of the next end step, put a +0/+1 counter on that creature for each 1 damage prevented
 * this way (Sacred Boon).
 * <p>
 * Resolution sets the target's {@code damageToCounterPreventionShield}. As that shield prevents
 * damage in {@code DamagePreventionService.applyCreaturePreventionShield}, the prevented amount is
 * accumulated into a {@code DelayedPlusZeroPlusOneCounters} keyed to the creature, which
 * {@code StepTriggerService} drains at the next end step.
 */
public record PreventNextDamageToTargetAndAddToughnessCountersEffect(int amount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
