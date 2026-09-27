package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record ExileAllPermanentsEffect(PermanentPredicate filter, boolean trackWithSource,
                                       boolean returnOneAtEachUpkeep, boolean ownerMayPlayWhileExiled,
                                       int perpetualCastCostIncrease, boolean perpetualEnterTapped)
        implements CardEffect {

    public ExileAllPermanentsEffect(PermanentPredicate filter) {
        this(filter, false, false, false, 0, false);
    }

    public ExileAllPermanentsEffect(PermanentPredicate filter, boolean trackWithSource) {
        this(filter, trackWithSource, false, false, 0, false);
    }

    public ExileAllPermanentsEffect(PermanentPredicate filter, boolean trackWithSource,
                                    boolean returnOneAtEachUpkeep) {
        this(filter, trackWithSource, returnOneAtEachUpkeep, false, 0, false);
    }
}
