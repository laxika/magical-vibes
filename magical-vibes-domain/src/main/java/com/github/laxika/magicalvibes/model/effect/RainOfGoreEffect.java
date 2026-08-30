package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement marker: if a spell or ability would cause its controller to gain life, that
 * player loses that much life instead. Used by Rain of Gore.
 */
public record RainOfGoreEffect() implements CardEffect {
}
