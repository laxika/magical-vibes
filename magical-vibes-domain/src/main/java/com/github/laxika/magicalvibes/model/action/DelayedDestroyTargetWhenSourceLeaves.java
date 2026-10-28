package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger: "When this artifact leaves the battlefield this turn, destroy that creature" (War Barge),
 * or "When this creature leaves the battlefield or becomes untapped, destroy that creature" (Merieke Ri
 * Berit). With {@code exile} the trigger exiles the creature instead, and with
 * {@code alsoWhenControlLost} the controller losing control of the watched permanent also fires it
 * (Coffin Queen). It triggers only once (CR 603.7b); a this-turn registration is cleared at turn cleanup.
 *
 * @param watchedPermanentId the permanent whose leaving (or untapping) fires the destruction
 * @param targetPermanentId the creature to destroy
 * @param controllerId controller of the delayed trigger
 * @param sourceCard card used for the triggered-ability stack entry
 * @param alsoWhenUntapped whether the watched permanent becoming untapped also fires it
 * @param thisTurnOnly whether the registration ends at turn cleanup
 * @param exile whether the creature is exiled rather than destroyed
 * @param alsoWhenControlLost whether the delayed trigger's controller losing control of the watched
 *                            permanent also fires it
 */
public record DelayedDestroyTargetWhenSourceLeaves(
        UUID watchedPermanentId,
        UUID targetPermanentId,
        UUID controllerId,
        Card sourceCard,
        boolean alsoWhenUntapped,
        boolean thisTurnOnly,
        boolean exile,
        boolean alsoWhenControlLost
) implements DelayedAction {

    public DelayedDestroyTargetWhenSourceLeaves(UUID watchedPermanentId, UUID targetPermanentId, UUID controllerId,
                                                Card sourceCard, boolean alsoWhenUntapped, boolean thisTurnOnly) {
        this(watchedPermanentId, targetPermanentId, controllerId, sourceCard, alsoWhenUntapped, thisTurnOnly,
                false, false);
    }
}
