package com.github.laxika.magicalvibes.model.effect;

/**
 * A land-tap triggered mana ability's "add an additional one mana of that color" for a land tapped for mana of the
 * source permanent's chosen color: one more of it when the tap produced it, or, for a tap that reported none, when the
 * land's basic land types make it add that color. Caged Sun's "causes you to add … add" goes to the ability's
 * controller; Gauntlet of Power's "its controller adds" ({@link #ofItsController}) to the land's controller. Which lands
 * count and who must tap them is the ability's trigger and its {@code withFilter}.
 *
 * @param toTappedLandController whether the tapped land's controller adds it ("its controller adds")
 */
public record AddExtraManaOfChosenColorOnLandTapEffect(boolean toTappedLandController)
        implements TriggeredManaAbilityEffect {

    /** "Add an additional one mana of that color" (Caged Sun): the ability's controller adds it. */
    public AddExtraManaOfChosenColorOnLandTapEffect() {
        this(false);
    }

    /** "Its controller adds an additional one mana of that color" (Gauntlet of Power). */
    public static AddExtraManaOfChosenColorOnLandTapEffect ofItsController() {
        return new AddExtraManaOfChosenColorOnLandTapEffect(true);
    }
}
