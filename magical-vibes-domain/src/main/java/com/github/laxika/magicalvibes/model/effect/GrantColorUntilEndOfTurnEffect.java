package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Target permanent becomes {@link #color} until end of turn.
 *
 * <p>{@link #additive} is {@code false} for the usual "becomes [color]" wording, which replaces all
 * previous colors (Distorting Lens, Grand Architect). It is {@code true} for "becomes [color] in
 * addition to its other colors" (Indigo Faerie), which adds the color instead of replacing.
 *
 * <p>{@link Subjects#triggering()} is "it becomes [color]" of the permanent whose event triggered the
 * ability, read from the stack entry (Shadow Puppeteers).
 *
 * <p>Group scopes such as {@link Subjects#otherCreatures()} apply the change to the matching
 * permanents that exist when the effect resolves.
 *
 * <p>{@link #canTargetSpell} is {@code true} for "target permanent or spell becomes [color]"
 * (Eight-and-a-Half-Tails), where a target that is not on the battlefield is looked up on the stack.
 */
public record GrantColorUntilEndOfTurnEffect(CardColor color, boolean additive, PermanentSubject subject,
                                             boolean canTargetSpell) implements CardEffect {

    public GrantColorUntilEndOfTurnEffect(CardColor color) {
        this(color, false, Subjects.target(), false);
    }

    public GrantColorUntilEndOfTurnEffect(CardColor color, boolean additive) {
        this(color, additive, Subjects.target(), false);
    }

    public GrantColorUntilEndOfTurnEffect(CardColor color, boolean additive, PermanentSubject subject) {
        this(color, additive, subject, false);
    }

    public GrantColorUntilEndOfTurnEffect(CardColor color, PermanentSubject subject) {
        this(color, false, subject, false);
    }

    public GrantColorUntilEndOfTurnEffect(CardColor color, boolean additive, boolean canTargetSpell) {
        this(color, additive, Subjects.target(), canTargetSpell);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresPlayerTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        if (subject.isSet() || subject instanceof PermanentSubject.Triggering) {
            return TargetSpec.NONE;
        }
        return subject instanceof PermanentSubject.Source
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.benign(TargetPredicates.permanent());
    }
}
