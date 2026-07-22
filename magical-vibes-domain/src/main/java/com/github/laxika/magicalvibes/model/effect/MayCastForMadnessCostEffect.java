package com.github.laxika.magicalvibes.model.effect;

/**
 * Accept/decline prompt during resolution of {@link MadnessMayCastEffect}: cast from exile for
 * the card's madness cost, or put it into the graveyard.
 */
public record MayCastForMadnessCostEffect() implements CardEffect {
}
