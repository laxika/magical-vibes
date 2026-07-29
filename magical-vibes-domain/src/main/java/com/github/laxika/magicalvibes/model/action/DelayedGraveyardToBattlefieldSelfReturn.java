package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Delayed trigger: return a specific card from its owner's graveyard to the battlefield under that
 * owner's control at the beginning of the next end step, entering with {@code counterAmount} counters
 * of {@code counterType} (no counters when {@code counterType} is null). Used by Sand Golem.
 */
public record DelayedGraveyardToBattlefieldSelfReturn(
        UUID cardId,
        UUID ownerId,
        CounterType counterType,
        int counterAmount
) implements DelayedAction {
}
