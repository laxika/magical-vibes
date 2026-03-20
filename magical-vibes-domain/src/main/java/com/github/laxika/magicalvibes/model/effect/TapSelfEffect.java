package com.github.laxika.magicalvibes.model.effect;

/**
 * Taps the source permanent as part of an effect (not as a cost).
 * Unlike {@code requiresTap} on an activated ability, this can be activated
 * even when the permanent is already tapped — it simply stays tapped.
 *
 * <p>Used by cards like Drudge Sentinel: "{3}: Tap Drudge Sentinel.
 * It gains indestructible until end of turn."
 */
public record TapSelfEffect() implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }
}
