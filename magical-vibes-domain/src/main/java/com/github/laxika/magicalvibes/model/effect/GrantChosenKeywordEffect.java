package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * On resolution, prompts the controller to choose one keyword from {@code options}, then grants
 * that keyword until end of turn to the permanent selected by {@code subject}.
 * <p>
 * {@link Subjects#source()} is the non-targeted "this creature gains your choice of …" shape
 * (Urza's Avenger, Illusionary Presence) and resolves against the ability's source permanent.
 * {@link Subjects#target()} is "target creature gains your choice of …" (Golem Artisan,
 * Practiced Offense). {@code filter} optionally narrows the targeted creature, such as to a
 * creature controlled by the ability's controller. No other subject is supported.
 */
public record GrantChosenKeywordEffect(List<Keyword> options, PermanentSubject subject,
                                       PermanentPredicate filter, Duration duration) implements CardEffect {

    public GrantChosenKeywordEffect(List<Keyword> options, PermanentSubject subject) {
        this(options, subject, null, Duration.UNTIL_END_OF_TURN);
    }

    public GrantChosenKeywordEffect(List<Keyword> options, PermanentSubject subject, Duration duration) {
        this(options, subject, null, duration);
    }

    public GrantChosenKeywordEffect(List<Keyword> options, PermanentSubject subject, PermanentPredicate filter) {
        this(options, subject, filter, Duration.UNTIL_END_OF_TURN);
    }

    public GrantChosenKeywordEffect {
        if (!(subject instanceof PermanentSubject.Source) && !(subject instanceof PermanentSubject.Target)) {
            throw new IllegalArgumentException("GrantChosenKeywordEffect supports only source() and target(), got "
                    + subject);
        }
        if (duration == null) {
            throw new IllegalArgumentException("GrantChosenKeywordEffect duration must not be null");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.creature(), filter)
                : TargetSpec.NONE;
    }
}
