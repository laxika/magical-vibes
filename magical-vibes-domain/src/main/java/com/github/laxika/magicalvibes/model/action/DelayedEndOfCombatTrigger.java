package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * A delayed "at end of combat" triggered ability (CR 603.7) waiting to be put onto the stack as the
 * next end of combat step begins (CR 511.2); one created during that step waits for a later combat.
 * The {@code affectedPermanentId} becomes the trigger's non-targeting target and the optional
 * {@code triggeringPermanentId} its triggering permanent.
 */
public record DelayedEndOfCombatTrigger(
        UUID controllerId,
        Card sourceCard,
        UUID sourcePermanentId,
        UUID affectedPermanentId,
        CardEffect effect,
        UUID triggeringPermanentId
) implements DelayedAction {
    public DelayedEndOfCombatTrigger(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                     CardEffect effect) {
        this(controllerId, sourceCard, sourcePermanentId, null, effect, null);
    }

    public DelayedEndOfCombatTrigger(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                     UUID affectedPermanentId, CardEffect effect) {
        this(controllerId, sourceCard, sourcePermanentId, affectedPermanentId, effect, null);
    }
}
