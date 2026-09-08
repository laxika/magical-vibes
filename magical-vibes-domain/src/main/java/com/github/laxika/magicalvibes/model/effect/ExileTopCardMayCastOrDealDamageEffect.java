package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the controller's top library card and offers to cast it during the current resolution.
 * If the card is not cast, the source deals the configured damage to each opponent.
 */
public record ExileTopCardMayCastOrDealDamageEffect(int damage) implements CardEffect {
}
