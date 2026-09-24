package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Removes all protection abilities from permanents in the selected scope until end of turn.
 */
public record RemoveAllProtectionUntilEndOfTurnEffect(GrantScope scope, PermanentPredicate filter)
        implements CardEffect {

    public RemoveAllProtectionUntilEndOfTurnEffect(GrantScope scope) {
        this(scope, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET -> TargetSpec.benign(TargetPredicates.permanent(), filter);
            case SELF -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
