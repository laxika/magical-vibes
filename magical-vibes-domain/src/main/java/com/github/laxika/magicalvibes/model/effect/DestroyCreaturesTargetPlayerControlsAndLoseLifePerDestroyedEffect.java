package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys all creatures the targeted player controls, then the controller loses
 * {@code lifePerDestroyed} life for each creature actually destroyed this way
 * (indestructible / regenerated creatures do not count). Used by Rain of Daggers.
 */
public record DestroyCreaturesTargetPlayerControlsAndLoseLifePerDestroyedEffect(
        int lifePerDestroyed
) implements CardEffect {
}
