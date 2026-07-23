package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger: "When this creature leaves the battlefield this turn, sacrifice that creature."
 * Registered by Phantasmal Mount's activated ability. Cleared at turn cleanup.
 *
 * @param watchedPermanentId the ability's source whose leave fires the sacrifice
 * @param targetPermanentId  the pumped creature to sacrifice
 * @param controllerId       controller of the delayed trigger
 * @param sourceCard         card used for the triggered-ability stack entry
 */
public record DelayedSacrificeTargetWhenSourceLeaves(
        UUID watchedPermanentId,
        UUID targetPermanentId,
        UUID controllerId,
        Card sourceCard
) implements DelayedAction {
}
