package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger watching one chosen creature for an unblocked attack until end of turn. The cube
 * counter goes on the specific artifact permanent that created the trigger ({@code sourcePermanentId}),
 * so a later object with the same card does not receive it.
 */
public record DelayedUnblockedAttackerCubeCounter(
        UUID watchedPermanentId,
        UUID controllerId,
        Card sourceCard,
        UUID sourcePermanentId
) implements DelayedAction {
}
