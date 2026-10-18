package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger (Quenchable Fire): at the beginning of {@code spellControllerId}'s next upkeep step,
 * deal {@code damage} to {@code targetId} — a player or planeswalker. Drained in
 * {@code StepTriggerService} only when {@code spellControllerId} is the active player; the drain
 * pushes the damage trigger onto the stack under the spell controller. The {@code manaCost} payment
 * window (a special action before upkeep begins) is not modelled, so the damage is unconditional.
 * Persists across intervening turns until the spell controller's own upkeep is reached.
 */
public record DamageAtNextUpkeepUnlessPays(UUID spellControllerId, UUID targetId, int damage,
        String manaCost, Card sourceCard) implements DelayedAction {
}
