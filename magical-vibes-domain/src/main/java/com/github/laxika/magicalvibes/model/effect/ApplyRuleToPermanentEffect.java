package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "[The permanent {@code subject} names] [rule] [this turn]": a resolution's rule about one permanent that gives it no
 * ability ("it can't be sacrificed this turn", Slicer, Hired Muscle). It is floated on the permanent as a
 * {@link GrantNonAbilityEffectEffect} for {@code duration} (CR 611.2a), so a later "loses all abilities" leaves it.
 * A quoted ability the permanent gains ("it gains 'This creature can't be sacrificed'") is a grant instead
 * ({@link GrantEffectToTargetUntilEndOfTurnEffect}), and a rule over a set covers permanents arriving later
 * (CR 611.2c), so a set subject is refused.
 *
 * @param rule     the restriction, read wherever the layered state's granted effects are read
 * @param subject  the one permanent: the target, {@link Subjects#source()} or {@link Subjects#triggering()}
 * @param duration how long the rule lasts
 */
public record ApplyRuleToPermanentEffect(CardEffect rule, PermanentSubject subject, Duration duration)
        implements CardEffect {

    public ApplyRuleToPermanentEffect {
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(duration, "duration");
        if (subject.isSet() || subject.declaresPlayerTarget()) {
            throw new IllegalArgumentException("A rule over a set isn't locked to its members (CR 611.2c): " + subject);
        }
    }

    /** "[The permanent {@code subject} names] [rule] this turn." */
    public static ApplyRuleToPermanentEffect untilEndOfTurn(CardEffect rule, PermanentSubject subject) {
        return new ApplyRuleToPermanentEffect(rule, subject, Duration.UNTIL_END_OF_TURN);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        return subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Triggering
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.NONE;
    }
}
