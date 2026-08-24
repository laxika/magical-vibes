package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * A controller-draw trigger that only applies to the first card drawn by that controller on an
 * eligible turn.
 * The draw service supplies the drawn card and selects the ordinary effect to put on the stack;
 * implementations can defer public reveal until a may-choice is accepted.
 */
public interface FirstDrawRevealTriggerEffect extends CardEffect {

    default boolean onlyOnControllerTurn() {
        return false;
    }

    default boolean revealBeforeChoice() {
        return true;
    }

    CardEffect effectFor(Card drawnCard);
}
