package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys a targeted permanent, then resolves a bonus effect if the permanent's
 * mana value was at or below a threshold.
 *
 * @param maxManaValue     the maximum mana value for the bonus effect to trigger
 * @param conditionalEffect the bonus effect to resolve when the condition is met
 */
public record DestroyTargetPermanentWithManaValueConditionalEffect(
        int maxManaValue, CardEffect conditionalEffect) implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
