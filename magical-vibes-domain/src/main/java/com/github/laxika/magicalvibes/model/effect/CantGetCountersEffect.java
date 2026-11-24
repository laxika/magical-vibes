package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "It can't have counters put on it for as long as this creature remains on the battlefield", "that player can't get
 * counters for as long as ..." (Suncleanser): a rule the resolution makes, not an ability, so it still applies after
 * the permanent loses its abilities. Each permanent {@code permanents} names and each player {@code players} names gets
 * this record as a floating effect lasting {@code duration}; {@code GameQueryService.cantHaveCounters} and the player
 * counter paths ({@code GameQueryService.playerCantGetCounters}) read it back. A "for as long as" duration that has
 * already ended as it resolves makes nothing (CR 611.2b).
 *
 * <p>Neither subject declares a target: the removal step beside it does ({@code targetDeclaredElsewhere()},
 * {@code targetPlayerDeclaredElsewhere()}).</p>
 */
public record CantGetCountersEffect(PermanentSubject permanents, PlayerSubject players, Duration duration)
        implements CardEffect {

    public CantGetCountersEffect {
        Objects.requireNonNull(duration, "duration");
        if ((permanents == null) == (players == null)) {
            throw new IllegalArgumentException("Exactly one of permanents and players");
        }
    }

    /** "[Permanents] can't have counters put on [them] [duration]." */
    public static CantGetCountersEffect onPermanents(PermanentSubject permanents, Duration duration) {
        return new CantGetCountersEffect(permanents, null, duration);
    }

    /** "[Players] can't get counters [duration]." */
    public static CantGetCountersEffect onPlayers(PlayerSubject players, Duration duration) {
        return new CantGetCountersEffect(null, players, duration);
    }
}
