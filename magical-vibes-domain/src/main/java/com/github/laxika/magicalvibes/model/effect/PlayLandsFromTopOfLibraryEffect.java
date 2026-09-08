package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker effect: "You may play lands from the top of your library."
 * The permission applies to the source permanent's controller and uses the normal land-play
 * timing and per-turn land-play allowance.
 */
public record PlayLandsFromTopOfLibraryEffect() implements CardEffect {
}
