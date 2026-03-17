package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record RegenerateAllOwnCreaturesEffect(PermanentPredicate filter) implements CardEffect {

    public RegenerateAllOwnCreaturesEffect() {
        this(null);
    }
}
