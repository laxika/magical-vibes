package com.github.laxika.magicalvibes.model.amount;

/**
 * Number of commander casts from the command zone this game. By default this totals the casts by
 * players in scope; with {@code triggeringCommanderOnly} it counts only the casts of the commander
 * whose cast triggered the ability ("the number of times it's been cast from the command zone this
 * game" — Study Hall), so partner commanders keep separate counts.
 */
public record CommanderCastsFromCommandZoneThisGame(CountScope scope, boolean triggeringCommanderOnly)
        implements DynamicAmount {

    public CommanderCastsFromCommandZoneThisGame(CountScope scope) {
        this(scope, false);
    }

    public CommanderCastsFromCommandZoneThisGame() {
        this(CountScope.CONTROLLER, false);
    }

    /** The casts from the command zone of only the commander that triggered this ability. */
    public static CommanderCastsFromCommandZoneThisGame ofTriggeringCommander() {
        return new CommanderCastsFromCommandZoneThisGame(CountScope.CONTROLLER, true);
    }
}
