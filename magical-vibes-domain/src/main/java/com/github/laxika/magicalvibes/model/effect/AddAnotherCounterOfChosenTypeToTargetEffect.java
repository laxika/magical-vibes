package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a counter on target permanent or player. Give that permanent or player another counter of that kind"
 * (Animation Module, Ichormoon Gauntlet), and over a set that doesn't target, "for each [permanent], choose a counter on
 * it. You may put an additional counter of that kind on that permanent" (The Caves of Androzani: each permanent's
 * choice may be declined). The set is found once as the effect resolves (CR 608.2h); each choice is made in turn.
 *
 * @param subject {@link Subjects#target()} (a permanent or player), or a set of permanents
 */
public record AddAnotherCounterOfChosenTypeToTargetEffect(PermanentSubject subject) implements CardEffect {

    public AddAnotherCounterOfChosenTypeToTargetEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
    }

    public AddAnotherCounterOfChosenTypeToTargetEffect() {
        this(Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.playerOrPermanent())
                : TargetSpec.NONE;
    }
}
