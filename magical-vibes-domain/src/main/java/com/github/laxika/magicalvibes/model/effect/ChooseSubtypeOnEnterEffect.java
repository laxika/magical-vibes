package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect placed in ON_ENTER_BATTLEFIELD to indicate that this permanent
 * requires a creature type choice as it enters the battlefield ("As ~ enters, choose a creature type.").
 */
public record ChooseSubtypeOnEnterEffect() implements CardEffect {
}
