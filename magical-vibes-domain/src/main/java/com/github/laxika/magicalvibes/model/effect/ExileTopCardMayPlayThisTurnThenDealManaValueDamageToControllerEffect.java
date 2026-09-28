package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the controller's library, lets them play it until end of turn, then has
 * the source deal damage to its controller equal to the exiled card's mana value.
 */
public record ExileTopCardMayPlayThisTurnThenDealManaValueDamageToControllerEffect() implements CardEffect {
}
