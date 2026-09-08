package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the two targeted creatures when their effective color sets are exactly equal.
 * Both targets are destroyed without allowing regeneration.
 */
public record DestroyTwoTargetCreaturesIfSameColorsEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
