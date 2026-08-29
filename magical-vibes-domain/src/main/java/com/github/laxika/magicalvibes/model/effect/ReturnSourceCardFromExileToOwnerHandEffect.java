package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved as an exile trigger, returns the source card from exile to its owner's hand.
 * Uses the stack entry's card ID to identify the card that was exiled from the battlefield.
 */
public record ReturnSourceCardFromExileToOwnerHandEffect() implements CardEffect {
}
