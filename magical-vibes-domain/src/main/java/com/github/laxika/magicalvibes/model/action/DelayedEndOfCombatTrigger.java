package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** A triggered ability waiting to be put onto the stack at the beginning of the end-of-combat step. */
public record DelayedEndOfCombatTrigger(
        UUID controllerId,
        Card sourceCard,
        UUID sourcePermanentId,
        UUID affectedPermanentId,
        CardEffect effect
) implements DelayedAction {
    public DelayedEndOfCombatTrigger(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                     CardEffect effect) {
        this(controllerId, sourceCard, sourcePermanentId, null, effect);
    }
}
