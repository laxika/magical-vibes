package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * A tap-for-mana triggered mana ability's "add an additional {C}" (the ability's controller adds it: Crypt Ghast,
 * Leyline of Abundance) or "its controller adds an additional {C}" ({@link #ofItsController}: the tapped permanent's
 * controller, as it is on the battlefield, else the player who tapped it: Vernal Bloom, Gauntlet of Might; a player who
 * taps another's land through Piracy is not its controller). Which permanents count ("you tap a Swamp", "a Forest is
 * tapped", "a creature you control", "this land") is the ability's trigger and its {@code withFilter}.
 *
 * @param color                     the color of the mana added
 * @param toTappedPermanentController whether the tapped permanent's controller adds it ("its controller adds")
 */
public record AddManaWhenTappedForManaEffect(ManaColor color, boolean toTappedPermanentController)
        implements TriggeredManaAbilityEffect {

    /** "Add an additional {C}": the ability's controller adds it. */
    public AddManaWhenTappedForManaEffect(ManaColor color) {
        this(color, false);
    }

    /** "Its controller adds an additional {C}": the tapped permanent's controller adds it. */
    public static AddManaWhenTappedForManaEffect ofItsController(ManaColor color) {
        return new AddManaWhenTappedForManaEffect(color, true);
    }
}
