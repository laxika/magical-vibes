package com.github.laxika.magicalvibes.model.effect;

/** Restricts counter placement on the source or the permanent it enchants. */
public record CantHaveCountersEffect(GrantScope scope) implements CardEffect {

    public CantHaveCountersEffect() {
        this(GrantScope.SELF);
    }
}
