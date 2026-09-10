package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the target creature to its owner's hand, then prevents that card's owner from casting
 * spells with the creature's name until the resolving ability controller's next turn.
 */
public record ReturnTargetCreatureToHandAndOwnerCantCastSameNameUntilNextTurnEffect()
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
