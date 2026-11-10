package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;

import java.util.UUID;

/**
 * A delayed "at end of combat" triggered ability (CR 603.7) waiting to be put onto the stack as the
 * next end of combat step begins (CR 511.2); one created during that step waits for a later combat.
 * The {@code affectedPermanentId} becomes the trigger's non-targeting target and the optional
 * {@code triggeringPermanentId} its triggering permanent. {@code affectedPermanentAttachments} are the Auras and
 * Equipment attached to the affected permanent just before it left the battlefield, recorded by its departure
 * ({@link DepartedAttachments#NONE} while it is still there), which the stack entry keeps for "destroy all Equipment
 * attached to that creature" (Corrosive Ooze).
 */
public record DelayedEndOfCombatTrigger(
        UUID controllerId,
        Card sourceCard,
        UUID sourcePermanentId,
        UUID affectedPermanentId,
        CardEffect effect,
        UUID triggeringPermanentId,
        DepartedAttachments affectedPermanentAttachments
) implements DelayedAction {

    public DelayedEndOfCombatTrigger {
        affectedPermanentAttachments = affectedPermanentAttachments == null
                ? DepartedAttachments.NONE : affectedPermanentAttachments;
    }

    public DelayedEndOfCombatTrigger(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                     UUID affectedPermanentId, CardEffect effect, UUID triggeringPermanentId) {
        this(controllerId, sourceCard, sourcePermanentId, affectedPermanentId, effect, triggeringPermanentId,
                DepartedAttachments.NONE);
    }

    public DelayedEndOfCombatTrigger(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                     CardEffect effect) {
        this(controllerId, sourceCard, sourcePermanentId, null, effect, null);
    }

    public DelayedEndOfCombatTrigger(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                     UUID affectedPermanentId, CardEffect effect) {
        this(controllerId, sourceCard, sourcePermanentId, affectedPermanentId, effect, null);
    }

    /** This trigger with {@code attachments} as what was attached to its affected permanent as it left. */
    public DelayedEndOfCombatTrigger withAffectedPermanentAttachments(DepartedAttachments attachments) {
        return new DelayedEndOfCombatTrigger(controllerId, sourceCard, sourcePermanentId, affectedPermanentId, effect,
                triggeringPermanentId, attachments);
    }
}
