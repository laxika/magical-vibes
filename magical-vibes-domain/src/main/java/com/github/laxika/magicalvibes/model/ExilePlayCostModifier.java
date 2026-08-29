package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A generic cost increase attached to a card that may be played from exile.
 *
 * @param permittedPlayerId the player who received permission to play the card
 * @param sourceControllerId the player whose opponents are taxed
 * @param amount the generic mana increase
 */
public record ExilePlayCostModifier(UUID permittedPlayerId, UUID sourceControllerId, int amount) {
}
