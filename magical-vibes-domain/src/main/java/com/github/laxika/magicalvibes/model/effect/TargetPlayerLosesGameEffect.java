package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Target player loses the game.
 *
 * <p>{@code playerId} may be pre-resolved by engine-built abilities that already know the player; when
 * it is {@code null} the handler reads the stack entry's player: the target chosen for a spell or
 * activated ability, or the player dealt combat damage for a "deals combat damage to a player, that
 * player loses the game" ability (Phage the Untouchable), whose collector gives the entry that player
 * as its non-targeting player (the card declares no player target).
 */
public record TargetPlayerLosesGameEffect(UUID playerId) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return playerId == null
                ? TargetSpec.harmful(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
