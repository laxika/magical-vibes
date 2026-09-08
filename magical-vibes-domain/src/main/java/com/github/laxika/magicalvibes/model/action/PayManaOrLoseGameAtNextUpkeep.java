package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger: at the beginning of the spell controller's next upkeep, they may pay
 * {@code manaCost}; if they do not, they lose the game.
 */
public record PayManaOrLoseGameAtNextUpkeep(UUID playerId, String manaCost, Card sourceCard)
        implements DelayedAction {
}
