package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                         Integer toughnessOverride) implements CardEffect {

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel) {
        this(filter, typeLabel, null, null);
    }
}
