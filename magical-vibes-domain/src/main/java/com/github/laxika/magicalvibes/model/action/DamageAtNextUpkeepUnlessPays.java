package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger (Quenchable Fire): at the beginning of {@code spellControllerId}'s next upkeep step,
 * deal {@code damage} to {@code targetId} — a player or planeswalker. Drained in
 * {@code StepTriggerService} only when {@code spellControllerId} is the active player; the drain
 * pushes the damage trigger onto the stack under the spell controller. Until then the damaged player
 * (or the damaged planeswalker's current controller) may pay {@code manaCost} as a special action
 * (CR 116.2c), which removes this action so the ability never triggers. Persists across intervening turns until the spell controller's own upkeep is reached.
 */
public record DamageAtNextUpkeepUnlessPays(UUID spellControllerId, UUID targetId, int damage,
        String manaCost, Card sourceCard) implements DelayedAction {
}
