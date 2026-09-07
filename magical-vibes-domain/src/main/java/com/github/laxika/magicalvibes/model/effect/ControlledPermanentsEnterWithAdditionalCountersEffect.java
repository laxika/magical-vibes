package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record ControlledPermanentsEnterWithAdditionalCountersEffect(
        PermanentPredicate enteringPermanentPredicate,
        int count
) implements ControlledPermanentEntryReplacementEffect {

    @Override
    public int additionalCounterCount(GameData gameData, Permanent enteringPermanent) {
        return count;
    }
}
