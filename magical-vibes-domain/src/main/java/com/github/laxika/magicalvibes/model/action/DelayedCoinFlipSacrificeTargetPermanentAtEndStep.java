package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger that flips a coin at the next end step and sacrifices the tracked permanent
 * only if the controller loses the flip.
 */
public record DelayedCoinFlipSacrificeTargetPermanentAtEndStep(
        UUID permanentId,
        UUID controllerId,
        Card sourceCard
) implements DelayedAction {
}
