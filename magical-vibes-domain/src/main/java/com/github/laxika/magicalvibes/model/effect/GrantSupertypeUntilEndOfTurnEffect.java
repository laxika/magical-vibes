package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/**
 * Gives or removes a supertype on a permanent until the end of the turn.
 */
public record GrantSupertypeUntilEndOfTurnEffect(CardSupertype supertype, GrantScope scope,
                                                 boolean gained)
        implements CardEffect {

    public GrantSupertypeUntilEndOfTurnEffect(CardSupertype supertype, GrantScope scope) {
        this(supertype, scope, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case SELF -> new TargetSpec(null, false, null, true, 1);
            case TARGET -> TargetSpec.benign(TargetPredicates.permanent());
            default -> TargetSpec.NONE;
        };
    }
}
