package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the controller's library until a nonland card is revealed or the library is
 * empty, then gives the source permanent +X/+0 until end of turn, where X is that card's mana
 * value. All revealed cards are put on the bottom of the library in any order.
 */
public record RevealUntilNonlandBottomThenBoostSelfByManaValueEffect() implements CardEffect {
}
