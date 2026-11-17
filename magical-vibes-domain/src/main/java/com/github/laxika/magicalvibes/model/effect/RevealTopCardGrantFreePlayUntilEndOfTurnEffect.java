package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Reveals the top card of the controller's library and grants permission to play that card
 * without paying its mana cost until end of turn while it remains on top.
 *
 * <p>The card is chosen as the effect resolves, so the floating effect it makes carries a copy of this record that
 * names the card: it grants the permission only while that card is still the top card.
 *
 * @param cardId the revealed card the permission is for, or {@code null} on the effect a card is written with
 */
public record RevealTopCardGrantFreePlayUntilEndOfTurnEffect(UUID cardId) implements CardEffect {

    public RevealTopCardGrantFreePlayUntilEndOfTurnEffect() {
        this(null);
    }
}
