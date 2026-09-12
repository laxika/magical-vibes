package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library and puts it onto the battlefield if it is a
 * permanent card. A nonpermanent card remains on top of the library.
 */
public record RevealTopCardPermanentToBattlefieldEffect() implements CardEffect {
}
