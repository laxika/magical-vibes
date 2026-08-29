package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if a player taps a permanent for mana, it produces the configured
 * multiple of that mana instead. Used by Mana Reflection and Nyxbloom Ancient. Multiple instances
 * stack multiplicatively. Applied in mana-ability resolution via
 * {@code GameQueryService.manaProductionMultiplier}.
 */
public record ManaReflectionEffect(int multiplier) implements CardEffect {

    public ManaReflectionEffect() {
        this(2);
    }
}
