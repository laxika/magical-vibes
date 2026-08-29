package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns target spell or creature to its owner's hand.
 */
public record ReturnTargetSpellOrCreatureToHandEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.creature(), TargetPredicates.spellOnStack()));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
