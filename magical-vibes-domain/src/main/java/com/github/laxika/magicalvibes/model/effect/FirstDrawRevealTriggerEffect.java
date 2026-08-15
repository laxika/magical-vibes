package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * A controller-draw trigger that only applies to the first card drawn by that controller on an
 * eligible turn.
 * The draw service reveals the card and selects the ordinary effect to put on the stack.
 */
public interface FirstDrawRevealTriggerEffect extends CardEffect {

    default boolean onlyOnControllerTurn() {
        return false;
    }

    CardEffect effectFor(Card drawnCard);
}
