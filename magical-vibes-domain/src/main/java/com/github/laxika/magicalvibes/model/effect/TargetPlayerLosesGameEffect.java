package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Target player loses the game.
 *
 * <p>{@code playerId} may be pre-resolved (triggered abilities that already know the player, e.g.
 * Phage the Untouchable's combat-damage trigger); when it is {@code null} the handler falls back to
 * the target chosen for the stack entry, which is what spells and activated abilities use.
 */
public record TargetPlayerLosesGameEffect(UUID playerId) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return playerId == null
                ? TargetSpec.harmful(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
