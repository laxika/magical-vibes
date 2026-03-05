package com.github.laxika.magicalvibes.model.effect;

/**
 * Land-tap trigger: whenever a land you control taps for mana of the source permanent's chosen color,
 * add one additional mana of that color. Used by Caged Sun and similar "mana doubling" effects
 * that depend on a chosen color stored on the permanent.
 */
public record AddExtraManaOfChosenColorOnLandTapEffect() implements CardEffect {
}
