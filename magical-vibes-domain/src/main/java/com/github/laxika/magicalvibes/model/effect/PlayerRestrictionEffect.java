package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;

/**
 * "[Players] can't [restriction] [duration]": the players {@code players} names as the effect resolves can't do what
 * {@code restriction} says for {@code duration}. Each of them gets a floating effect (CR 611.2a) that lasts whatever
 * happens to the source afterwards; "players can't …" ({@link Subjects#eachPlayer()}) is one
 * floating effect on every player. The restriction is the effect's own text (Silence's "your opponents can't cast
 * spells this turn", Turf Wound's "target player can't play lands this turn", Single Combat's "players can't cast
 * creature or planeswalker spells until the end of your next turn"); a static ability's restriction is a different
 * record.
 *
 * <p>The floating effect stores this record; the casting, land-play and activation checks read
 * {@code GameData.restrictionsOnPlayer}.
 *
 * @param players     who is restricted; a declared {@link Subjects#targetPlayer()} makes this a harmful player target
 * @param restriction what they can't do
 * @param duration    how long: until end of turn, until your next turn, until the end of your next turn, during the
 *                    affected player's next turn, until a player planeswalks, or for the rest of the game
 */
public record PlayerRestrictionEffect(PlayerSubject players, PlayerRestriction restriction, Duration duration)
        implements CardEffect {

    public PlayerRestrictionEffect {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(restriction, "restriction");
        Objects.requireNonNull(duration, "duration");
        if (!DURATIONS.contains(duration)) {
            throw new IllegalArgumentException("Not a duration a player restriction states: " + duration);
        }
    }

    /** The durations the cards state, each of which the log line can say. */
    public static final List<Duration> DURATIONS = List.of(Duration.UNTIL_END_OF_TURN,
            Duration.UNTIL_YOUR_NEXT_TURN, Duration.UNTIL_END_OF_YOUR_NEXT_TURN,
            Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN, Duration.UNTIL_PLANESWALK, Duration.PERMANENT);

    /** "[Players] can't [restriction] this turn". */
    public static PlayerRestrictionEffect thisTurn(PlayerSubject players, PlayerRestriction restriction) {
        return new PlayerRestrictionEffect(players, restriction, Duration.UNTIL_END_OF_TURN);
    }

    @Override
    public TargetSpec targetSpec() {
        return players.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
