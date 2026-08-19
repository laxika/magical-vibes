package com.github.laxika.magicalvibes.model.action;

import java.util.List;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Delayed trigger: "Until end of turn, whenever you cast a [filter] spell, [effects]." Registered by
 * Mountain Titan's activated ability. Fires once per matching spell the registering controller casts
 * for the rest of the turn; the stack entry carries {@code sourcePermanentId} so self-referential
 * effects ({@code PutCountersOnSourceEffect}) find the permanent that granted the trigger. Cleared
 * at turn cleanup.
 *
 * @param controllerId      player whose spells the trigger watches (and who controls the trigger)
 * @param sourcePermanentId permanent that registered the trigger; the trigger does nothing once it
 *                          has left the battlefield
 * @param sourceCard        card shown in the log / on the stack
 * @param spellFilter       which cast spells trigger it; {@code null} = any spell
 * @param resolvedEffects   effects put on the stack when it fires
 * @param sourceMustRemainOnBattlefield whether the source must still be on the battlefield
 */
public record DelayedControllerSpellCastTrigger(UUID controllerId,
                                                UUID sourcePermanentId,
                                                Card sourceCard,
                                                CardPredicate spellFilter,
                                                List<CardEffect> resolvedEffects,
                                                boolean sourceMustRemainOnBattlefield)
        implements DelayedAction {
}
