package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Untap target permanent.
 *
 * @param targetPredicate optional predicate restricting which permanents can be targeted
 */
public record UntapTargetPermanentEffect(PermanentPredicate targetPredicate) implements CardEffect {

    public UntapTargetPermanentEffect() {
        this(null);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public PermanentPredicate targetPredicate() {
        return targetPredicate;
    }
}
