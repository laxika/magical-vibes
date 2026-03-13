package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents the next N damage that a chosen source would deal to the controller and/or
 * permanents they control this turn, and deals that damage to any target instead.
 * The source is chosen on resolution (not a target). The redirect target is the spell's target.
 * Used by Harm's Way.
 *
 * @param amount the maximum amount of damage to prevent and redirect
 */
public record PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect(int amount) implements CardEffect {
    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
