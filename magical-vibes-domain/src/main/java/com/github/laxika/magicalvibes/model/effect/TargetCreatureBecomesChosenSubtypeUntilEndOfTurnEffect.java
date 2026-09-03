package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: the target, or every creature on the battlefield for the all-creatures scope,
 * becomes the creature type chosen at resolution until end of turn, replacing all other creature
 * types.
 */
public record TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(GrantScope scope) implements CardEffect {

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect() {
        this(GrantScope.TARGET);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.ALL_CREATURES
                ? TargetSpec.NONE
                : TargetSpec.benign(TargetPredicates.creature());
    }
}
