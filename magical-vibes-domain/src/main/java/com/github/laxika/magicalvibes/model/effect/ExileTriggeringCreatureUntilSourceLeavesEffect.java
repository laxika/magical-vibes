package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the creature that caused the trigger and links it to the source permanent, so it returns
 * under its owner's control when that source leaves the battlefield.
 *
 * @param minimumOtherCreatures the minimum number of other creatures required by the intervening
 *                              condition
 */
public record ExileTriggeringCreatureUntilSourceLeavesEffect(int minimumOtherCreatures)
        implements CardEffect {

    public ExileTriggeringCreatureUntilSourceLeavesEffect() {
        this(2);
    }
}
