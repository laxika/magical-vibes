package com.github.laxika.magicalvibes.model.action;

import java.util.List;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/**
 * Delayed trigger: "Until end of turn, whenever you cast a [filter] spell, [effects]." Registered by
 * Mountain Titan's activated ability. Fires once per matching spell the registering controller casts
 * for the rest of the turn; the stack entry carries {@code sourcePermanentId} so self-referential
 * effects ({@code PutCountersOnSourceEffect}) find the permanent that granted the trigger. Cleared
 * at turn cleanup.
 *
 * @param controllerId      player whose spells the trigger watches (and who controls the trigger)
 * @param sourcePermanentId             permanent that registered the trigger
 * @param sourceCard                    card shown in the log / on the stack
 * @param spellFilter                   which cast spells trigger it; {@code null} = any spell
 * @param resolvedEffects               effects put on the stack when it fires
 * @param sourceMustRemainOnBattlefield whether the source permanent must still be on the battlefield
 *                                      for the delayed trigger to fire
 * @param targetFilter                  optional permanent/player target filter used when the delayed
 *                                      trigger goes on the stack
 */
public record DelayedControllerSpellCastTrigger(UUID controllerId,
                                                UUID sourcePermanentId,
                                                Card sourceCard,
                                                CardPredicate spellFilter,
                                                List<CardEffect> resolvedEffects,
                                                boolean oneShot,
                                                boolean sourceMustRemainOnBattlefield,
                                                TargetFilter targetFilter)
        implements DelayedAction {

    public DelayedControllerSpellCastTrigger(UUID controllerId, UUID sourcePermanentId,
                                             Card sourceCard, CardPredicate spellFilter,
                                             List<CardEffect> resolvedEffects) {
        this(controllerId, sourcePermanentId, sourceCard, spellFilter, resolvedEffects,
                false, true, null);
    }

    public DelayedControllerSpellCastTrigger(UUID controllerId, UUID sourcePermanentId,
                                             Card sourceCard, CardPredicate spellFilter,
                                             List<CardEffect> resolvedEffects,
                                             boolean sourceMustRemainOnBattlefield) {
        this(controllerId, sourcePermanentId, sourceCard, spellFilter, resolvedEffects,
                false, sourceMustRemainOnBattlefield, null);
    }

    public DelayedControllerSpellCastTrigger(UUID controllerId, UUID sourcePermanentId,
                                             Card sourceCard, CardPredicate spellFilter,
                                             List<CardEffect> resolvedEffects, boolean oneShot,
                                             boolean sourceMustRemainOnBattlefield) {
        this(controllerId, sourcePermanentId, sourceCard, spellFilter, resolvedEffects,
                oneShot, sourceMustRemainOnBattlefield, null);
    }
}
