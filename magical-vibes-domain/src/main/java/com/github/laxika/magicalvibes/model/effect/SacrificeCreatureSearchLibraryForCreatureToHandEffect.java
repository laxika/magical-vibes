package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifice a creature. If you do, search your library for a creature card,
 * reveal it, put it into your hand, then shuffle your library.
 *
 * <p>The sacrifice is part of the effect, not a cost — the ability can be
 * activated even with no creatures. If no creature is sacrificed (because
 * the controller controls none), the library search does not occur.</p>
 *
 * <p>Used by Garruk, the Veil-Cursed's −1 loyalty ability.</p>
 */
public record SacrificeCreatureSearchLibraryForCreatureToHandEffect() implements CardEffect {
}
