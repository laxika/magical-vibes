package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record UntapEachOtherCreatureYouControlEffect(PermanentPredicate filter) implements CardEffect {

    public UntapEachOtherCreatureYouControlEffect() {
        this(null);
    }
}
