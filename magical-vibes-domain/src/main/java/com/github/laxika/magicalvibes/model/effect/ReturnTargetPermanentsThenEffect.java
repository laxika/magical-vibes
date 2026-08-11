package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns each chosen target permanent to its owner's hand, then resolves an existing rider once
 * with the number of permanents actually returned as the derived entry's event value.
 */
public record ReturnTargetPermanentsThenEffect(CardEffect thenEffect) implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
