package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player votes for innocent or guilty. If guilty gets
 * more votes, cards exiled with the source permanent are put on the bottoms of their owners'
 * libraries.
 */
public record VoteForInnocentOrGuiltyEffect() implements CardEffect {
}
