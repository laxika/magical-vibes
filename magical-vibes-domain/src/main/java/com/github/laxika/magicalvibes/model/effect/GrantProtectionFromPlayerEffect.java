package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.Set;

/**
 * "You and [permanents] gain protection from [player] [duration]": "you and target permanent you control each gain
 * protection from the chosen player until end of turn" (Guardian Archon: {@code chosenPlayer()}, {@code target()}),
 * "until your next turn, you and planeswalkers you control gain protection from that player" (Eon Frolicker:
 * {@code targetPlayerDeclaredElsewhere()}, {@code controlledBy(you(), planeswalker)}). Protection from a player covers
 * every object that player controls, and each object they own that no other player controls (CR 702.16k).
 *
 * <p>The player and the permanents are found as it resolves (CR 611.2c): a {@link ProtectionFromPlayersEffect} is
 * floated on the controller and granted to each permanent for the duration, so a permanent keeps it if its control
 * changes and one entering later doesn't get it. Nothing happens if no player is named.
 *
 * @param from        the player protected from
 * @param permanents  the permanents that also gain it; a target subject declares a benign permanent target
 * @param duration    {@link Duration#UNTIL_END_OF_TURN} or {@link Duration#UNTIL_YOUR_NEXT_TURN}
 */
public record GrantProtectionFromPlayerEffect(PlayerSubject from, PermanentSubject permanents, Duration duration)
        implements CardEffect {

    private static final Set<Duration> DURATIONS = Set.of(Duration.UNTIL_END_OF_TURN, Duration.UNTIL_YOUR_NEXT_TURN);

    public GrantProtectionFromPlayerEffect {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(permanents, "permanents");
        if (from.declaresTarget()) {
            throw new IllegalArgumentException("The protected-from player is read, not targeted, by this effect");
        }
        if (!DURATIONS.contains(duration)) {
            throw new IllegalArgumentException("No player protection grant lasts " + duration);
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return permanents.declaresTarget() ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }
}
