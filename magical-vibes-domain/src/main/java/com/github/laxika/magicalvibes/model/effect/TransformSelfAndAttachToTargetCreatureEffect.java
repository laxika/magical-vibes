package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Transform this permanent. If you do, attach it to target creature." The creature is chosen as the
 * ability is put on the stack; the transform and attachment happen on resolution.
 *
 * @param targetPredicate restricts the legal target creatures (for example, those one player controls)
 */
public record TransformSelfAndAttachToTargetCreatureEffect(PermanentPredicate targetPredicate) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(), targetPredicate);
    }
}
