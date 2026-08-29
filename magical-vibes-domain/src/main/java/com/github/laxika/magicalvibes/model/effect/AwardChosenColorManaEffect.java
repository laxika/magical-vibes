package com.github.laxika.magicalvibes.model.effect;

/**
 * Adds one mana of the source permanent's chosen color ("{T}: Add one mana of the chosen
 * color", Quirion Elves). The color comes from {@code Permanent.getChosenColor()}, so pair
 * this with an {@code ON_ENTER_BATTLEFIELD ChooseColorOnEnterEffect}. An optional restriction
 * routes the produced mana into a restricted bucket when the ability is activated.
 */
public record AwardChosenColorManaEffect(ManaRestriction restriction) implements ManaProducingEffect {

    public AwardChosenColorManaEffect() {
        this(null);
    }
}
