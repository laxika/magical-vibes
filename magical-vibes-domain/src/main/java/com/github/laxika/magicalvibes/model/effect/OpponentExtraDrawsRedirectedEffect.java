package com.github.laxika.magicalvibes.model.effect;

/**
 * Static draw-replacement effect: while a source with this effect is on the battlefield, if an
 * opponent of its controller would draw a card except the first one they draw in each of their
 * draw steps, that player skips that draw and the controller draws a card instead (Notion Thief).
 */
public record OpponentExtraDrawsRedirectedEffect() implements CardEffect {
}
