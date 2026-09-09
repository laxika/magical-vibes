package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record ExileAllPermanentsEffect(PermanentPredicate filter, boolean trackWithSource,
                                       boolean returnOneAtEachUpkeep) implements CardEffect {

    public ExileAllPermanentsEffect(PermanentPredicate filter) {
        this(filter, false, false);
    }

    public ExileAllPermanentsEffect(PermanentPredicate filter, boolean trackWithSource) {
        this(filter, trackWithSource, false);
    }
}
