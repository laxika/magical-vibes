package com.github.laxika.magicalvibes.model.effect;

/**
 * Emblem marker for "Whenever a creature you control enters, you may draw a card."
 * The emblem handler registers the persistent trigger; the marker itself is stored in the emblem.
 */
public record DrawOnControlledCreatureEntersEffect() implements CardEffect {
}
