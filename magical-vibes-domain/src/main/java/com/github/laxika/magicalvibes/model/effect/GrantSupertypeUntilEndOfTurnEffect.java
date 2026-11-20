package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/**
 * Gives or removes a supertype on a permanent until the end of the turn.
 */
public record GrantSupertypeUntilEndOfTurnEffect(CardSupertype supertype, PermanentSubject subject,
                                                 boolean gained)
        implements CardEffect {

    public GrantSupertypeUntilEndOfTurnEffect(CardSupertype supertype, PermanentSubject subject) {
        this(supertype, subject, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            case PermanentSubject.Source ignored -> new TargetSpec(null, false, null, true, 1);
            case PermanentSubject.Target ignored -> TargetSpec.benign(TargetPredicates.permanent());
            default -> TargetSpec.NONE;
        };
    }
}
