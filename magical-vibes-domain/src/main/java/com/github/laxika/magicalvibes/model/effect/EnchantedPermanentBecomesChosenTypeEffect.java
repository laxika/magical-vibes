package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect for auras that set the enchanted permanent's subtype to the
 * chosen basic land type stored on the source permanent's {@code chosenSubtype} field.
 * "Enchanted land is the chosen type."
 *
 * <p>Works like {@link EnchantedPermanentBecomesTypeEffect} but reads the subtype
 * dynamically from the source permanent rather than a hard-coded value.
 *
 * <p>Used by cards like Convincing Mirage.
 */
public record EnchantedPermanentBecomesChosenTypeEffect() implements CardEffect {
}
