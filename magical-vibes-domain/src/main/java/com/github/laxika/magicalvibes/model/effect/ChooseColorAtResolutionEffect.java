package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a color during resolution and stores it in the current game resolution
 * context. This is for effects whose source permanent is not the right place to store a
 * resolution-time choice.
 */
public record ChooseColorAtResolutionEffect() implements CardEffect {
}
