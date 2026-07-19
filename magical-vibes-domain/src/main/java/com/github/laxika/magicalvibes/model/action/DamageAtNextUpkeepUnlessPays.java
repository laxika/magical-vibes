package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger (Quenchable Fire): at the beginning of {@code spellControllerId}'s next upkeep step,
 * deal {@code damage} to {@code targetId} — a player or planeswalker — unless that player (or that
 * planeswalker's controller) pays {@code manaCost} before that step. Drained in
 * {@code StepTriggerService} only when {@code spellControllerId} is the active player; the drain
 * resolves the paying party from {@code targetId} and pushes a pay-or-damage trigger onto the stack.
 * Persists across intervening turns until the spell controller's own upkeep is reached.
 */
public record DamageAtNextUpkeepUnlessPays(UUID spellControllerId, UUID targetId, int damage,
        String manaCost, Card sourceCard) implements DelayedAction {
}
