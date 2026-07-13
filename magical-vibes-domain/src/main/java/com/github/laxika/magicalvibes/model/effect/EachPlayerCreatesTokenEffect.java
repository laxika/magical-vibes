package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player (in turn order) creates tokens described by {@code token} under their own control.
 * The wrapped {@link CreateTokenEffect}'s amount is re-evaluated relative to each creating player,
 * so a {@code CountScope.CONTROLLER} count reads that player's own board (e.g. Waiting in the Weeds:
 * "each player creates a 1/1 Cat for each untapped Forest they control").
 */
public record EachPlayerCreatesTokenEffect(CreateTokenEffect token) implements CardEffect {
}
