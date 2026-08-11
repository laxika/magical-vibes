package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may put a creature card with mana value X from your hand onto the battlefield. If you do,
 * return this creature to its owner's hand."
 *
 * <p>The hand choice carries the source permanent into the pending interaction. Once a card is
 * chosen, the source return is inserted as the next effect so the two actions remain conditional.
 */
public record PutCreatureFromHandWithManaValueXThenReturnSourceToHandEffect() implements CardEffect {
}
