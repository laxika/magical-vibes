package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: spells the source's controller casts whose name equals the card name chosen by
 * the source permanent ({@code permanent.chosenName}) cost {@code amount} generic mana less to cast.
 *
 * <p>Kept as its own record rather than a {@link ReduceCastCostForMatchingSpellsEffect} with a
 * {@code CardPredicate}, because the matching name lives on the source permanent and the card
 * predicate evaluation path carries no source permanent.
 *
 * <p>Pair with {@link ChooseCardNameOnEnterEffect} — Council of the Absolute.
 */
public record ReduceCastCostForChosenNameSpellsEffect(int amount) implements CardEffect {
}
