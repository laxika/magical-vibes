package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library, puts it into their hand, then makes each
 * opponent lose life and the controller gain life equal to that card's mana value.
 */
public record RevealTopCardPutIntoHandAndDrainOpponentsEffect() implements CardEffect {
}
