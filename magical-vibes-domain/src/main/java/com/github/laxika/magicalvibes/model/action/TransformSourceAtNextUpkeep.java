package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger: transform {@code permanentId} to its back face at the beginning of the next
 * upkeep (e.g. Archangel Avacyn). Drained in {@code StepTriggerService.handleUpkeepTriggers}
 * regardless of active player. Pushed onto the stack as a triggered ability.
 */
public record TransformSourceAtNextUpkeep(
        UUID permanentId, UUID controllerId, Card sourceCard) implements DelayedAction {
}
