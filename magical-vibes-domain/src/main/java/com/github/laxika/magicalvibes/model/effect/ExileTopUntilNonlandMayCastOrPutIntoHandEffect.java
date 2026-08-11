package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles cards from the controller's library until a nonland card is found, then offers that card
 * as a free cast when its mana value is within the life gained this turn; otherwise it goes to hand.
 */
public record ExileTopUntilNonlandMayCastOrPutIntoHandEffect() implements CardEffect {
}
