package com.github.laxika.magicalvibes.model.amount;

/** Number of commander casts from the command zone by players in scope this game. */
public record CommanderCastsFromCommandZoneThisGame(CountScope scope) implements DynamicAmount {

    public CommanderCastsFromCommandZoneThisGame() {
        this(CountScope.CONTROLLER);
    }
}
