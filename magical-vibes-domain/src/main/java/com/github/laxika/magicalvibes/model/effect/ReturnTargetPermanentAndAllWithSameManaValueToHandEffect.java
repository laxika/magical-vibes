package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns the target permanent and every other matching permanent with the same mana value to
 * their owners' hands.
 *
 * @param targetPredicate   predicate restricting the target
 * @param matchingPredicate predicate restricting the other same-mana-value permanents
 */
public record ReturnTargetPermanentAndAllWithSameManaValueToHandEffect(
        PermanentPredicate targetPredicate,
        PermanentPredicate matchingPredicate
) implements RemovalEffect {

    public ReturnTargetPermanentAndAllWithSameManaValueToHandEffect(PermanentPredicate targetPredicate) {
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
