package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * One-shot effect that turns all controlled permanents matching the filter into
 * creatures with the given base power and toughness until end of turn.
 * They become artifact creatures (retain their other types) with base P/T set
 * to the specified values.
 *
 * <p>Example: The Antiquities War chapter III — "Artifacts you control become
 * artifact creatures with base power and toughness 5/5 until end of turn."
 */
public record AnimateControlledPermanentsEffect(
        int power,
        int toughness,
        PermanentPredicate filter
) implements CardEffect {
}
