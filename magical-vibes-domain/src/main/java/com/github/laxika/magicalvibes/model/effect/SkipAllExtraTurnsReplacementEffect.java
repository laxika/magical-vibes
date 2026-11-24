package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Static replacement: "if [a player / an opponent] would begin an extra turn, that player skips that turn instead"
 * (CR 614.10), while its source remains on the battlefield. Gerrard's Hourglass Pendant names every player
 * ({@link Subjects#eachPlayer()}); Stranglehold and Trouble in Pairs the controller's opponents
 * ({@link #opponents()}). The players are judged by {@code SubjectResolutionService.namesPlayer}.
 *
 * @param players whose extra turns are skipped
 */
public record SkipAllExtraTurnsReplacementEffect(PlayerSubject players) implements ExtraTurnSkipReplacementEffect {

    public SkipAllExtraTurnsReplacementEffect {
        Objects.requireNonNull(players, "players");
    }

    /** "If a player would begin an extra turn, that player skips that turn instead." */
    public SkipAllExtraTurnsReplacementEffect() {
        this(Subjects.eachPlayer());
    }

    /** "If an opponent would begin an extra turn, that player skips that turn instead." */
    public static SkipAllExtraTurnsReplacementEffect opponents() {
        return new SkipAllExtraTurnsReplacementEffect(Subjects.eachOpponent());
    }
}
