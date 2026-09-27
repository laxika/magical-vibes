package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;

/** Returns the permanent that caused the trigger when it had the specified counter. */
public record ReturnTriggeringPermanentToBattlefieldWithCounterEffect(CounterType counterType,
                                                                       Zone fromZone)
        implements CardEffect {

    public ReturnTriggeringPermanentToBattlefieldWithCounterEffect {
        if (counterType == null) {
            throw new IllegalArgumentException("counterType must not be null");
        }
        if (fromZone != Zone.GRAVEYARD && fromZone != Zone.EXILE) {
            throw new IllegalArgumentException("fromZone must be GRAVEYARD or EXILE");
        }
    }
}
