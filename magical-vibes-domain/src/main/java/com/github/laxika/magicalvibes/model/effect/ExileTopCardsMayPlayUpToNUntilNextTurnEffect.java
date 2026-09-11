package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles cards from the top of the controller's library and grants a shared, limited play
 * permission for those cards until the end of that player's next turn.
 *
 * @param count the number of cards to exile
 * @param maxCardsToPlay the maximum number of those cards that may be played
 */
public record ExileTopCardsMayPlayUpToNUntilNextTurnEffect(
        DynamicAmount count,
        int maxCardsToPlay
) implements CardEffect {

    public ExileTopCardsMayPlayUpToNUntilNextTurnEffect {
        if (maxCardsToPlay < 1) {
            throw new IllegalArgumentException("maximum cards to play must be positive");
        }
    }

    public ExileTopCardsMayPlayUpToNUntilNextTurnEffect(int count, int maxCardsToPlay) {
        this(new Fixed(count), maxCardsToPlay);
    }
}
