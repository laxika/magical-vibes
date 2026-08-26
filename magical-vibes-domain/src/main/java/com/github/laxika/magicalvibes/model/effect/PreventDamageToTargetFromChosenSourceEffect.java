package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents damage that would be dealt to the spell's target this turn by a source of the
 * controller's choice. The source is chosen on resolution (not a target).
 *
 * @param amount the maximum amount of damage to prevent when {@code allDamage} is false
 * @param allDamage whether to prevent all matching damage this turn
 */
public record PreventDamageToTargetFromChosenSourceEffect(int amount, boolean allDamage) implements CardEffect {
    public PreventDamageToTargetFromChosenSourceEffect(int amount) {
        this(amount, false);
    }

    public static PreventDamageToTargetFromChosenSourceEffect allDamageToTarget() {
        return new PreventDamageToTargetFromChosenSourceEffect(0, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyTarget());
    }
}
