package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Choose a number between {@code minNumber} and {@code maxNumber}" for the first permanent created
 * earlier in this same resolution ({@code StackEntry.createdPermanentIds}) that matches {@code filter},
 * stored on that permanent via {@code Permanent.getChosenNumber()} and read by
 * {@link com.github.laxika.magicalvibes.model.amount.ChosenNumberOnCreatedPermanent}. Does nothing when
 * no created permanent matches, so a later effect reading the number sees 0. Esper Terra uses it for
 * "if it's a Saga, put up to three lore counters on it".
 *
 * @param filter restricts which created permanent is asked about; {@code null} accepts any
 */
public record ChooseNumberForCreatedPermanentEffect(int minNumber, int maxNumber, PermanentPredicate filter)
        implements CardEffect {
}
