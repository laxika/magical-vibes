package com.github.laxika.magicalvibes.model.effect;


import java.util.Objects;

/**
 * Stamps a static effect onto the player {@code player} names as a player-scoped effect until a player planeswalks: the
 * ability's controller by default (Agyrem), or target player ({@link Subjects#targetPlayer()}, Eloren Wilds, a harmful
 * player target). The stamped effect is independent of the source planar card's continued presence.
 *
 * @param staticEffect static effect to apply to the player's scope
 * @param player       who gets it
 */
public record GrantStaticEffectToPlayerUntilPlaneswalkEffect(CardEffect staticEffect, PlayerSubject player)
        implements CardEffect {

    public GrantStaticEffectToPlayerUntilPlaneswalkEffect {
        Objects.requireNonNull(player, "player");
    }

    public GrantStaticEffectToPlayerUntilPlaneswalkEffect(CardEffect staticEffect) {
        this(staticEffect, Subjects.you());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
