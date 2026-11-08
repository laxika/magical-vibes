package com.github.laxika.magicalvibes.model.effect;

/**
 * Model of Unity's "whenever players finish voting" effect: reads the votes from the stack entry's triggering vote
 * ({@code StackEntry.getTriggeringVotingResult()}), the vote as the players finished it.
 */
public record ModelOfUnityEffect() implements CardEffect {
}
