package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents the next damage event that would be dealt by the target creature this turn.
 *
 * <p>The target creature is the damage source, so the shield applies regardless of the recipient
 * of that damage. When {@code gainLife} is true, the controller gains life equal to the damage prevented.</p>
 */
public record PreventNextDamageByTargetCreatureEffect(boolean gainLife) implements CardEffect {

    public PreventNextDamageByTargetCreatureEffect() {
        this(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
