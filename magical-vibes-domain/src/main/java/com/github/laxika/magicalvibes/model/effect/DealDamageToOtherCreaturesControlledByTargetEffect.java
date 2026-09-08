package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to each creature controlled by the targeted creature's
 * controller, except for the targeted creature itself.
 */
public record DealDamageToOtherCreaturesControlledByTargetEffect(int damage) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
