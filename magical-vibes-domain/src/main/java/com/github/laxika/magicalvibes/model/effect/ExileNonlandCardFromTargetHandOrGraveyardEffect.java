package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the target player's hand, then lets the controller choose a nonland card from that
 * hand or that player's graveyard and exile it with a lasting permission to cast it.
 */
public record ExileNonlandCardFromTargetHandOrGraveyardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
