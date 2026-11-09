package com.github.laxika.magicalvibes.model.effect;

/**
 * A land-tap triggered mana ability's "that player adds one mana of any type that land produced": one mana of a type
 * the tapped land produced, read from the stack entry's produced mana colors as the ability resolves (the land's own
 * colors as it last existed when the tap reported none), added to the entry's triggering player (the player who tapped
 * the land). When the land produced several types, that player chooses one (Mana Flare's and Vorinclex, Voice of
 * Hunger's rulings). Mana Flare, Heartbeat of Spring, Mirari's Wake, Keeper of Progenitus, Winter's Night and similar
 * "mana doubling" abilities.
 *
 * <p>Which lands count ("a basic land", "a snow land", "a Mountain, Forest, or Plains") is the trigger's
 * {@code withFilter}, judged as the land is tapped.
 *
 * @param controllerOnly           when {@code true} the ability triggers only for lands the source's controller taps
 *                                 ("whenever you tap a land for mana": Vorinclex, Mirari's Wake); when {@code false}
 *                                 it is symmetric (Mana Flare)
 * @param matchesImprintedCardName when {@code true}, only lands whose names match the card imprinted on the source
 *                                 trigger (Extraplanar Lens)
 * @param monarchOnly              when {@code true}, the source's controller must be the monarch when the land is
 *                                 tapped (Regal Behemoth)
 * @param anyColor                 when {@code true}, the mana is "one mana of any color" instead of a type the land
 *                                 produced (Regal Behemoth): the player chooses one of the five colors (CR 106.1a),
 *                                 never colorless, whatever the land produced
 */
public record AddOneOfEachManaTypeProducedByLandEffect(
        boolean controllerOnly,
        boolean matchesImprintedCardName,
        boolean monarchOnly,
        boolean anyColor
) implements TriggeredManaAbilityEffect {

    public AddOneOfEachManaTypeProducedByLandEffect(boolean controllerOnly) {
        this(controllerOnly, false, false, false);
    }

    public AddOneOfEachManaTypeProducedByLandEffect(boolean controllerOnly, boolean matchesImprintedCardName) {
        this(controllerOnly, matchesImprintedCardName, false, false);
    }

    /** "Add an additional one mana of any color" (Regal Behemoth). */
    public static AddOneOfEachManaTypeProducedByLandEffect anyColor(boolean controllerOnly, boolean monarchOnly) {
        return new AddOneOfEachManaTypeProducedByLandEffect(controllerOnly, false, monarchOnly, true);
    }
}
