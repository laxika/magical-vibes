package com.github.laxika.magicalvibes.model.effect;

/** Restricts untapping of the source or the permanent it enchants. */
public record CantBecomeUntappedEffect(PermanentSubject subject) implements CardEffect {

    public CantBecomeUntappedEffect() {
        this(Subjects.source());
    }
}
