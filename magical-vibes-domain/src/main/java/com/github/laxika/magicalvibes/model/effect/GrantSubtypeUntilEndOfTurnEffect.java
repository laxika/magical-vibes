package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additively grants a creature subtype for the specified duration.
 *
 * <p>The affected permanents are captured when the effect resolves. The normal effect handler
 * records the grant as a temporary continuous effect so it participates in the layered subtype
 * system and preserves the permanents' existing creature types.</p>
 *
 * @param subtype the creature subtype to grant
 * @param subject the permanent or player subject receiving the subtype
 * @param duration how long the subtype grant applies
 */
public record GrantSubtypeUntilEndOfTurnEffect(CardSubtype subtype, PermanentSubject subject,
                                               PermanentPredicate filter, Duration duration) implements CardEffect {

    public GrantSubtypeUntilEndOfTurnEffect(CardSubtype subtype, PermanentSubject subject,
                                           PermanentPredicate filter) {
        this(subtype, subject, filter, Duration.UNTIL_END_OF_TURN);
    }

    public GrantSubtypeUntilEndOfTurnEffect(CardSubtype subtype, PermanentSubject subject) {
        this(subtype, subject, null);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresPlayerTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        if (subject.isSet()) {
            return TargetSpec.NONE;
        }
        return subject instanceof PermanentSubject.Source
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.benign(TargetPredicates.creature());
    }
}
