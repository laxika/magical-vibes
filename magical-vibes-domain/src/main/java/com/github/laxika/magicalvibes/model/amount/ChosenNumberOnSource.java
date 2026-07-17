package com.github.laxika.magicalvibes.model.amount;

/**
 * The number last chosen for the source permanent ({@code Permanent.getChosenNumber()}) — the
 * value picked "as this enters" / at upkeep by a {@link
 * com.github.laxika.magicalvibes.model.effect.NumberChoiceEffect}. Reads 0 without a source
 * permanent. Used by Shapeshifter's characteristic-defining P/T (power = chosen number,
 * toughness = 7 − chosen number, expressed with {@link Sum}/{@link Scaled}/{@link Fixed}).
 */
public record ChosenNumberOnSource() implements DynamicAmount {
}
