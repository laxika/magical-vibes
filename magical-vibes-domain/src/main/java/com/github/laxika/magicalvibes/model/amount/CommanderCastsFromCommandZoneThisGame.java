package com.github.laxika.magicalvibes.model.amount;

/** The number of times players in scope have cast a commander from the command zone this game. */
public record CommanderCastsFromCommandZoneThisGame(CountScope scope) implements DynamicAmount {

    public CommanderCastsFromCommandZoneThisGame() {
        this(CountScope.CONTROLLER);
    }
}
