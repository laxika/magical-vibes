package com.github.laxika.magicalvibes.model.effect;

/**
 * Land-tap trigger: whenever a land you control taps for mana, add one mana of any type
 * that land produced. Used by Vorinclex, Voice of Hunger and similar "mana doubling" effects.
 *
 * <p>Per MTG ruling: if the land produces multiple types, this adds one mana of only one
 * of those types (player's choice). Unlike {@link AddExtraManaOfChosenColorOnLandTapEffect},
 * this effect does not depend on a chosen color.</p>
 */
public record AddOneOfEachManaTypeProducedByLandEffect() implements CardEffect {
}
