package com.github.laxika.magicalvibes.model.effect;


/**
 * Transforms the permanents {@code subject} names to their other face: the source (the default; used by double-faced
 * cards with the Transform keyword), target permanent ({@link Subjects#target()}, Elvish Vatkeeper), the permanent the
 * trigger event was about ({@link Subjects#triggering()}: "you may transform it", Vildin-Pack Alpha), the permanent that
 * granted the ability ({@link Subjects#granter()}: "convert Optimus Prime"), the permanents an earlier step of the
 * resolution named ({@link Subjects#thisWay}: "transform any number of Human Werewolves you control" after a
 * {@code ChoosePermanentsEffect}, Tovolar) or created ({@link Subjects#createdThisResolution()}: "conjure ... and you
 * may transform it", High Marshal Arguel). A permanent without another face isn't changed; transform prevention is
 * respected. Records {@code StepKind.TRANSFORM}: the permanents that transformed.
 *
 * @param subject what transforms; only {@link Subjects#target()} targets
 */
public record TransformSelfEffect(PermanentSubject subject) implements CardEffect {

    public TransformSelfEffect {
        if (!(subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Target
                || subject instanceof PermanentSubject.Triggering || subject instanceof PermanentSubject.Granter
                || subject instanceof PermanentSubject.ThisWay
                || subject instanceof PermanentSubject.CreatedThisResolution)) {
            throw new IllegalArgumentException("Transforms the source, the target, the triggering or granting permanent, "
                    + "or what an earlier step named: " + subject);
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
