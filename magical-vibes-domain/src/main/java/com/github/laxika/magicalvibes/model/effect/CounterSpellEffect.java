package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Counter target spell (or ability). The {@code destination} controls where the countered card is
 * placed instead of the graveyard — see {@link CounteredSpellDestination}. The no-arg constructor
 * defaults to {@link CounteredSpellDestination#GRAVEYARD} for the many plain "counter target spell"
 * cards.
 *
 * <p>{@code spell} names what is countered: {@link Subjects#targetSpell()} (the default, a benign spell target), or
 * {@link Subjects#triggeringSpell()} for a cast trigger's "counter that spell" / "counter [this spell]" without a
 * target ({@link #triggeringSpell()}: "any player may discard three cards. If a player does, counter Phantasmagorian").
 * A triggering spell that has left the stack is not countered (CR 701.6a, 608.2b).
 */
public record CounterSpellEffect(CounteredSpellDestination destination, PermanentSubject spell)
        implements CounterSpellingEffect {

    public CounterSpellEffect {
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(spell, "spell");
        if (!(spell instanceof PermanentSubject.TargetSpell) && !(spell instanceof PermanentSubject.TriggeringSpell)) {
            throw new IllegalArgumentException("Not a spell subject: " + spell);
        }
    }

    public CounterSpellEffect() {
        this(CounteredSpellDestination.GRAVEYARD);
    }

    public CounterSpellEffect(CounteredSpellDestination destination) {
        this(destination, Subjects.targetSpell());
    }

    /** "Counter that spell" / "counter [this spell]" of a cast trigger: the triggering spell, not targeted. */
    public static CounterSpellEffect triggeringSpell() {
        return new CounterSpellEffect(CounteredSpellDestination.GRAVEYARD, Subjects.triggeringSpell());
    }

    @Override
    public TargetSpec targetSpec() {
        return spell instanceof PermanentSubject.TriggeringSpell
                ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
