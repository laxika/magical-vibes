package com.github.laxika.magicalvibes.model.effect;

/**
 * Shiko and Narset's flurry trigger: on the controller's second spell each turn,
 * copy it when it targets a permanent or player, otherwise draw a card.
 */
public record FlurryCopyOrDrawTriggerEffect() implements CardEffect {
}
