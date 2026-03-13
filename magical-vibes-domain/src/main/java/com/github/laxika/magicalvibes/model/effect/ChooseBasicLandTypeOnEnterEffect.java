package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect placed in ON_ENTER_BATTLEFIELD to indicate that this permanent
 * requires a basic land type choice as it enters the battlefield
 * ("As ~ enters, choose a basic land type.").
 */
public record ChooseBasicLandTypeOnEnterEffect() implements CardEffect {
}
