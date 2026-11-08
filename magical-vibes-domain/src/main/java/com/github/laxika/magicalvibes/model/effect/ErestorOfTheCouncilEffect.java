package com.github.laxika.magicalvibes.model.effect;

/**
 * Erestor of the Council's "whenever players finish voting" effect: reads the votes from the stack entry's triggering
 * vote ({@code StackEntry.getTriggeringVotingResult()}), the vote as the players finished it.
 */
public record ErestorOfTheCouncilEffect() implements CardEffect {
}
