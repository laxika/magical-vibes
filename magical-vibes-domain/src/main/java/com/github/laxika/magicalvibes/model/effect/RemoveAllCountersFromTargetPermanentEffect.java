package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Remove all counters from {target permanent | target creature | enchanted creature}."
 *
 * <p>Removes every counter of every concrete type from each permanent {@code subject} names and records the number
 * removed as the stack entry's event value for a following {@code EventValue} effect. This effect does not prevent
 * the permanent from receiving counters again after resolution.</p>
 *
 * <p>{@link Subjects#target()} targets a permanent, or a permanent matching {@code targetFilter} ({@link
 * #fromTargetCreature()}: "target creature"). {@link Subjects#attached()} acts on the source Aura's host without
 * targeting it, so hexproof or protection does not stop it (Enchanted River's Grasp). A filter needs a target
 * subject.</p>
 */
public record RemoveAllCountersFromTargetPermanentEffect(PermanentSubject subject, PermanentPredicate targetFilter)
        implements CardEffect {

    public RemoveAllCountersFromTargetPermanentEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
        if (targetFilter != null && !subject.declaresTarget()) {
            throw new IllegalArgumentException("A target filter needs a target subject: " + subject);
        }
    }

    /** "Remove all counters from target permanent." */
    public RemoveAllCountersFromTargetPermanentEffect() {
        this(Subjects.target(), null);
    }

    /** "Remove all counters from {subject}", which it targets only if {@code subject} is a target. */
    public RemoveAllCountersFromTargetPermanentEffect(PermanentSubject subject) {
        this(subject, null);
    }

    /** "Remove all counters from target creature." */
    public static RemoveAllCountersFromTargetPermanentEffect fromTargetCreature() {
        return new RemoveAllCountersFromTargetPermanentEffect(Subjects.target(), new PermanentIsCreaturePredicate());
    }

    @Override
    public TargetSpec targetSpec() {
        if (!subject.declaresTarget()) {
            return TargetSpec.NONE;
        }
        return TargetSpec.benign(targetFilter == null ? TargetPredicates.permanent()
                : TargetPredicates.permanents(targetFilter));
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
    }
}
