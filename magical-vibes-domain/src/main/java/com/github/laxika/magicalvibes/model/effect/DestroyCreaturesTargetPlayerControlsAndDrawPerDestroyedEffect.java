package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys all creatures the targeted player controls, then the controller draws
 * {@code cardsPerDestroyed} card(s) for each creature actually destroyed this way
 * (indestructible / regenerated creatures do not count). Used by Overwhelming Forces.
 */
public record DestroyCreaturesTargetPlayerControlsAndDrawPerDestroyedEffect(
        int cardsPerDestroyed
) implements CardEffect {
}
