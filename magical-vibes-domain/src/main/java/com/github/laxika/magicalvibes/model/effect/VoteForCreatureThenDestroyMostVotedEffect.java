package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player secretly votes for up to one creature. If at least one creature receives a vote,
 * every creature tied for the most votes is destroyed; otherwise each player draws a card.
 */
public record VoteForCreatureThenDestroyMostVotedEffect() implements CardEffect {
}
