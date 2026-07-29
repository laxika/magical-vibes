package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger: at the beginning of {@code playerId}'s next upkeep they get {@code amount} poison
 * counters unless they pay {@code manaCost} (a "you may pay; if you don't, get poisoned" prompt).
 * Scheduled by Sabertooth Cobra's {@code ON_DAMAGE_TO_PLAYER} trigger and drained in
 * {@code StepTriggerService} when that player's upkeep begins. Persists across intervening turns
 * until the affected player's own upkeep is reached.
 */
public record PoisonAtNextUpkeepUnlessPays(UUID playerId, int amount, String manaCost, Card sourceCard)
        implements DelayedAction {
}
