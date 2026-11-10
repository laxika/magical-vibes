package com.github.laxika.magicalvibes.model.effect;

/**
 * A land-tap triggered mana ability's "that player adds one mana of any type that land produced": one mana of a type
 * the tapped land produced, read from the stack entry's produced mana colors as the ability resolves (the land's own
 * colors as it last existed when the tap reported none), added to the entry's triggering player (the player who tapped
 * the land). When the land produced several types, that player chooses one (Mana Flare's and Vorinclex, Voice of
 * Hunger's rulings). Mana Flare, Heartbeat of Spring, Mirari's Wake, Keeper of Progenitus, Winter's Night and similar
 * "mana doubling" abilities.
 *
 * <p>Which lands count and who must tap them ("a basic land", "you tap a land", "a land with the same name as the
 * exiled card"), and Regal Behemoth's "while you're the monarch", are the trigger, its {@code withFilter} and its event
 * condition, judged as the land is tapped.
 *
 * @param anyColor when {@code true}, the mana is "one mana of any color" instead of a type the land produced (Regal
 *                 Behemoth): the player chooses one of the five colors (CR 106.1a), never colorless, whatever the land
 *                 produced
 */
public record AddOneOfEachManaTypeProducedByLandEffect(boolean anyColor) implements TriggeredManaAbilityEffect {

    /** "That player adds one mana of any type that land produced". */
    public AddOneOfEachManaTypeProducedByLandEffect() {
        this(false);
    }

    /** "Add an additional one mana of any color" (Regal Behemoth). */
    public static AddOneOfEachManaTypeProducedByLandEffect ofAnyColor() {
        return new AddOneOfEachManaTypeProducedByLandEffect(true);
    }

    @Override
    public boolean readsProducedMana() {
        return !anyColor;
    }
}
