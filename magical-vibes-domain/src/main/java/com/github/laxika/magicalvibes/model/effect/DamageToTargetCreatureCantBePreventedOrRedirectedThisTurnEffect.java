package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks the target creature so damage that would be dealt to it this turn cannot be prevented
 * or redirected to another recipient.
 */
public record DamageToTargetCreatureCantBePreventedOrRedirectedThisTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
