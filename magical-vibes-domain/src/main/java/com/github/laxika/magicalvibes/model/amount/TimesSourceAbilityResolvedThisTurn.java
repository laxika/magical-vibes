package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of times the source permanent's resolution-counted ability has resolved this turn.
 * The count includes the current resolution and is zero outside stack resolution.
 */
public record TimesSourceAbilityResolvedThisTurn() implements DynamicAmount {

    @Override
    public boolean requiresAbilityResolutionCount() {
        return true;
    }
}
