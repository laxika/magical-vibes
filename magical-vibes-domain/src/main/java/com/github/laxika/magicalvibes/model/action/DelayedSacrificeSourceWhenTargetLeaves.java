package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger: "When that creature leaves the battlefield this turn, sacrifice this creature."
 * Registered by Kjeldoran Elite Guard's activated ability. Cleared at turn cleanup.
 *
 * @param watchedPermanentId the pumped creature whose leave fires the sacrifice
 * @param sourcePermanentId  the permanent to sacrifice (the ability's source)
 * @param controllerId       controller of the delayed trigger
 * @param sourceCard         card used for the triggered-ability stack entry
 */
public record DelayedSacrificeSourceWhenTargetLeaves(
        UUID watchedPermanentId,
        UUID sourcePermanentId,
        UUID controllerId,
        Card sourceCard
) implements DelayedAction {
}
