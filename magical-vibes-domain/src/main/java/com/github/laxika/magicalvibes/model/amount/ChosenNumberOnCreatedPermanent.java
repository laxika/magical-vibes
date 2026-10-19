package com.github.laxika.magicalvibes.model.amount;

/**
 * The number last chosen for the first permanent created earlier in this same resolution
 * ({@code Permanent.getChosenNumber()}, set by a
 * {@link com.github.laxika.magicalvibes.model.effect.ChooseNumberForCreatedPermanentEffect}). Reads 0
 * when no permanent was created. Used by Esper Terra's "put up to three lore counters on it".
 */
public record ChosenNumberOnCreatedPermanent() implements DynamicAmount {
}
