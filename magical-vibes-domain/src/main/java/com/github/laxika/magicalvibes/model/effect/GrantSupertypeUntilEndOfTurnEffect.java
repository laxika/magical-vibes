package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/**
 * Grants a supertype to a permanent until the end of the turn.
 */
public record GrantSupertypeUntilEndOfTurnEffect(CardSupertype supertype, GrantScope scope)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case SELF -> new TargetSpec(null, false, null, true, 1);
            case TARGET -> TargetSpec.benign(TargetPredicates.permanent());
            default -> TargetSpec.NONE;
        };
    }
}
