package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys target permanent and all other permanents with the same name that match the supplied
 * predicate. Destruction respects regeneration and indestructible.
 *
 * @param targetPredicate   predicate restricting the target
 * @param sameNamePredicate predicate restricting the other same-name permanents
 */
public record DestroyTargetPermanentAndAllWithSameNameEffect(
        PermanentPredicate targetPredicate,
        PermanentPredicate sameNamePredicate
) implements RemovalEffect {

    public DestroyTargetPermanentAndAllWithSameNameEffect(PermanentPredicate targetPredicate) {
        this(targetPredicate, targetPredicate);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), targetPredicate);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
