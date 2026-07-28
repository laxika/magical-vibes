package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, the controller chooses a color and it becomes the source permanent's chosen color,
 * replacing any previously chosen one. The resolution-time sibling of {@link ChooseColorOnEnterEffect},
 * for abilities that re-choose ("... and choose a color", Chromatic Armor). Effects that read
 * {@code Permanent.getChosenColor()} (e.g. {@link PreventColorDamageToEnchantedCreatureEffect})
 * immediately see the new color.
 */
public record ChooseColorForSourceEffect() implements CardEffect {
}
