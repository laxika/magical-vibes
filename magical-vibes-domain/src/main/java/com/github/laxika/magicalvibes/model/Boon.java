package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.UUID;

/**
 * A finite-use triggered ability granted directly to a player.
 *
 * @param filter what the object of the boon's event must be, or {@code null} for any: "whenever one or more Merfolk
 *               enter under your control" is a {@link BoonTrigger#PERMANENT_ENTERS} boon with a Merfolk filter on the
 *               entering permanent's card
 */
public record Boon(UUID controllerId, Card sourceCard, CardEffect effect, int remainingUses,
                   BoonTrigger trigger, TargetFilter targetFilter, CardPredicate filter) {

    public Boon(UUID controllerId, Card sourceCard, CardEffect effect, int remainingUses) {
        this(controllerId, sourceCard, effect, remainingUses, BoonTrigger.CREATURE_ENTERS, null, null);
    }

    /** A boon for any object of its event. */
    public Boon(UUID controllerId, Card sourceCard, CardEffect effect, int remainingUses,
                BoonTrigger trigger, TargetFilter targetFilter) {
        this(controllerId, sourceCard, effect, remainingUses, trigger, targetFilter, null);
    }

    /** This boon with {@code remainingUses} uses left. */
    public Boon withRemainingUses(int remainingUses) {
        return new Boon(controllerId, sourceCard, effect, remainingUses, trigger, targetFilter, filter);
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
