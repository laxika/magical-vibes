package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles every token on the battlefield with the name of the nontoken creature that caused the
 * source's leave trigger.
 */
public record ExileTokensWithSameNameAsLeavingCreatureEffect(String creatureName)
        implements CardEffect, LeavingCreatureNameAwareEffect {

    public ExileTokensWithSameNameAsLeavingCreatureEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToLeavingCreatureName(String creatureName) {
        return new ExileTokensWithSameNameAsLeavingCreatureEffect(creatureName);
    }
}
