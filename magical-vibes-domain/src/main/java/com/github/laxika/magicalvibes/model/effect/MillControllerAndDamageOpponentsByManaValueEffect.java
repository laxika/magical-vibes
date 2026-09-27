package com.github.laxika.magicalvibes.model.effect;

/** Mills cards from the controller's library, then deals each opponent damage equal to their
 * total mana value. Only cards that actually reach the graveyard count as milled this way. */
public record MillControllerAndDamageOpponentsByManaValueEffect(int count) implements CardEffect {
}
