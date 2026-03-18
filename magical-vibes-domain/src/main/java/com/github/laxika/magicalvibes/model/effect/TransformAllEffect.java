package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Transforms all permanents on the battlefield that match the given predicate.
 * Each matching permanent is transformed to its back face (or back to its front face if already transformed).
 *
 * @param filter the predicate that determines which permanents are transformed
 */
public record TransformAllEffect(PermanentPredicate filter) implements CardEffect {
}
