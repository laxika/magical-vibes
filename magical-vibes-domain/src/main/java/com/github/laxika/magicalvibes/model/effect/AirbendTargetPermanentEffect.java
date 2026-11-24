package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Objects;

/**
 * Airbends the permanents {@code subject} names, and the target spell if the declared target is one (CR 701.65a): they
 * are exiled together, and for each card exiled this way its owner may cast it by paying {@code {2}} rather than its
 * mana cost while it remains exiled. A token or a copy of a spell ceases to exist, and a land card can't be cast. One
 * "whenever you airbend" event follows if anything was exiled (CR 701.65b).
 *
 * @param subject        the permanents airbent: {@code target()} for "airbend target nonland permanent" (every target
 *                       of a bound multi-target group), {@code triggering()} for "airbend that creature" (Monk
 *                       Gyatso), {@code except(allCreatures(), targetDeclaredElsewhere())} for "choose up to one target
 *                       creature, then airbend all other creatures" (Avatar's Wrath)
 * @param declaredTarget the target the effect declares: the airbent target ("target nonland permanent", "target
 *                       creature or spell", whose spell is airbent too), or the target a set subject leaves out;
 *                       {@link TargetSpec#NONE} for a subject that targets nothing
 */
public record AirbendTargetPermanentEffect(PermanentSubject subject, TargetSpec declaredTarget)
        implements RemovalEffect {

    public AirbendTargetPermanentEffect {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(declaredTarget, "declaredTarget");
        if (subject.declaresTarget() && declaredTarget.declaredTarget() == null) {
            throw new IllegalArgumentException("A target subject needs the target it declares");
        }
    }

    /** "Airbend target nonland permanent." */
    public AirbendTargetPermanentEffect() {
        this(Subjects.target(), TargetSpec.harmful(TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }

    /** "Airbend target creature or spell": the declared target, a spell on the stack airbent too (Aang, Swift Savior). */
    public static AirbendTargetPermanentEffect targeting(TargetPredicate target) {
        return new AirbendTargetPermanentEffect(Subjects.target(), TargetSpec.harmful(target));
    }

    /** "Airbend that creature": a subject that targets nothing (Monk Gyatso's {@code triggering()}). */
    public static AirbendTargetPermanentEffect of(PermanentSubject subject) {
        return new AirbendTargetPermanentEffect(subject, TargetSpec.NONE);
    }

    @Override
    public TargetSpec targetSpec() {
        return declaredTarget;
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
