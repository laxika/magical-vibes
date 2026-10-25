package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger: at the beginning of {@code playerId}'s next upkeep they get {@code amount} poison
 * counters. Scheduled by Sabertooth Cobra's {@code ON_DAMAGE_TO_PLAYER} trigger and drained in
 * {@code StepTriggerService} when that player's upkeep begins; the resulting ability is controlled by
 * {@code controllerId}, the controller of the scheduling ability. Until then the affected player may
 * pay {@code manaCost} as a special action (CR 116.2c), which removes this action. Persists across intervening turns
 * until the affected player's own upkeep is reached.
 */
public record PoisonAtNextUpkeepUnlessPays(UUID playerId, UUID controllerId, int amount, String manaCost,
                                           Card sourceCard)
        implements DelayedAction {
}
