package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.Set;

/**
 * Stamps a static effect onto each player {@code player} names as a player-scoped floating effect for {@code duration}:
 * "until end of turn" (Web of Inertia, Yusri, Fortune's Flame), "until your next turn" (Teferi's Protection) or "until a
 * player planeswalks" (Agyrem, Celestine Reef). The stamped effect is independent of the source's continued presence.
 * A target player ({@link Subjects#targetPlayer()}) is a harmful player target.
 *
 * @param staticEffect static effect to apply to the player's scope
 * @param player       who gets it; the ability's controller by default
 * @param duration     how long it lasts
 */
public record GrantStaticEffectToPlayerEffect(CardEffect staticEffect, PlayerSubject player, Duration duration)
        implements CardEffect {

    private static final Set<Duration> DURATIONS =
            Set.of(Duration.UNTIL_END_OF_TURN, Duration.UNTIL_YOUR_NEXT_TURN, Duration.UNTIL_PLANESWALK);

    public GrantStaticEffectToPlayerEffect {
        Objects.requireNonNull(staticEffect, "staticEffect");
        Objects.requireNonNull(player, "player");
        if (!DURATIONS.contains(duration)) {
            throw new IllegalArgumentException("No player-scoped static grant lasts " + duration);
        }
    }

    /** The controller has {@code staticEffect} until end of turn. */
    public static GrantStaticEffectToPlayerEffect untilEndOfTurn(CardEffect staticEffect) {
        return new GrantStaticEffectToPlayerEffect(staticEffect, Subjects.you(), Duration.UNTIL_END_OF_TURN);
    }

    /** The controller has {@code staticEffect} until their next turn. */
    public static GrantStaticEffectToPlayerEffect untilYourNextTurn(CardEffect staticEffect) {
        return new GrantStaticEffectToPlayerEffect(staticEffect, Subjects.you(), Duration.UNTIL_YOUR_NEXT_TURN);
    }

    /** The controller has {@code staticEffect} until a player planeswalks. */
    public static GrantStaticEffectToPlayerEffect untilPlaneswalk(CardEffect staticEffect) {
        return new GrantStaticEffectToPlayerEffect(staticEffect, Subjects.you(), Duration.UNTIL_PLANESWALK);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
