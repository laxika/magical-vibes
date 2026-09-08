package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller names a card, then the target player reveals cards from the top of their library
 * until that card is revealed. If it is found, the other revealed cards go to that player's
 * graveyard and the named card goes back on top; otherwise the player shuffles their library.
 */
public record ChooseNameRevealUntilNamedPutOnTopRestToGraveyardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
