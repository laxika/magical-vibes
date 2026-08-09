package com.github.laxika.magicalvibes.model.effect;

/**
 * Cannibalize: the controller chooses one of two targeted creatures to exile, then puts two
 * +1/+1 counters on the other creature.
 *
 * <p>The effect is multi-target removal rather than single-target removal, because only one of the
 * two targeted creatures is exiled.</p>
 */
public record ExileOneOfTwoTargetCreaturesThenPutCountersOnOtherEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return null;
    }
}
