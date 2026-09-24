package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills each player, then exiles up to two creature cards milled by this resolution and creates
 * one token whose base power and toughness equal the total power of the exiled cards.
 */
public record MillEachPlayerAndExileUpToTwoCreaturesCreateTokenWithTotalPowerEffect(
        CreateTokenEffect tokenTemplate
) implements CardEffect {
}
