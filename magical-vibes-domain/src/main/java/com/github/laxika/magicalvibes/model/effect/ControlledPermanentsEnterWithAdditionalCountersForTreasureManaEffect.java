package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static entry replacement that gives matching permanents additional +1/+1 counters equal to the
 * amount of Treasure-produced mana spent to cast the entering permanent.
 */
public record ControlledPermanentsEnterWithAdditionalCountersForTreasureManaEffect(
        PermanentPredicate enteringPermanentPredicate
) implements ControlledPermanentEntryReplacementEffect {

    @Override
    public int additionalCounterCount(GameData gameData, Permanent enteringPermanent) {
        return gameData.getSpellCastTreasureManaSpent(enteringPermanent.getCard().getId());
    }
}
