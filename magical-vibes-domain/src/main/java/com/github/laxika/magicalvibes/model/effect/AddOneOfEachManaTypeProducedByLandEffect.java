package com.github.laxika.magicalvibes.model.effect;

/**
 * Land-tap trigger: whenever a land taps for mana, the tapping player adds one mana of any type
 * that land produced. Used by Vorinclex, Voice of Hunger and similar "mana doubling" effects.
 *
 * <p>When {@code controllerOnly} is {@code true} the trigger fires only for lands the source's
 * controller taps (Vorinclex). When {@code false} it is symmetric — every player's land taps add
 * the extra mana to that player's pool (Mana Flare).</p>
 *
 * <p>Per MTG ruling: if the land produces multiple types, this adds one mana of only one
 * of those types (player's choice). Unlike {@link AddExtraManaOfChosenColorOnLandTapEffect},
 * this effect does not depend on a chosen color.</p>
 */
public record AddOneOfEachManaTypeProducedByLandEffect(boolean controllerOnly) implements CardEffect {
}
