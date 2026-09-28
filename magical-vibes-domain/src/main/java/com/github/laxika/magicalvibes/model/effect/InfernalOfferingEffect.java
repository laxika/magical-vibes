package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution-time implementation of Infernal Offering's selected mode. The opponent and creature
 * choices are deliberately made while the spell resolves rather than being cast-time targets.
 */
public record InfernalOfferingEffect(boolean sacrificeMode) implements CardEffect {
}
