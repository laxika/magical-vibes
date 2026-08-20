package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns target spell on the stack or target permanent to its owner's hand.
 */
public record ReturnTargetSpellOrPermanentToHandEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.permanent(), TargetPredicates.spellOnStack()));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
