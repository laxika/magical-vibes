package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Makes every currently suspected creature matching the optional filter no longer suspected. */
public record UnsuspectAllCreaturesEffect(PermanentPredicate filter) implements CardEffect {

    public UnsuspectAllCreaturesEffect() {
        this(null);
    }
}
