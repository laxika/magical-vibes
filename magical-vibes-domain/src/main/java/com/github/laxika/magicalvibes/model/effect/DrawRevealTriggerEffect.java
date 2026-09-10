package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * A draw trigger that reveals the card drawn and selects the effect to put on the stack from its
 * card types.
 */
public interface DrawRevealTriggerEffect extends CardEffect {

    CardEffect effectFor(Card drawnCard);
}
