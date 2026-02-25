package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: players can't search libraries.
 * Any player may pay {2} to ignore this effect until end of turn (handled at search resolution time).
 */
public record CantSearchLibrariesEffect() implements CardEffect {
}
