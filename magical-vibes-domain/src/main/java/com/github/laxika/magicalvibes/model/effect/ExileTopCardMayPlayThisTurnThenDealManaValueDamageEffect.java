package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the controller's library and lets them play it until end of turn. If the
 * exiled card is a nonland, a reflexive ability deals damage equal to its mana value to any target.
 */
public record ExileTopCardMayPlayThisTurnThenDealManaValueDamageEffect() implements CardEffect {
}
