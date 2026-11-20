package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Effect: creatures lose all creature types. The
 * {@link PermanentSubject} selects who is affected: {@link Subjects#target()} strips the single
 * targeted creature (e.g. Amoeboid Changeling's second ability), while
 * {@link Subjects#creaturesTargetPlayerControls()} strips every creature the targeted player
 * controls (e.g. Ego Erasure). In a static slot, the effect continuously replaces creature
 * subtypes for matching permanents. The one-shot path sets
 * {@code Permanent.losesAllCreatureTypesUntilEndOfTurn}, which makes every creature subtype
 * (base, transient, granted) read as absent and nullifies the Changeling keyword's type grant.
 * The inverse "gains all creature types" is {@code GrantKeywordEffect(Keyword.CHANGELING, subject)}.
 */
public record LoseAllCreatureTypesEffect(PermanentSubject subject, PermanentPredicate filter) implements CardEffect {

    /** Single targeted creature loses all creature types (Amoeboid Changeling). */
    public LoseAllCreatureTypesEffect() {
        this(Subjects.target(), null);
    }

    public LoseAllCreatureTypesEffect(PermanentSubject subject) {
        this(subject, null);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject instanceof PermanentSubject.Target) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        if (subject.declaresPlayerTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return TargetSpec.NONE;
    }
}
