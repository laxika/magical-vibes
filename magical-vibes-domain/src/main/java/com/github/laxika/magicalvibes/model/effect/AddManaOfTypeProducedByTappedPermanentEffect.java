package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * A tapped-for-mana triggered mana ability's "add one mana of any type that permanent produced" (Eloren Wilds, Kinnan,
 * Roxanne), "whenever you tap a [land / permanent] for {C}, add an additional {C}" (Ultima, Forsaken Monument) and
 * "whenever a land is tapped for mana of the chosen color, add an additional one mana of that color" (Caged Sun,
 * Gauntlet of Power). It reads the types of mana the tap produced (the stack entry's produced mana colors,
 * {@code StackEntry.getProducedManaColors()}), so the ability waits for the tapped permanent's own mana choice: it
 * triggers only for a mana ability that produced that type as it resolved (CR 106.12a), the type a replacement made it
 * (CR 106.12b). A tap that produced none of it adds nothing.
 *
 * @param requiredColor              the type the tap must have produced, and the type added ({@code null}: any type
 *                                   it produced, its recipient choosing among several)
 * @param sourceChosenColor          whether the required type is the source permanent's chosen color instead (no
 *                                   color chosen: nothing is added)
 * @param toTappedPermanentController whether the tapped permanent's controller adds it ("its controller adds"), not
 *                                   the player who tapped it
 */
public record AddManaOfTypeProducedByTappedPermanentEffect(ManaColor requiredColor, boolean sourceChosenColor,
                                                           boolean toTappedPermanentController)
        implements ManaProducingEffect, TriggeredManaAbilityEffect {

    /** "Add one mana of any type that permanent produced." */
    public AddManaOfTypeProducedByTappedPermanentEffect() {
        this(null, false, false);
    }

    /** "Whenever you tap a permanent for {C}, add an additional {C}." */
    public AddManaOfTypeProducedByTappedPermanentEffect(ManaColor requiredColor) {
        this(requiredColor, false, false);
    }

    /** "… mana of the chosen color, add an additional one mana of that color" (Caged Sun). */
    public static AddManaOfTypeProducedByTappedPermanentEffect ofSourceChosenColor() {
        return new AddManaOfTypeProducedByTappedPermanentEffect(null, true, false);
    }

    /** "Its controller adds" (Gauntlet of Power): the tapped permanent's controller adds the mana. */
    public AddManaOfTypeProducedByTappedPermanentEffect toItsController() {
        return new AddManaOfTypeProducedByTappedPermanentEffect(requiredColor, sourceChosenColor, true);
    }

    @Override
    public boolean readsProducedMana() {
        return true;
    }
}
