package com.github.laxika.magicalvibes.model.action;

import java.util.List;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/**
 * Delayed trigger: "Until end of turn, whenever you cast a [filter] spell, [effects]." Registered by
 * Mountain Titan's activated ability. Fires once per matching spell (or once total when
 * {@code oneShot} is true) the registering controller casts for the rest of the turn; the stack
 * entry carries {@code sourcePermanentId} so self-referential
 * effects ({@code PutCountersOnSourceEffect}) find the permanent that granted the trigger. Cleared
 * at turn cleanup.
 *
 * @param controllerId      player whose spells the trigger watches (and who controls the trigger)
 * @param sourcePermanentId             permanent that registered the trigger
 * @param sourceCard                    card shown in the log / on the stack
 * @param spellFilter                   which cast spells trigger it; {@code null} = any spell
 * @param stackEntryFilter              optional filter evaluated against the cast stack entry
 * @param resolvedEffects               effects put on the stack when it fires
 * @param sourceMustRemainOnBattlefield whether the source permanent must still be on the battlefield
 *                                      for the delayed trigger to fire
 * @param targetFilter                  optional permanent/player target filter used when the delayed
 *                                      trigger goes on the stack
 * @param sourcePermanentSnapshot        last-known source snapshot for source-relative filters
 * @param sourcePowerAtLastKnown         last-known effective source power for source-relative filters
 */
public record DelayedControllerSpellCastTrigger(UUID controllerId,
                                                UUID sourcePermanentId,
                                                Card sourceCard,
                                                CardPredicate spellFilter,
                                                StackEntryPredicate stackEntryFilter,
                                                List<CardEffect> resolvedEffects,
                                                boolean oneShot,
                                                boolean sourceMustRemainOnBattlefield,
                                                TargetFilter targetFilter,
                                                Permanent sourcePermanentSnapshot,
                                                Integer sourcePowerAtLastKnown)
        implements DelayedAction {

    public DelayedControllerSpellCastTrigger(UUID controllerId, UUID sourcePermanentId,
                                             Card sourceCard, CardPredicate spellFilter,
                                             List<CardEffect> resolvedEffects) {
        this(controllerId, sourcePermanentId, sourceCard, spellFilter, null, resolvedEffects,
                false, true, null, null, null);
    }

    public DelayedControllerSpellCastTrigger(UUID controllerId, UUID sourcePermanentId,
                                             Card sourceCard, CardPredicate spellFilter,
                                             List<CardEffect> resolvedEffects,
                                             boolean sourceMustRemainOnBattlefield) {
        this(controllerId, sourcePermanentId, sourceCard, spellFilter, null, resolvedEffects,
                false, sourceMustRemainOnBattlefield, null, null, null);
    }

    public DelayedControllerSpellCastTrigger(UUID controllerId, UUID sourcePermanentId,
                                             Card sourceCard, CardPredicate spellFilter,
                                             List<CardEffect> resolvedEffects, boolean oneShot,
                                             boolean sourceMustRemainOnBattlefield) {
        this(controllerId, sourcePermanentId, sourceCard, spellFilter, null, resolvedEffects,
                oneShot, sourceMustRemainOnBattlefield, null, null, null);
    }

    public DelayedControllerSpellCastTrigger withSourcePermanentSnapshot(Permanent snapshot) {
        return withSourcePermanentSnapshot(snapshot, null);
    }

    public DelayedControllerSpellCastTrigger withSourcePermanentSnapshot(
            Permanent snapshot, Integer powerAtLastKnown) {
        return new DelayedControllerSpellCastTrigger(
                controllerId, sourcePermanentId, sourceCard, spellFilter, stackEntryFilter,
                resolvedEffects, oneShot, sourceMustRemainOnBattlefield, targetFilter, snapshot,
                powerAtLastKnown);
    }
}
