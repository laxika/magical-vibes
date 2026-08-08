package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * Prevents the next N damage that a chosen source would deal to the controller and/or
 * permanents they control this turn, and deals that damage to any target instead.
 * The source is chosen on resolution (not a target). The redirect target is the spell's target.
 * Used by Harm's Way and Shining Shoal.
 *
 * @param amount the maximum amount of damage to prevent and redirect
 */
public record PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect(DynamicAmount amount) implements CardEffect {

    /** Fixed amount ("the next 2 damage" — Harm's Way). */
    public PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect(int amount) {
        this(new Fixed(amount));
    }

    /** "The next X damage …" — X as chosen when the spell was cast (Shining Shoal). */
    public static PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect forX() {
        return new PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect(new XValue());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}
