package com.github.laxika.magicalvibes.model.effect;

/**
 * Non-targeting effect: the stack entry's target — a player, or a planeswalker whose controller is
 * used instead — can't gain life for the rest of the turn. It declares no target of its own and
 * piggybacks on a companion player-or-planeswalker effect's {@code targetId}, the same way
 * {@code DiscardEffect} with {@code TARGET_PLAYER_OR_PERMANENT_CONTROLLER} does on Blightning.
 *
 * <p>Per-player counterpart of the global {@link PlayersCantGainLifeThisTurnEffect} (Skullcrack) and
 * turn-scoped counterpart of {@link TargetPlayerCantGainLifeRestOfGameEffect} (Stigma Lasher).
 * Used by Flames of the Blood Hand.</p>
 */
public record TargetPlayerCantGainLifeThisTurnEffect() implements CardEffect {
}
