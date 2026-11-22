package com.github.laxika.magicalvibes.model.effect;


/**
 * Transforms the permanent {@code subject} names to its other face: the source (the default; used by double-faced cards
 * with the Transform keyword), target permanent ({@link Subjects#target()}, Elvish Vatkeeper) or the permanent the
 * trigger event was about ({@link Subjects#triggering()}: "you may transform it", Vildin-Pack Alpha). A permanent
 * without another face isn't changed; transform prevention is respected.
 *
 * @param subject what transforms; only {@link Subjects#target()} targets
 */
public record TransformSelfEffect(PermanentSubject subject) implements CardEffect {

    public TransformSelfEffect {
        if (!(subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Target
                || subject instanceof PermanentSubject.Triggering)) {
            throw new IllegalArgumentException("Transforms the source, the target or the triggering permanent: " + subject);
        }
    }

    public TransformSelfEffect() {
        this(Subjects.source());
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        // Marks the ability as self-affecting so trigger collectors stamp sourcePermanentId
        // (needed when TransformSelf is nested in a cast trigger's effects or a SequenceEffect).
        return subject instanceof PermanentSubject.Source ? new TargetSpec(null, false, null, true, 1) : TargetSpec.NONE;
    }
}
