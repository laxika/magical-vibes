package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns a targeted permanent to its owner's hand, then resolves a bonus effect
 * if the permanent's mana value was at or below a threshold.
 *
 * @param maxManaValue     the maximum mana value for the bonus effect to trigger
 * @param conditionalEffect the bonus effect to resolve when the condition is met
 */
public record ReturnTargetPermanentToHandWithManaValueConditionalEffect(
        int maxManaValue, CardEffect conditionalEffect) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
