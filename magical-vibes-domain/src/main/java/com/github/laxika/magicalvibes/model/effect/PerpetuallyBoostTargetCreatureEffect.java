package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Perpetually modifies the power and toughness of a target creature's runtime card. */
public record PerpetuallyBoostTargetCreatureEffect(int powerBoost, int toughnessBoost,
                                                   PermanentPredicate filter, PermanentSubject subject)
        implements CardEffect {

    public PerpetuallyBoostTargetCreatureEffect(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        this(powerBoost, toughnessBoost, filter, Subjects.target());
    }

    /** "It perpetually gets +X/+Y": the creature {@code subject} names, with no target of its own. */
    public PerpetuallyBoostTargetCreatureEffect(PermanentSubject subject, int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, null, subject);
    }

    public PerpetuallyBoostTargetCreatureEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, new PermanentControlledBySourceControllerPredicate());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature(), filter) : TargetSpec.NONE;
    }
}
