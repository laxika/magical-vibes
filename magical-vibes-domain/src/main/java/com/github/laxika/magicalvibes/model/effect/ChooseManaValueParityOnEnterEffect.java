package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect placed in ON_ENTER_BATTLEFIELD to indicate that this permanent
 * requires the controller to choose odd or even as it enters the battlefield
 * ("As this enchantment enters, choose odd or even."). The chosen parity is stored
 * on the source permanent via {@code Permanent.getChosenManaValueParity()}.
 */
public record ChooseManaValueParityOnEnterEffect() implements CardEffect {
}
