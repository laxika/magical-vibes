package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.UUID;

/** A finite-use triggered ability granted directly to a player. */
public record Boon(UUID controllerId, Card sourceCard, CardEffect effect, int remainingUses,
                   BoonTrigger trigger, TargetFilter targetFilter) {

    public Boon(UUID controllerId, Card sourceCard, CardEffect effect, int remainingUses) {
        this(controllerId, sourceCard, effect, remainingUses, BoonTrigger.CREATURE_ENTERS, null);
    }

    public Boon {
        if (remainingUses <= 0) {
            throw new IllegalArgumentException("A boon must have at least one remaining use");
        }
        if (trigger == null) {
            throw new IllegalArgumentException("A boon trigger is required");
        }
    }
}
