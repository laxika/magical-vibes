package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker (STATIC) effect for Booby Trap. Detected in {@code DrawService}: the source's controller's
 * opponent (the chosen player) reveals each card they draw, and when they draw a card whose name
 * matches the source permanent's chosen name, the source is sacrificed and — if it was — deals 10
 * damage to that player. Pairs with a {@code ChooseCardNameOnEnterEffect} that stamps the chosen name.
 */
public record BoobyTrapEffect() implements CardEffect {
}
