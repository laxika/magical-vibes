package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.UUID;

/**
 * A global triggered ability registered for the rest of the turn by a resolving effect.
 * The source card is retained because the spell that registered the ability is no longer on the
 * battlefield when the trigger fires.
 */
public record TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, EffectSlot slot,
                                              CardEffect effect, TargetFilter targetFilter,
                                              boolean untilEndOfNextTurn, int registrationTurnNumber) {

    public TemporaryGlobalTriggeredAbility(UUID controllerId, Card sourceCard, EffectSlot slot,
                                           CardEffect effect) {
        this(controllerId, sourceCard, slot, effect, null, false, -1);
    }
}
