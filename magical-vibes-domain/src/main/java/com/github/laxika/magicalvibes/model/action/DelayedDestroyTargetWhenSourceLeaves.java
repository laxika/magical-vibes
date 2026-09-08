package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger: "When this artifact leaves the battlefield this turn, destroy that creature."
 * Registered by War Barge's activated ability and cleared at turn cleanup.
 *
 * @param watchedPermanentId the artifact whose leave fires the destruction
 * @param targetPermanentId the creature to destroy
 * @param controllerId controller of the delayed trigger
 * @param sourceCard card used for the triggered-ability stack entry
 */
public record DelayedDestroyTargetWhenSourceLeaves(
        UUID watchedPermanentId,
        UUID targetPermanentId,
        UUID controllerId,
        Card sourceCard
) implements DelayedAction {
}
