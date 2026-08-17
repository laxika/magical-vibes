package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library, puts it into their hand, then has the source
 * permanent deal damage to itself equal to that card's mana value.
 */
public record RevealTopCardPutIntoHandAndDealDamageToSelfEffect() implements CardEffect {
}
