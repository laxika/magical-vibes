package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents the next damage event that would be dealt by the target creature this turn.
 *
 * <p>The target creature is the damage source, so the shield applies regardless of the recipient
 * of that damage. The controller of the resolving effect gains life equal to the damage prevented.</p>
 */
public record PreventNextDamageByTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
