package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents the next N damage that would be dealt to the spell's target this turn
 * by a source of the controller's choice. The source is chosen on resolution (not a target).
 * Used by Healing Grace.
 *
 * @param amount the maximum amount of damage to prevent
 */
public record PreventDamageToTargetFromChosenSourceEffect(int amount) implements CardEffect {
    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
