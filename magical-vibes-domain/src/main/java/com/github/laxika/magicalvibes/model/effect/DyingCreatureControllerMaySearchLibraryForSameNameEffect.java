package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for "whenever a creature dies, that creature's controller may search their library for a
 * card with the same name and put it onto the battlefield".
 *
 * <p>The death-trigger collector binds the dying creature's name and routes the optional search to
 * that creature's controller.
 */
public record DyingCreatureControllerMaySearchLibraryForSameNameEffect() implements CardEffect {
}
