package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns each chosen target permanent to its owner's hand, then resolves an existing rider once
 * with a derived event value. With {@link EventStat#NONE}, the value is the number of permanents
 * actually returned; otherwise it is the sum of the selected last-known statistic across returned
 * permanents.
 */
public record ReturnTargetPermanentsThenEffect(EventStat stat, CardEffect thenEffect) implements RemovalEffect {

    public ReturnTargetPermanentsThenEffect(CardEffect thenEffect) {
        this(EventStat.NONE, thenEffect);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
