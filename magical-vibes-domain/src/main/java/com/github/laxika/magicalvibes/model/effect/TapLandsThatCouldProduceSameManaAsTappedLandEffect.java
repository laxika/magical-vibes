package com.github.laxika.magicalvibes.model.effect;

/**
 * Land-tap trigger used by Mana Web. When it resolves, it taps the triggering land's controller's
 * lands that could produce any type of mana the triggering land could produce.
 */
public record TapLandsThatCouldProduceSameManaAsTappedLandEffect() implements CardEffect {
}
