package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedules the permanents {@code subject} names to be returned to their owners' hands at the beginning of the next
 * end step: the target by default (Dragon Mask's "Return it to its owner's hand at the beginning of the next end
 * step"), or {@code source()} for "return this artifact to its owner's hand at the beginning of the next end step".
 * The delayed action does nothing to a permanent that has left the battlefield by then.
 *
 * @param subject which permanents; {@code null} normalises to {@code target()}
 */
public record ReturnTargetPermanentToHandAtEndStepEffect(PermanentSubject subject) implements CardEffect {

    public ReturnTargetPermanentToHandAtEndStepEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
    }

    public ReturnTargetPermanentToHandAtEndStepEffect() {
        this(Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }
}
