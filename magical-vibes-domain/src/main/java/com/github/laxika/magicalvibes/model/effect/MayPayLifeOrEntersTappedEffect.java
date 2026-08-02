package com.github.laxika.magicalvibes.model.effect;

/**
 * As this permanent enters, its controller may pay life. If they do not, it enters tapped.
 *
 * <p>Placed in the static slot and handled during battlefield entry. The controller is prompted
 * after the permanent has been placed so declining can tap the actual permanent.
 */
public record MayPayLifeOrEntersTappedEffect(int lifeCost) implements ReplacementEffect {
}
