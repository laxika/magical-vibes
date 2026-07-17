package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect (Lich's Mirror): if the controller of this permanent would lose the
 * game, instead they shuffle their hand, their graveyard, and all permanents they own into their
 * library, then draw seven cards and their life total becomes 20.
 *
 * <p>As part of the reset the source permanent (owned by the controller) is shuffled away, so the
 * replacement can only apply once. Poison counters are NOT reset (CR ruling): a player who would
 * lose from ten or more poison counters is saved once, then loses to the next state-based check.
 */
public record ReplaceControllerLossWithGameResetEffect() implements CardEffect {
}
