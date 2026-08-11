package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns target permanent and all other permanents with the same name that match the supplied
 * predicate to their owners' hands.
 *
 * @param targetPredicate   predicate restricting the target
 * @param sameNamePredicate predicate restricting the other same-name permanents
 */
public record ReturnTargetPermanentAndAllWithSameNameToHandEffect(
        PermanentPredicate targetPredicate,
        PermanentPredicate sameNamePredicate
) implements RemovalEffect {

    public ReturnTargetPermanentAndAllWithSameNameToHandEffect(PermanentPredicate targetPredicate) {
        this(targetPredicate, targetPredicate);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), targetPredicate);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
