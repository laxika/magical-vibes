package com.github.laxika.magicalvibes.model.effect;

/** Restricts counter placement on the source or the permanent it enchants. */
public record CantHaveCountersEffect(PermanentSubject subject) implements CardEffect {

    public CantHaveCountersEffect() {
        this(Subjects.source());
    }
}
