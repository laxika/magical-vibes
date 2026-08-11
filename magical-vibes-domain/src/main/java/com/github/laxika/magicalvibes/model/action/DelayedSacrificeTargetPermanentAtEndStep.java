package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger for sacrificing a targeted permanent at the next end step, with the trigger
 * controller and source card retained for the conditional life gain rider.
 */
public record DelayedSacrificeTargetPermanentAtEndStep(
        UUID permanentId,
        UUID controllerId,
        Card sourceCard
) implements DelayedAction {
}
