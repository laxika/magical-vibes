package com.github.laxika.magicalvibes.model.effect;

/**
 * Adds one mana of the source permanent's chosen color ("{T}: Add one mana of the chosen
 * color", Quirion Elves). The color comes from {@code Permanent.getChosenColor()}, so pair
 * this with an {@code ON_ENTER_BATTLEFIELD ChooseColorOnEnterEffect}. The activation path
 * rewrites the effect into a concrete {@link AwardManaEffect} at activation time; if no
 * color has been chosen the ability produces no mana.
 */
public record AwardChosenColorManaEffect() implements ManaProducingEffect {
}
