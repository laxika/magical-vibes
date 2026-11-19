package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedules the permanents {@code subject} names to be returned to their owners' hands at end of combat: the target
 * by default, or {@code source()} for "return this creature to its owner's hand at end of combat". The delayed action
 * does nothing to a permanent that has left the battlefield by then.
 *
 * @param subject which permanents; {@code null} normalises to {@code target()}
 */
public record ReturnTargetPermanentToHandAtEndOfCombatEffect(PermanentSubject subject) implements CardEffect {

    public ReturnTargetPermanentToHandAtEndOfCombatEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
    }

    public ReturnTargetPermanentToHandAtEndOfCombatEffect() {
        this(Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }
}
