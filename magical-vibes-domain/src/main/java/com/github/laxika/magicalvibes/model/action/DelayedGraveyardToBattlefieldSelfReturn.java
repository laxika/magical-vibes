package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Delayed trigger: return a specific card from its owner's graveyard to the battlefield under that
 * owner's control, entering with {@code counterAmount} counters of {@code counterType} (no counters
 * when {@code counterType} is null). Used by Sand Golem.
 *
 * <p>{@code atNextUpkeep} moves the timing from the next end step to the beginning of the owner's
 * next upkeep (Phytotitan), and {@code tapped} makes it enter tapped.
 */
public record DelayedGraveyardToBattlefieldSelfReturn(
        UUID cardId,
        UUID ownerId,
        CounterType counterType,
        int counterAmount,
        boolean atNextUpkeep,
        boolean tapped
) implements DelayedAction {
}
