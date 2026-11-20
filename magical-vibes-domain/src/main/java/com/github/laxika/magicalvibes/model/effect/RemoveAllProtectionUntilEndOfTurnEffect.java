package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Removes all protection abilities from permanents in the selected subject until end of turn.
 */
public record RemoveAllProtectionUntilEndOfTurnEffect(PermanentSubject subject, PermanentPredicate filter)
        implements CardEffect {

    public RemoveAllProtectionUntilEndOfTurnEffect(PermanentSubject subject) {
        this(subject, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            case PermanentSubject.Target ignored -> TargetSpec.benign(TargetPredicates.permanent(), filter);
            case PermanentSubject.Source ignored -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
