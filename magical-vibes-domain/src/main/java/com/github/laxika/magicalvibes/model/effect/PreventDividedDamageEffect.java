package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "Prevent the next N damage that would be dealt this turn to any number of targets, divided as you
 * choose." (Remedy).
 *
 * <p>The reverse of {@link DealDividedDamageEffect} CHOSEN mode: the controller announces per-target
 * amounts (summing to {@code amount}) that ride on {@code StackEntry.damageAssignments}, or on the
 * pending ETB assignments for an enter-the-battlefield trigger. The handler adds a "next X damage"
 * prevention shield to each target (creatures and/or players). Each target needs at least 1, so
 * {@code amount} is the effective cap on the number of targets. The shields expire at end of turn
 * (cleared in turn cleanup like every prevention shield).
 */
public record PreventDividedDamageEffect(DynamicAmount amount, boolean etbAssignments) implements CardEffect {

    public PreventDividedDamageEffect(DynamicAmount amount) {
        this(amount, false);
    }

    public PreventDividedDamageEffect(int amount) {
        this(new Fixed(amount), false);
    }

    public static PreventDividedDamageEffect chosenAmongAnyTargetsEtb(int amount) {
        return new PreventDividedDamageEffect(new Fixed(amount), true);
    }

    // "Any number of targets" is "any target" (CR 115.4): a creature, player or planeswalker. The
    // per-target amounts ride on StackEntry.damageAssignments, so the validated targetId is null;
    // that tolerance comes from EffectResolution.distributesAmountsAmongTargets.
    @Override
    public TargetSpec targetSpec() {
        if (etbAssignments) {
            return TargetSpec.NONE;
        }
        return TargetSpec.benign(TargetPredicates.anyTarget());
    }
}
