package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the controller lose all but one life and remembers the actual amount lost on the source
 * permanent for a matching leaves-the-battlefield trigger.
 */
public record LoseAllButOneLifeEffect() implements CardEffect {
}
