package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles cards from the controller's library until a nonland permanent card is found, then lets
 * the controller put that card onto the battlefield or into their hand. Other exiled cards remain
 * in exile.
 */
public record ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect() implements CardEffect {
}
