package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles cards from the top of the controller's library until a nonland card is found. If that
 * card's mana value is less than the controller's Mountain count, it may be cast without paying
 * its mana cost; otherwise, or if it is not cast, it is put into the controller's hand.
 */
public record ExileUntilNonlandCardMayCastIfManaValueLessThanMountainsEffect() implements CardEffect {
}
