package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker for Zur's Weirding: "If a player would draw a card, they reveal it instead. Then any
 * other player may pay 2 life. If a player does, put that card into its owner's graveyard. Otherwise,
 * that player draws a card." Detected in {@code DrawService.resolveDrawCard} for every player's draw.
 */
public record ZursWeirdingDrawReplacementEffect() implements CardEffect {
}
