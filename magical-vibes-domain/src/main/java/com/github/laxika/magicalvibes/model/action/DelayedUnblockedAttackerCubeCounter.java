package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger watching one chosen creature for an unblocked attack until end of turn.
 */
public record DelayedUnblockedAttackerCubeCounter(
        UUID watchedPermanentId,
        UUID controllerId,
        Card sourceCard
) implements DelayedAction {
}
