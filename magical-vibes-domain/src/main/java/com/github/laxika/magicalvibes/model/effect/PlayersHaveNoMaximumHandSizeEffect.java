package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: players have no maximum hand size (all players, while the source is on the
 * battlefield or face-up plane). Checked in {@code TurnCleanupService.hasNoMaximumHandSize}. Used
 * by Anvil of Bogardan and Stairs to Infinity. Distinct from {@link NoMaximumHandSizeEffect}, which
 * only affects the controller.
 */
public record PlayersHaveNoMaximumHandSizeEffect() implements CardEffect {
}
