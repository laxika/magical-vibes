package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A cost adjustment or replacement attached to a card that may be played from exile.
 *
 * @param permittedPlayerId the player who received permission to play the card
 * @param sourceControllerId the player whose opponents are taxed
 * @param amount the generic mana increase
 * @param replacementManaCost optional mana cost that replaces the card's mana cost for this exile cast
 */
public record ExilePlayCostModifier(UUID permittedPlayerId, UUID sourceControllerId, int amount,
                                    String replacementManaCost) {

    public ExilePlayCostModifier(UUID permittedPlayerId, UUID sourceControllerId, int amount) {
        this(permittedPlayerId, sourceControllerId, amount, null);
    }
}
