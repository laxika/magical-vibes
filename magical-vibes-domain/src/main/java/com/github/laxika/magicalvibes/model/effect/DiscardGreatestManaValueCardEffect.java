package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the controller of the targeted permanent discard a card with the greatest mana value
 * among cards in their hand. The targeted permanent is supplied by a companion target group.
 */
public record DiscardGreatestManaValueCardEffect() implements CardEffect {
}
