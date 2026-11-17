package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the target creature to its owner's hand, then prevents that card's owner from casting
 * spells with the creature's name until the resolving ability controller's next turn.
 */
public record ReturnTargetCreatureToHandAndOwnerCantCastSameNameUntilNextTurnEffect(String cardName)
        implements RemovalEffect {

    /** The effect a card is built with; resolution carries the returned creature's name on the effect it makes. */
    public ReturnTargetCreatureToHandAndOwnerCantCastSameNameUntilNextTurnEffect() {
        this(null);
    }


    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
